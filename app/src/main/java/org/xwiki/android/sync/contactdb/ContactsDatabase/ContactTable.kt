package org.xwiki.android.sync.contactdb.ContactsDatabase

import android.database.sqlite.SQLiteDatabase

interface ContactTable {
    fun init(db: SQLiteDatabase)
}