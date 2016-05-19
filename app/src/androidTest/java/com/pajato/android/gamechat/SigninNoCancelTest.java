package com.pajato.android.gamechat;

import android.app.Application;
import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.test.SigninNoCancelTestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Test that signin works for the supported providers such that the designated providers and credentials succeed.
 */
@RunWith(AndroidJUnit4.class)
public class SigninNoCancelTest {

    /** Set up the activity rule such that the application under test starts immediately. */
    @Rule public ActivityTestRule mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    /** Register the token idling resource. */
    @Before public void registerTokenIdlingResource() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        idlingResource = new TokenIdlingResource(instrumentation.getTargetContext());
        Espresso.registerIdlingResources(idlingResource);
    }

    // Unregister the token idling resource.
    @After public void unregisterIntentServiceIdlingResource() {
        Espresso.unregisterIdlingResources(idlingResource);
    }

    @Test public void testNothing() {
        // Used to generate the initial test coverage report.
    }

    //@Test
    public void testSigninSuccess() {
        // Verify that the Google signin button can be clicked.
        onView(withText("Sign in with Google")).check(matches(isDisplayed())).perform(click());

        // It can.  Select the account provided by the environment in the "Choose an account" dialog that is presented
        // by the system (via the AccountManager --- I strongly suspect) which allows the User to choose an on-device
        // account or to add an account.
        String temp = System.getenv("GAMECHAT_TEST_ACCOUNT_NAME");
        String accountAddress = temp != null ? temp : "GameChatTester@gmail.com";
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        testAndClick(device, accountAddress);
        testAndClick(device, "OK");

        // Look for the Toast message to signify success.
        String toastString = "You are successfully signed in to GameChat";
        testToast(toastString);
        onView(withText("Rooms")).check(matches(isEnabled()));
    }

    /** Test that a signin failure works properly. */
    //@Test fails with the messsage: java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
    public void testSigninFailure() {
        // Now test a signin failure.  First, ensure that the activity is a MainActivity.
        Activity activity = mActivityTestRule.getActivity();
        if (activity instanceof MainActivity) {
            // The activity is a correct one.  It will be used subsequently to invoke the error handler directly.  But
            // first, use the back button to remove the provider chooser.  Then cause the Toast message to be shown and
            // verify it is displayed.
            MainActivity mainActivity = (MainActivity) activity;
            ///onView(withText("Sign in with Google")).check(matches(isDisplayed()));
            UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            //device.pressBack();
            onView(withText("Rooms")).check(matches(isDisplayed()));
            mainActivity.onSignInFailed();
            testToast("Sign in failed");
        } else {
            // Report an incorrect main activity class as an error.
            throw new AssertionError(String.format("The activity {%s} is not a MainActivity, as expected.", activity.getClass().getSimpleName()));
        }
    }

    /** Test that a signin works properly. */
    @Test public void testSigninSuccess() {
        // Now test that signin success has occurred during the token idling stabilization.
        Activity activity = mActivityTestRule.getActivity();
        if (activity instanceof MainActivity) {
            // The activity is a correct one.  It will be used subsequently to invoke the error handler directly.  But
            // first, use the back button to remove the provider chooser.  Then cause the Toast message to be shown and
            // verify it is displayed.
            MainActivity mainActivity = (MainActivity) activity;
            assertTrue("There is no account!", mainActivity.hasAccount)///onView(withText("Sign in with Google")).check(matches(isDisplayed()));
            UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            //device.pressBack();
            onView(withText("Rooms")).check(matches(isDisplayed()));
            mainActivity.onSignInFailed();
            testToast("Sign in failed");
        } else {
            // Report an incorrect main activity class as an error.
            throw new AssertionError(String.format("The activity {%s} is not a MainActivity, as expected.", activity.getClass().getSimpleName()));
        }
    }

    // Private instance methods

    /** Test for a the presence of a button with the given text and click on it, failing if it is not found. */
    private void testAndClick(final UiDevice device, final String text) {
        // Determine if the button with the given text exists.
        UiObject button = device.findObject(new UiSelector().text(text));
        if (!button.exists()) {
            // The button does not exist.  Flag a failure.
            throw new AssertionError(String.format("Button with text {%s} has not been found.", text));
        }

        // The button does exist.  Click on it and report a failure as an error.
        try {
            button.click();
        } catch (UiObjectNotFoundException exc) {
            throw new AssertionError(String.format("Button {%s} could not be clicked.", button));
        }
    }

    /** Test that a toast message with given text is in the foreground. */
    private void testToast(final String text) {
        onView(withText(text)).inRoot(withDecorView(not(is(mActivityTestRule.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

}
