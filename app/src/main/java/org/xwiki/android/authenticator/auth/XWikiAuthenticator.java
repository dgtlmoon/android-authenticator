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
package org.xwiki.android.authenticator.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;

import org.xwiki.android.authenticator.AccountGeneral;
import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.activities.GrantPermissionActivity;
import org.xwiki.android.authenticator.rest.HttpResponse;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.utils.Loger;
import org.xwiki.android.authenticator.utils.SharedPrefsUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static org.xwiki.android.authenticator.AccountGeneral.*;

/**
 * @version $Id: $
 */
public class XWikiAuthenticator extends AbstractAccountAuthenticator {
    private String TAG = "XWikiAuthenticator";
    private final Context mContext;

    public XWikiAuthenticator(Context context) {
        super(context);
        // I hate you! Google - set mContext as protected!
        this.mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d("xwiki", TAG + "> addAccount");

        int uid = options.getInt(AccountManager.KEY_CALLER_UID);
        String packageName = mContext.getPackageManager().getNameForUid(uid);

        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(AuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AuthenticatorActivity.PARAM_APP_UID, uid);
        intent.putExtra(AuthenticatorActivity.PARAM_APP_PACKAGENAME, packageName);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

        Log.d("xwiki", TAG + "> getAuthToken");

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager am = AccountManager.get(mContext);
        String accountName = am.getUserData(account, AccountManager.KEY_USERDATA);
        String accountPassword = am.getUserData(account, AccountManager.KEY_PASSWORD);
        String accountServer = am.getUserData(account, AuthenticatorActivity.PARAM_USER_SERVER);

        int uid = options.getInt(AccountManager.KEY_CALLER_UID);
        String packageName = mContext.getPackageManager().getNameForUid(uid);

        // If the caller requested an authToken type we don't support, then
        // return an error  if checking validity tokenType != TYPE+PackegeName
        if (!authTokenType.equals(AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS+packageName)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }


        if(!AppContext.isAuthorizedApp(uid)){
            final Intent intent = new Intent(AppContext.getInstance().getApplicationContext(), GrantPermissionActivity.class);
            intent.putExtra("uid",uid);
            intent.putExtra("packageName", packageName);
            intent.putExtra("accountName",account.name);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            //Bundle bundle = new Bundle();
            //bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            //return bundle;
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            AppContext.getInstance().getApplicationContext().startActivity(intent);
            return null;
        }

        //authTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;
        String authToken = am.peekAuthToken(account, authTokenType);
        Log.d("xwiki", TAG + "> peekAuthToken returned - " + authToken);

        // Lets give another try to authenticate the user
        if (TextUtils.isEmpty(authToken)) {
            //if other AuthTokenTypes have a cached token, just return this consistent token.
            //mainly used when a new app being installed. and at this time, there're no cached
            //authToken corresponding to its authTokenType, but other AuthTokenTypes may have a
            //cached token.
            String consistentToken = getTheSameAuthToken(am, account);
            if(consistentToken!=null){
                am.setAuthToken(account, authTokenType, consistentToken);
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, consistentToken);
                return result;
            }

            //if  having no cached token, request server for new token and refreshAllAuthTokenType
            //make all cached authTokenType-tokens consistent.
            try {
                Log.d("xwiki", TAG + "> re-authenticating with the existing password");
                HttpResponse httpResponse = XWikiHttp.login(accountServer, accountName, accountPassword);
                authToken = httpResponse.getHeaders().get("Set-Cookie");
                Loger.debug("XWikiAuthenticator, authtoken="+authToken);
            } catch (IOException e) {
                e.printStackTrace();
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ERROR_MESSAGE, "getAuthToken network error!!!");
                return result;
            }
        }

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            //refresh all tokentype for all apps' package
            refreshAllAuthTokenType(am, account, authToken);

            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_NAME, account.name);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }


    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Log.d(TAG, "getAuthTokenLabel,"+ authTokenType);
        if (AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
            return AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        else if (AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType))
            return AUTHTOKEN_TYPE_READ_ONLY_LABEL;
        else
            return authTokenType + " (Label)";
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        //final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        //intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        //intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, account.type);
        //intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, AccountGeneral.ACCOUNT_TYPE);
        //intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_NAME, account.name);
        //final Bundle bundle = new Bundle();
        //bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        //return bundle;
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    public static void refreshAllAuthTokenType(AccountManager am, Account account, String authToken){
        List<String> packageList = SharedPrefsUtil.getArrayList(AppContext.getInstance().getApplicationContext(), "packageList");
        if(packageList == null || packageList.size()==0 ) return;
        for(String item : packageList){
            String tokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS + item;
            am.setAuthToken(account, tokenType, authToken);
        }
    }

    public static String getTheSameAuthToken(AccountManager am, Account account){
        List<String> packageList = SharedPrefsUtil.getArrayList(AppContext.getInstance().getApplicationContext(), "packageList");
        if(packageList == null || packageList.size()==0 ) return null;
        String tokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS + packageList.get(0);
        String authToken = am.peekAuthToken(account, tokenType);
        return authToken;
    }


}
