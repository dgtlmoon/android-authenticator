package org.xwiki.android.sync.contactdb

import android.database.ContentObserver
import android.net.Uri
import android.provider.ContactsContract
import org.xwiki.android.sync.AppContext
import org.xwiki.android.sync.contactdb.ContactsDatabase.ContactsVersionsTable

class ContactsUpdatesObserver(
    private val accountName: String,
    private val contactsVersionsTable: ContactsVersionsTable
) : ContentObserver(null) {

    init {
        AppContext.getInstance().contentResolver.registerContentObserver(
            ContactsContract.AUTHORITY_URI,
            false,
            this
        )
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        uri ?.let {
            contactsVersionsTable.updateVersions(
                AppContext.getInstance().contentResolver,
                accountName
            ).forEach {
                println("Was updated user $it")
            }
        }
    }

    override fun deliverSelfNotifications(): Boolean {
        return false
    }
}