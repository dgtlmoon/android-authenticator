package org.xwiki.android.sync.activities;


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
import org.xwiki.android.sync.R;
import org.xwiki.android.sync.activities.EditContact.EditContactActivity;

/**
 * EditContactActivityTest
 */
public class EditContactActivityTest {
    private Uri userUri = null;

    @Rule
    public ActivityTestRule<EditContactActivity> mActivityRule = new ActivityTestRule<>(
        EditContactActivity.class,
        true,
        false
    );

    @Before
    public void addTestContact() {
        Intent intent = new Intent();
        intent.setData(userUri);

        mActivityRule.launchActivity(intent);
    }

    @After
    public void removeTestContact() {

    }

    @Test
    public void editActivityInstantiated() {
        Espresso.onView(
            ViewMatchers.withId(
                R.id.editContactSaveButton
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        );
    }
}