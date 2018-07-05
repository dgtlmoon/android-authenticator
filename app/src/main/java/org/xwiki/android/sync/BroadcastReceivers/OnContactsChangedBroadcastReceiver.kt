package org.xwiki.android.sync.BroadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.xwiki.android.sync.contactdb.initContactsChangingListener

class OnPhoneLoadedBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(this::class.java.simpleName, "onReceive: $context; $intent;")
        context ?. initContactsChangingListener()
    }
}