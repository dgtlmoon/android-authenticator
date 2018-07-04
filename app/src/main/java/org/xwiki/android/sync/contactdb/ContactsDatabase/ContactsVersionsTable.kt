package org.xwiki.android.sync.contactdb.ContactsDatabase

import android.content.ContentResolver
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.provider.ContactsContract

private const val contactVersionTableName = "ContactsVersions"

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
                append("${BaseColumns._ID} INTEGER PRIMARY KEY,")
                append("${ContactsContract.Data.RAW_CONTACT_ID} TEXT,")
                append("${ContactsContract.RawContacts.ACCOUNT_NAME} TEXT,")
                append("${ContactsContract.Data.MIMETYPE} TEXT,")
                append("${ContactsContract.Data.DATA_VERSION} TEXT")
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

            val updateSQL = StringBuilder()

            while (contactsDataVersionsCursor.moveToNext()) {
                val rowId = contactsDataVersionsCursor.getLong(
                    rowIdColumnIndex
                )
                val mimeType = contactsDataVersionsCursor.getString(
                    mimetypeColumnIndex
                )
                databaseHelper.readableDatabase.query(
                    contactVersionTableName,
                    null,
                    "${ContactsContract.Data.RAW_CONTACT_ID}=\"$rowId\"" +
                        "AND ${ContactsContract.RawContacts.ACCOUNT_NAME}=\"$accountName\"" +
                        "AND ${ContactsContract.Data.MIMETYPE}=\"$mimeType\"",
                    null,
                    null,
                    null,
                    null
                ) ?.use {
                    val idIndex = it.getColumnIndex(BaseColumns._ID)
                    if (it.moveToFirst()) {
                        val dataVersion = it.getInt(
                            it.getColumnIndex(ContactsContract.Data.DATA_VERSION)
                        )
                        val actualDataVersion = contactsDataVersionsCursor.getInt(
                            dataVersionColumnIndex
                        )
                        val id = it.getInt(idIndex)
                        if (dataVersion < actualDataVersion) {
                            updateSQL.append("UPDATE $contactVersionTableName ")
                            updateSQL.append("SET ${ContactsContract.Data.DATA_VERSION}=\"$actualDataVersion\" ")
                            updateSQL.append("WHERE ${BaseColumns._ID}=\"$id\";\n")
                            updated.add(rowId)
                        } else {
                            null
                        }
                    } else {
                        updateSQL.append("INSERT INTO $contactVersionTableName ")
                        updateSQL.append("(${ContactsContract.Data.RAW_CONTACT_ID},")
                        updateSQL.append("${ContactsContract.RawContacts.ACCOUNT_NAME},")
                        updateSQL.append("${ContactsContract.Data.MIMETYPE},")
                        updateSQL.append("${ContactsContract.Data.DATA_VERSION})")
                        updateSQL.append(" VALUES ")
                        updateSQL.append("(\"$rowId\", \"$accountName\",")
                        updateSQL.append("\"${contactsDataVersionsCursor.getString(mimetypeColumnIndex)}\",")
                        updateSQL.append("\"${contactsDataVersionsCursor.getString(dataVersionColumnIndex)}\"")
                        updateSQL.append(");")
                        updated.add(rowId)
                    }
                }
            }
            if (updateSQL.isNotEmpty()) {
                databaseHelper.writableDatabase.execSQL(
                    updateSQL.toString()
                )
            }
        }
        return updated
    }
}