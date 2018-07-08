package org.xwiki.android.sync.contactdb.ContactsDatabase

import android.content.ContentResolver
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.provider.ContactsContract
import android.util.Log
import androidx.core.database.getString
import androidx.core.database.getStringOrNull
import androidx.core.database.sqlite.transaction

private const val contactVersionTableName = "ContactsVersions"

private const val idField = BaseColumns._ID
private const val rowIdField = ContactsContract.Data.RAW_CONTACT_ID
private const val accountNameField = ContactsContract.RawContacts.ACCOUNT_NAME
private const val versionField = ContactsContract.Data.DATA_VERSION

/**
 * This class provide work with contacts versions. Contains four columns:
 * id of contact, accountName, mimetype, version.
 *
 * @version $Id$
 * @since 0.5
 */
class ContactsVersionsTable(
    private val databaseHelper: SQLiteOpenHelper
) {

    fun init(db: SQLiteDatabase) {
        db.execSQL(
            StringBuilder().run {
                append("CREATE TABLE $contactVersionTableName (")
                append("$idField INTEGER PRIMARY KEY,")
                append("$rowIdField TEXT,")
                append("$accountNameField TEXT,")
                append("$versionField TEXT")
                append(");")
                toString()
            }
        )
    }

    fun updateVersions(
        resolver: ContentResolver,
        accountName: String
    ): Set<Long> {
        val updated = mutableSetOf<Long>()

        val updates = mutableListOf<String>()
        resolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            "${ContactsContract.RawContacts.ACCOUNT_NAME}=\"$accountName\"",
            null,
            null
        ) ?.use {
            contactsDataVersionsCursor ->
            val rowIdColumnIndex = contactsDataVersionsCursor.getColumnIndex(
                ContactsContract.Data.RAW_CONTACT_ID
            )
            val idColumnImdex = contactsDataVersionsCursor.getColumnIndex(
                BaseColumns._ID
            )
            val dataVersionColumnIndex = contactsDataVersionsCursor.getColumnIndex(
                ContactsContract.Data.DATA_VERSION
            )

            while (contactsDataVersionsCursor.moveToNext()) {
                val rowId = contactsDataVersionsCursor.getLong(
                    rowIdColumnIndex
                )

                val originalId = contactsDataVersionsCursor.getString(
                    idColumnImdex
                )

                val version = contactsDataVersionsCursor.getInt(
                    dataVersionColumnIndex
                )

                contactsDataVersionsCursor.getStringOrNull(ContactsContract.RawContacts.DIRTY) ?.let {
                    if (it == "1") {
                        Log.d(this::class.java.simpleName, "Need to synchronize: $rowId")
                    }
                }
                databaseHelper.readableDatabase.query(
                    contactVersionTableName,
                    null,
                        "$idField=\"$originalId\"",
                    null,
                    null,
                    null,
                    null
                ).use {
                    c ->
                    if (c.moveToFirst()) {
                        val dbVersion = c.getInt(
                            c.getColumnIndex(versionField)
                        )
                        val id = c.getInt(
                            c.getColumnIndex(idField)
                        )

                        if (dbVersion != version) {
                            updated.add(rowId)
                            "UPDATE $contactVersionTableName SET $versionField=\"$dbVersion\" WHERE $idField=\"$id\""
                        } else {
                            null
                        }
                    } else {
                        updated.add(rowId)
                        "INSERT INTO $contactVersionTableName " +
                            "($rowIdField,$accountNameField,$idField,$versionField) " +
                            "VALUES (\"$rowId\",\"$accountName\",\"$originalId\",\"$version\")"
                    }
                } ?.let {
                    updates.add(it)
                }
            }
        }
        databaseHelper.writableDatabase.let {
            db ->
            db.transaction {
                updates.forEach {
                    db.execSQL(it)
                }
            }
        }
        return updated
    }
}