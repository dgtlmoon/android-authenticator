package org.xwiki.android.sync.activities;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.android.sync.AppContext;
import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.R;
import org.xwiki.android.sync.activities.EditContact.EditContactActivity;
import org.xwiki.android.sync.bean.XWikiUserFull;
import org.xwiki.android.sync.contactdb.BatchOperation;

import java.util.List;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.xwiki.android.sync.contactdb.ContactOperationsKt.toContentProviderOperations;

/**
 * EditContactActivityTest
 */
public class EditContactActivityTest {
    private Uri userUri = null;

    @Rule
    public ActivityTestRule<EditContactActivity> activityRule = new ActivityTestRule<>(
        EditContactActivity.class,
        true,
        false
    );

    private final Account account = new Account("test", Constants.ACCOUNT_TYPE);
    private final String password = "testtest";
    private final XWikiUserFull xWikiUserFull = new XWikiUserFull();

    public EditContactActivityTest() {
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

        Intent intent = new Intent();
        intent.setData(userUri);

        activityRule.launchActivity(intent);
    }

    @After
    public void removeTestAccount() {
        AccountManager.get(AppContext.getInstance()).removeAccount(
            account,
            null,
            null
        );
    }

    @Test
    public void correctlyLoadData() {
        Espresso.onView(
            withId(
                R.id.editContactFirstNameEditText
            )
        ).check(
            matches(
                withText(xWikiUserFull.getFirstName())
            )
        );
        Espresso.onView(
            withId(
                R.id.editContactLastNameEditText
            )
        ).check(
            matches(
                withText(xWikiUserFull.getLastName())
            )
        );
        Espresso.onView(
            withId(
                R.id.editContactPhoneEditText
            )
        ).check(
            matches(
                withText(xWikiUserFull.getPhone())
            )
        );
        Espresso.onView(
            withId(
                R.id.editContactAddressEditText
            )
        ).check(
            matches(
                withText(xWikiUserFull.getAddress())
            )
        );
        Espresso.onView(
            withId(
                R.id.editContactCompanyEditText
            )
        ).check(
            matches(
                withText(xWikiUserFull.getCompany())
            )
        );
        Espresso.onView(
            withId(
                R.id.editContactNoteEditText
            )
        ).check(
            matches(
                withText(xWikiUserFull.getComment())
            )
        );
    }

    //TODO:: add tests for:
    // * changing of contact
    // * Updating contact data on server
    // * updating contact data locally
}