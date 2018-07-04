package org.xwiki.android.sync.contactdb.ContactsDatabase

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private const val databaseName: String = "ContactsManagement"

private const val dbVersion = 1

/**
 *
 * @version $Id$
 * @since 0.5
 */
class ContactsDatabaseHolder(
    context: Context
) : SQLiteOpenHelper(
    context,
    databaseName,
    null,
    dbVersion
) {
    val contactsVersionsTable: ContactsVersionsTable by lazy {
        ContactsVersionsTable(this)
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db ?.also {
            contactsVersionsTable.init(db)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.i(this::class.java.simpleName, "For some of reason, was asked about upgrade")
    }
}