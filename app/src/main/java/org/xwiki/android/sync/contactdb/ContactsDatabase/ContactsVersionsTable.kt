package org.xwiki.android.sync.contactdb.ContactsDatabase

import android.content.ContentResolver
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.provider.ContactsContract
import androidx.core.database.sqlite.transaction

private const val contactVersionTableName = "ContactsVersions"

private const val idField = BaseColumns._ID
private const val rowIdField = ContactsContract.Data.RAW_CONTACT_ID
private const val accountNameField = ContactsContract.RawContacts.ACCOUNT_NAME
private const val mimetypeField = ContactsContract.Data.MIMETYPE
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
                append("$mimetypeField TEXT,")
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
            arrayOf(
                ContactsContract.Data.RAW_CONTACT_ID,
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.DATA_VERSION
            ),
            "${ContactsContract.RawContacts.ACCOUNT_NAME}=\"$accountName\"",
            null,
            null
        ) ?.use {
            contactsDataVersionsCursor ->
            val rowIdColumnIndex = contactsDataVersionsCursor.getColumnIndex(
                ContactsContract.Data.RAW_CONTACT_ID
            )
            val mimetypeColumnIndex = contactsDataVersionsCursor.getColumnIndex(
                ContactsContract.Data.MIMETYPE
            )
            val dataVersionColumnIndex = contactsDataVersionsCursor.getColumnIndex(
                ContactsContract.Data.DATA_VERSION
            )

            while (contactsDataVersionsCursor.moveToNext()) {
                val rowId = contactsDataVersionsCursor.getLong(
                    rowIdColumnIndex
                )
                val mimeType = contactsDataVersionsCursor.getString(
                    mimetypeColumnIndex
                )
                val version = contactsDataVersionsCursor.getInt(
                    dataVersionColumnIndex
                )
                databaseHelper.readableDatabase.query(
                    contactVersionTableName,
                    null,
                    "$rowIdField=\"$rowId\" " +
                        "AND $accountNameField=\"$accountName\" " +
                        "AND $mimetypeField=\"$mimeType\"",
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
                            if (dbVersion < version) {
                                updated.add(rowId)
                            }
                            "UPDATE $contactVersionTableName SET $versionField=\"$dbVersion\" WHERE $idField=\"$id\""
                        } else {
                            null
                        }
                    } else {
                        updated.add(rowId)
                        "INSERT INTO $contactVersionTableName " +
                            "($rowIdField,$accountNameField,$mimetypeField,$versionField) " +
                            "VALUES (\"$rowId\",\"$accountName\",\"$mimeType\",\"$version\")"
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