package org.xwiki.android.sync.contactdb;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.util.Log;

public class EditContactContentObserver extends ContentObserver {
    private static ContentObserver commonContactsObserver = new EditContactContentObserver();

    public static void subscribeContactUpdates(Context c, Uri uri) {
        Log.i(
            EditContactContentObserver.class.getSimpleName(),
            "Subscribed to URI: " + uri
        );
        c.getContentResolver()
            .registerContentObserver(
                uri,
                false,
                commonContactsObserver
            );
    }

    public EditContactContentObserver() {
        super(null);
    }

    @Override
    public void onChange(
        boolean selfChange,
        Uri uri
    ) {
        super.onChange(selfChange, uri);
        Log.i(
            getClass().getSimpleName(),
            "Received: " + uri
        );
    }
}
