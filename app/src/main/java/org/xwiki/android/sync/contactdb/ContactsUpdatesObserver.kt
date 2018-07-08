package org.xwiki.android.sync.contactdb

import android.database.ContentObserver
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.xwiki.android.sync.AppContext
import org.xwiki.android.sync.contactdb.ContactsDatabase.ContactsVersionsTable

class ContactsUpdatesObserver(
    private val accountName: String,
    private val contactsVersionsTable: ContactsVersionsTable
) : ContentObserver(null) {

    private var updateAsync: Job? = null

    private val task: suspend CoroutineScope.() -> Unit = {
        Log.d(this::class.java.simpleName, "Start get updates")
        contactsVersionsTable.updateVersions(
            AppContext.getInstance().contentResolver,
            accountName
        ).forEach {
            Log.d(this::class.java.simpleName, "Was updated: $it")
        }
        updateAsync = null
        Log.d(this::class.java.simpleName, "Complete get updates")
    }

    init {
        AppContext.getInstance().contentResolver.registerContentObserver(
            ContactsContract.AUTHORITY_URI,
            false,
            this
        )
    }

    @Synchronized
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        uri ?.let {
            updateAsync ?:let {
                updateAsync = launch(block = task)
            }
        }
    }

    override fun deliverSelfNotifications(): Boolean {
        return false
    }
}