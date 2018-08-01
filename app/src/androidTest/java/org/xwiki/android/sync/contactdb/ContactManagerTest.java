package org.xwiki.android.sync.contactdb;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xwiki.android.sync.AppContext;
import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.bean.MutableInternalXWikiUserInfo;
import org.xwiki.android.sync.bean.XWikiUserFull;

import java.util.List;

import static org.junit.Assert.*;
import static org.xwiki.android.sync.contactdb.ContactOperationsKt.getContactRowId;
import static org.xwiki.android.sync.contactdb.ContactOperationsKt.getUserInfo;
import static org.xwiki.android.sync.contactdb.ContactOperationsKt.toContentProviderOperations;

/**
 * ContactManagerTest
 */
public class ContactManagerTest {

    private final Account account = new Account("test", Constants.ACCOUNT_TYPE);
    private final String password = "testtest";
    private final XWikiUserFull xWikiUserFull = new XWikiUserFull();
    private Uri userUri = null;

    public ContactManagerTest() {
        xWikiUserFull.space = "xwiki";
        xWikiUserFull.wiki = "XWiki";
        xWikiUserFull.pageName = "test";
        xWikiUserFull.id = xWikiUserFull.convertId();
        xWikiUserFull.setFirstName("FirstName");
        xWikiUserFull.setLastName("SecondName");
        xWikiUserFull.setEmail("email@email.test");
        xWikiUserFull.setPhone("+1-(234)-567-8901");
        xWikiUserFull.setAddress("It is example address");
        xWikiUserFull.setCity("City name");
        xWikiUserFull.setCountry("Country name");
        xWikiUserFull.setComment("Some comment");
        xWikiUserFull.setCompany("Company name");
    }

    @Before
    public void addAccount() {
        AccountManager.get(AppContext.getInstance()).addAccountExplicitly(
            account,
            password,
            null
        );
    }

    @Before
    @Test
    public void addTestContact() {
        ContentResolver cr = AppContext.getInstance().getContentResolver();

        BatchOperation batchOperation = new BatchOperation(
            cr
        );

        List<ContentProviderOperation> operations = toContentProviderOperations(
            xWikiUserFull,
            cr,
            account.name
        );

        for (ContentProviderOperation operation : operations) {
            batchOperation.add(
                operation
            );
        }

        for (Uri uri : batchOperation.execute()) {
            userUri = uri;
            if (userUri != null) {
                break;
            }
        }

        assertNotNull(userUri);
    }

    @Test
    public void getTestContactIsCorrect() {
        ContentResolver cr = AppContext.getInstance().getContentResolver();

        Long rowId = getContactRowId(
            cr,
            userUri
        );

        MutableInternalXWikiUserInfo getUserInfo = getUserInfo(
            cr,
            rowId,
            XWikiUserFull.splitId(xWikiUserFull.id)
        );

        assertEquals(getUserInfo.getFirstName(), xWikiUserFull.getFirstName());
        assertEquals(getUserInfo.getLastName(), xWikiUserFull.getLastName());
        assertEquals(getUserInfo.getPhone(), xWikiUserFull.getPhone());
        assertEquals(getUserInfo.getEmail(), xWikiUserFull.getEmail());
        assertEquals(getUserInfo.getAddress(), xWikiUserFull.getAddress());
        assertEquals(getUserInfo.getCity(), xWikiUserFull.getCity());
        assertEquals(getUserInfo.getCountry(), xWikiUserFull.getCountry());
        assertEquals(getUserInfo.getCompany(), xWikiUserFull.getCompany());
        assertEquals(getUserInfo.getComment(), xWikiUserFull.getComment());
        assertEquals(getUserInfo.getSpace(), xWikiUserFull.space);
        assertEquals(getUserInfo.getWiki(), xWikiUserFull.wiki);
        assertEquals(getUserInfo.getPageName(), xWikiUserFull.pageName);
    }

    @After
    public void removeTestAccount() {
        AccountManager.get(AppContext.getInstance()).removeAccount(
            account,
            null,
            null
        );
    }
}