/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.android.sync;

import android.app.Application;
import android.util.Log;

import org.xwiki.android.sync.contactdb.ContactsDatabase.ContactsDatabaseHolder;
import org.xwiki.android.sync.rest.BaseApiManager;
import org.xwiki.android.sync.utils.SharedPrefsUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Application class for authenticator
 *
 * @version $Id$
 */
public class AppContext extends Application {

    /**
     * Entry pair Server address - Base Api Manager
     */
    private static Map.Entry<String, BaseApiManager> baseApiManager;

    /**
     * Logging tag
     */
    private static final String TAG = "AppContext";

    /**
     * Instance of context to use it in static methods
     */
    private static AppContext instance;

    /**
     * Represent work with contacts database
     *
     * @since 0.5
     */
    private static ContactsDatabaseHolder contactsDB;

    /**
     * @return known AppContext instance
     */
    public static AppContext getInstance() {
        return instance;
    }

    /**
     * @return actual base url
     */
    public static String currentBaseUrl() {
        return SharedPrefsUtils.getValue(instance, Constants.SERVER_ADDRESS, null);
    }

    /**
     * @return {@link #contactsDB} if not null or try to create new using {@link #getInstance()} as
     * context
     *
     * @since 0.5
     */
    public static ContactsDatabaseHolder getContactsDatabase() {
        if (contactsDB != null) {
            return contactsDB;
        } else {
            contactsDB = new ContactsDatabaseHolder(
                getInstance()
            );
            return contactsDB;
        }
    }

    /**
     * Set {@link #instance} to this object.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "on create");
    }

    /**
     * Add app as authorized
     *
     * @param packageName Application package name to add as authorized
     */
    public static void addAuthorizedApp(String packageName) {
        Log.d(TAG, "packageName=" + packageName);
        List<String> packageList = SharedPrefsUtils.getArrayList(instance.getApplicationContext(), Constants.PACKAGE_LIST);
        if (packageList == null) {
            packageList = new ArrayList<>();
        }
        packageList.add(packageName);
        SharedPrefsUtils.putArrayList(instance.getApplicationContext(), Constants.PACKAGE_LIST, packageList);
    }

    /**
     * Check that application with packageName is authorised.
     *
     * @param packageName Application package name
     * @return true if application was authorized
     */
    public static boolean isAuthorizedApp(String packageName) {
        List<String> packageList = SharedPrefsUtils.getArrayList(
            instance.getApplicationContext(),
            Constants.PACKAGE_LIST
        );
        return packageList != null && packageList.contains(packageName);
    }

    /**
     * @return Current {@link #baseApiManager} value or create new and return
     *
     * @since 0.4
     */
    public static BaseApiManager getApiManager() {
        String url = currentBaseUrl();
        if (baseApiManager == null || !baseApiManager.getKey().equals(url)) {
            baseApiManager = new AbstractMap.SimpleEntry<>(
                url,
                new BaseApiManager(url)
            );
        }
        return baseApiManager.getValue();
    }
}
