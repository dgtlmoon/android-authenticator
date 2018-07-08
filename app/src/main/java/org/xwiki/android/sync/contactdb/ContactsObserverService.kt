package org.xwiki.android.sync.contactdb

import android.accounts.AccountManager
import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import org.xwiki.android.sync.AppContext
import org.xwiki.android.sync.Constants

fun Context.initContactsChangingListener() {
    val intent = Intent(this, ContactsObserverService::class.java)
    startService(intent)
}

class ContactsObserverService : Service() {
    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException()
    }

    private var contactsUpdatedObserver: List<ContactsUpdatesObserver>? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(this::class.java.simpleName, "onHandleIntent")
        unbindObservers()

        val contactsUpdatesTable = AppContext.getContactsDatabase().contactsVersionsTable
        contactsUpdatedObserver = AccountManager.get(applicationContext).getAccountsByType(
            Constants.ACCOUNT_TYPE
        ).map {
            ContactsUpdatesObserver(
                it.name,
                contactsUpdatesTable
            ).also {
                contentResolver.registerContentObserver(
                    ContactsContract.AUTHORITY_URI,
                    false,
                    it
                )
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        unbindObservers()
    }

    private fun unbindObservers() {
        contactsUpdatedObserver ?.forEach {
            contentResolver.unregisterContentObserver(it)
        }

        contactsUpdatedObserver = null
    }
}