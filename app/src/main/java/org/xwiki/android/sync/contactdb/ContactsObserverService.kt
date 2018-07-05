package org.xwiki.android.sync.contactdb

import android.accounts.AccountManager
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.util.Log
import org.xwiki.android.sync.AppContext
import org.xwiki.android.sync.Constants

fun Context.initContactsChangingListener() {
    val intent = Intent(this, ContactsObserverService::class.java)
    startService(intent)
}

class ContactsObserverService : IntentService(
    ContactsObserverService::class.java.simpleName
) {
    private var contactsUpdatedObserver: List<ContactsUpdatesObserver>? = null

    /**
     *
     * @return {@link #START_REDELIVER_INTENT} for be sure that service will be recreated
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(this::class.java.simpleName, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
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