package org.xwiki.android.sync.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xwiki.android.sync.AppContext;
import org.xwiki.android.sync.Constants;
import org.xwiki.android.sync.R;
import org.xwiki.android.sync.rest.BaseApiManager;
import org.xwiki.android.sync.rest.XWikiHttp;
import org.xwiki.android.sync.rest.XWikiServices;

import rx.Observable;

import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * AuthenticatorActivityTest
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ XWikiHttp.class })
public class AuthenticatorActivityTest {
    public static final Integer serverPort = 9001;
    public static final String serverIp = "http://localhost:" + serverPort + "/xwiki";
    public static final String username = "test";
    public static final String password = "testtest";

    private BaseApiManager baseApiManager = Mockito.mock(BaseApiManager.class);
    private XWikiServices xWikiServices = Mockito.mock(XWikiServices.class);
    private Observable<String> loginObservable = Mockito.mock(Observable.class);

    @Before
    public void prepareStatic() {
        PowerMockito.mockStatic(XWikiHttp.class);

        PowerMockito.when(
            XWikiHttp.login(
                anyString(),
                anyString()
            )
        ).thenReturn(
            loginObservable
        );
    }

    @Rule
    public ActivityTestRule<AuthenticatorActivity> mActivityRule = new ActivityTestRule<>(
        AuthenticatorActivity.class
    );

    @Test
    public void wrongServerIpShowError() {
        ViewInteraction serverTextViewInteraction = Espresso.onView(
            withId(R.id.accountServer)
        ).perform(
            clearText(),
            typeText("127.0.0.1:9001")
        );
        Espresso.onView(
            withText(R.string.next)
        ).perform(
            click()
        );
        serverTextViewInteraction.check(
            matches(
                hasErrorText(
                    mActivityRule.getActivity().getString(R.string.error_invalid_server)
                )
            )
        );
    }

    @Test
    public void correctServerIpEmptyUsernameAndPasswordShowErrorEmptyField() {
        typeCorrectServerIdAndOpenSignIn();

        ViewInteraction usernameViewInteraction = Espresso.onView(
            withId(R.id.accountName)
        ).perform(
            clearText()
        );


        ViewInteraction passwordViewInteraction = Espresso.onView(
            withId(R.id.accountPassword)
        ).perform(
            clearText()
        );

        Espresso.onView(
            withId(R.id.signInButton)
        ).perform(
            click()
        );

        usernameViewInteraction.check(
            matches(
                hasErrorText(
                    mActivityRule.getActivity().getString(R.string.error_field_required)
                )
            )
        );

        passwordViewInteraction.check(
            matches(
                hasErrorText(
                    mActivityRule.getActivity().getString(R.string.error_password_short)
                )
            )
        );
    }

    @Test
    public void correctServerIpOpenSignInViewFlipper() {
        typeCorrectServerIdAndOpenSignIn();

        Espresso.onView(
            withId(R.id.signInButton)
        ).check(
            matches(
                withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        );
    }

    @Test
    public void correctServerIpAndUsernameAndPasswordCloseActivity() {
        typeCorrectServerIdAndOpenSignIn();

        Espresso.onView(
            withId(R.id.accountName)
        ).perform(
            clearText(),
            typeText(
                username
            )
        );
        Espresso.onView(
            withId(R.id.accountPassword)
        ).perform(
            clearText(),
            typeText(
                password
            )
        );

        Espresso.onView(
            withId(R.id.signInButton)
        ).perform(
            click()
        );

        PowerMockito.verifyStatic(XWikiHttp.class);

        XWikiHttp.login(
            username,
            password
        );

    }

    @Before
    @After
    public void clearAccount() {
        AccountManager manager = AccountManager.get(AppContext.getInstance());

        Account account = null;
        for (Account current : manager.getAccountsByType(Constants.ACCOUNT_TYPE)) {
            if (current.name.equals(username)) {
                account = current;
                break;
            }
        }

        if (account != null) {
            manager.removeAccountExplicitly(
                account
            );
        }
    }

    private void typeCorrectServerIdAndOpenSignIn() {
        Espresso.onView(
            withId(R.id.accountServer)
        ).perform(
            clearText(),
            typeText(serverIp)
        );
        Espresso.onView(
            withText(R.string.next)
        ).perform(
            click()
        );
    }
}