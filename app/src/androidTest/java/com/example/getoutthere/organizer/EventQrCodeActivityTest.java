package com.example.getoutthere.organizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

// This test is AI generated with Anthropics Claude Code, Prompt: "Generate an android test for the following code", Date: Mar 13, 2026



/**
 * Instrumented tests for EventQrCodeActivity.
 *
 * These tests verify that the QR code screen loads correctly,
 * shows its main views, and allows the user to return using
 * the back button.
 *
 * Before running these tests, replace TEST_EVENT_ID with a real
 * event ID from Firebase that has QR code content.
 */
@RunWith(AndroidJUnit4.class)
public class EventQrCodeActivityTest {

    /**
     * Replace this with a real event ID from Firebase before running tests.
     * The event should have a non-empty qrCodeContent value.
     */
    private static final String TEST_EVENT_ID = "staYBy7EYit3QDjyE0rf";

    /**
     * Time to wait for Firebase event data and QR code generation.
     */
    private static final long LOAD_WAIT_MS = 3000;

    /**
     * Launches EventQrCodeActivity with a fixed event ID.
     */
    @Rule
    public ActivityScenarioRule<EventQrCodeActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    /**
     * Creates the intent used to launch the activity with the test event ID.
     *
     * @return an intent containing the event ID extra
     */
    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, EventQrCodeActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Waits briefly for Firebase data to load and the QR code to generate.
     */
    private void waitForQrCodeToLoad() {
        SystemClock.sleep(LOAD_WAIT_MS);
    }

    /**
     * Verifies that the QR code image view is displayed.
     */
    @Test
    public void qrCodeImage_isDisplayed() {
        waitForQrCodeToLoad();

        onView(withId(R.id.qrCodeImage))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that the back button is displayed.
     */
    @Test
    public void backButton_isDisplayed() {
        onView(withId(R.id.backButton))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that the export button is displayed.
     */
    @Test
    public void exportButton_isDisplayed() {
        onView(withId(R.id.exportButton))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that the export button is enabled after the QR code is loaded.
     */
    @Test
    public void exportButton_isEnabledAfterLoad() {
        waitForQrCodeToLoad();

        onView(withId(R.id.exportButton))
                .check(matches(isEnabled()));
    }

    /**
     * Verifies that pressing the back button closes the activity.
     */
    @Test
    public void backButton_finishesActivity() {
        onView(withId(R.id.backButton)).perform(click());

        assertEquals(Lifecycle.State.DESTROYED, activityRule.getScenario().getState());
    }
}