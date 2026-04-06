package com.example.getoutthere.organizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.matcher.ViewMatchers.withText;


// This test is AI generated with Anthropics Claude Code, Prompt: "Generate an android test for the following code", Date: Mar 13, 2026

/**
 * Instrumented tests for OrganizerEventDetailsActivity.
 *
 * These tests verify that the organizer event details screen loads,
 * displays event information, and navigates correctly to the edit,
 * QR code, and waitlist screens.
 *
 * Before running these tests, replace TEST_EVENT_ID with a real
 * event ID that exists in Firebase.
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerEventDetailsActivityTest {

    /**
     * Replace this with a real event ID from Firebase before running tests.
     */
    private static final String TEST_EVENT_ID = "staYBy7EYit3QDjyE0rf";

    /**
     * Time to wait for Firebase event data to load into the screen.
     */
    private static final long LOAD_WAIT_MS = 3000;

    /**
     * Launches OrganizerEventDetailsActivity with a fixed event ID.
     */
    @Rule
    public ActivityScenarioRule<OrganizerEventDetailsActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    /**
     * Creates the intent used to launch the activity with the test event ID.
     *
     * @return an intent containing the event ID extra
     */
    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, OrganizerEventDetailsActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Sets up Espresso Intents before each test.
     */
    @Before
    public void setUp() {
        init();
    }

    /**
     * Releases Espresso Intents after each test.
     */
    @After
    public void tearDown() {
        release();
    }

    /**
     * Waits briefly for Firebase data to load into the activity.
     */
    private void waitForEventToLoad() {
        SystemClock.sleep(LOAD_WAIT_MS);
    }

    /**
     * Verifies that the event name view is displayed.
     */
    @Test
    public void eventName_isDisplayed() {
        waitForEventToLoad();

        onView(withId(R.id.nameInput))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that the description text is displayed after the event loads.
     */
    @Test
    public void description_isDisplayed() {
        waitForEventToLoad();

        onView(withId(R.id.descriptionInput))
                .check(matches(withText(containsString("Description:"))));
    }

    /**
     * Verifies that the address text is displayed after the event loads.
     */
    @Test
    public void address_isDisplayed() {
        waitForEventToLoad();

        onView(withId(R.id.addressInput))
                .check(matches(withText(containsString("Address:"))));
    }

    /**
     * Verifies that the start date text is displayed after the event loads.
     */
    @Test
    public void startDate_isDisplayed() {
        waitForEventToLoad();

        onView(withId(R.id.startDateInput))
                .check(matches(withText(containsString("Start Date:"))));
    }

    /**
     * Verifies that the end date text is displayed after the event loads.
     */
    @Test
    public void endDate_isDisplayed() {
        waitForEventToLoad();

        onView(withId(R.id.endDateInput))
                .check(matches(withText(containsString("End Date:"))));
    }

    /**
     * Verifies that the draw date text is displayed after the event loads.
     */
    @Test
    public void drawDate_isDisplayed() {
        waitForEventToLoad();

        onView(withId(R.id.drawDateInput))
                .check(matches(withText(containsString("Draw Date:"))));
    }

    /**
     * Verifies that the registration start text is displayed after the event loads.
     */
    @Test
    public void registrationStart_isDisplayed() {
        waitForEventToLoad();

        onView(withId(R.id.registrationStartInput))
                .check(matches(withText(containsString("Registration Start:"))));
    }

    /**
     * Verifies that the registration end text is displayed after the event loads.
     */
    @Test
    public void registrationEnd_isDisplayed() {
        waitForEventToLoad();

        onView(withId(R.id.registrationEndInput))
                .check(matches(withText(containsString("Registration End:"))));
    }

    /**
     * Verifies that the capacity text is displayed after the event loads.
     */
    @Test
    public void capacity_isDisplayed() {
        waitForEventToLoad();

        onView(withId(R.id.capacityInput))
                .check(matches(withText(containsString("Capacity:"))));
    }

    /**
     * Verifies that the fee text is displayed after the event loads.
     */
    @Test
    public void fee_isDisplayed() {
        waitForEventToLoad();

        onView(withId(R.id.feeInput))
                .check(matches(withText(containsString("Signup Fee:"))));
    }

    /**
     * Verifies that the waitlist limit text is displayed after the event loads.
     */
    @Test
    public void waitlistLimit_isDisplayed() {
        waitForEventToLoad();

        onView(withId(R.id.waitlistLimitInput))
                .check(matches(withText(containsString("Waitlist Limit:"))));
    }

    /**
     * Verifies that the edit button is enabled.
     */
    @Test
    public void editButton_isEnabled() {
        waitForEventToLoad();

        onView(withId(R.id.editButton))
                .check(matches(isEnabled()));
    }

    /**
     * Verifies that the QR code button is enabled.
     */
    @Test
    public void qrCodeButton_isEnabled() {
        waitForEventToLoad();

        onView(withId(R.id.buttonQRCode))
                .perform(scrollTo())
                .check(matches(isEnabled()));
    }

    /**
     * Verifies that the waitlist button is enabled.
     */
    @Test
    public void viewWaitlistButton_isEnabled() {
        waitForEventToLoad();

        onView(withId(R.id.rvWaitlist))
                .perform(scrollTo())
                .check(matches(isEnabled()));
    }

    /**
     * Verifies that pressing the back button closes the activity.
     */
    @Test
    public void backButton_finishesActivity() {
        waitForEventToLoad();

        onView(withId(R.id.backButton)).perform(click());

        assertEquals(Lifecycle.State.DESTROYED, activityRule.getScenario().getState());
    }

    /**
     * Verifies that pressing the edit button opens OrganizerEditEventActivity
     * with the correct event ID.
     */
    @Test
    public void editButton_opensEditEventActivity() {
        waitForEventToLoad();

        onView(withId(R.id.editButton)).perform(click());

        intended(allOf(
                hasComponent(OrganizerEditEventActivity.class.getName()),
                hasExtra("eventId", TEST_EVENT_ID)
        ));
    }

    /**
     * Verifies that pressing the QR code button opens EventQrCodeActivity
     * with the correct event ID.
     */
    @Test
    public void qrCodeButton_opensQrCodeActivity() {
        waitForEventToLoad();

        onView(withId(R.id.buttonQRCode)).perform(scrollTo(), click());

        intended(allOf(
                hasComponent(EventQrCodeActivity.class.getName()),
                hasExtra("eventId", TEST_EVENT_ID)
        ));
    }

    /**
     * Verifies that pressing the waitlist button opens OrganizerWaitlistActivity
     * with the correct event ID.
     */
    @Test
    public void waitlistButton_opensWaitlistActivity() {
        waitForEventToLoad();

        onView(withId(R.id.rvWaitlist)).perform(scrollTo(), click());

        intended(allOf(
                hasComponent(OrganizerWaitlistActivity.class.getName()),
                hasExtra("eventId", TEST_EVENT_ID)
        ));
    }
}