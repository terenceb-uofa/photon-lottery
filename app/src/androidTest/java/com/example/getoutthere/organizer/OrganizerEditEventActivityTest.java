package com.example.getoutthere.organizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
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
 * Instrumented tests for OrganizerEditEventActivity.
 *
 * These tests verify that the edit event screen loads correctly,
 * existing event data appears, and validation works when fields
 * are cleared or changed.
 *
 * Before running these tests, replace TEST_EVENT_ID with a real
 * event ID that exists in Firebase.
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerEditEventActivityTest {

    /**
     * Replace this with a real event ID from Firebase before running tests.
     */
    private static final String TEST_EVENT_ID = "staYBy7EYit3QDjyE0rf";


    /**
     * Time to wait for Firebase event data to load into the screen.
     */
    private static final long LOAD_WAIT_MS = 3000;

    /**
     * Launches OrganizerEditEventActivity with a fixed event ID.
     */
    @Rule
    public ActivityScenarioRule<OrganizerEditEventActivity> activityRule =
            new ActivityScenarioRule<>(createIntent());

    /**
     * Creates the intent used to launch the activity with the test event ID.
     *
     * @return an intent containing the event ID extra
     */
    private static Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, OrganizerEditEventActivity.class);
        intent.putExtra("eventId", TEST_EVENT_ID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Waits briefly for Firebase data to load into the activity.
     */
    private void waitForEventToLoad() {
        SystemClock.sleep(LOAD_WAIT_MS);
    }

    /**
     * Clicks the Save Changes button after scrolling to it.
     */
    private void clickSaveChangesButton() {
        onView(withId(R.id.createEventButton)).perform(scrollTo(), click());
    }

    /**
     * Verifies that the screen title is displayed correctly.
     */
    @Test
    public void screenTitle_isEditEvent() {
        waitForEventToLoad();

        onView(withId(R.id.screenTitle))
                .check(matches(withText("Edit Event")));
    }

    /**
     * Verifies that the save button text is displayed correctly.
     */
    @Test
    public void saveButtonText_isSaveChanges() {
        waitForEventToLoad();

        onView(withId(R.id.createEventButton))
                .perform(scrollTo())
                .check(matches(withText("Save Changes")));
    }

    /**
     * Verifies that the upload poster button is enabled.
     */
    @Test
    public void uploadPosterButton_isEnabled() {
        waitForEventToLoad();

        onView(withId(R.id.uploadPosterButton))
                .check(matches(isEnabled()));
    }

    /**
     * Verifies that the save changes button is enabled.
     */
    @Test
    public void saveChangesButton_isEnabled() {
        waitForEventToLoad();

        onView(withId(R.id.createEventButton))
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
     * Verifies that an error is shown when the event name is cleared.
     */
    @Test
    public void saveChanges_emptyName_showsError() {
        waitForEventToLoad();

        onView(withId(R.id.nameInput)).perform(replaceText(""));
        clickSaveChangesButton();

        onView(withId(R.id.nameInput))
                .check(matches(hasErrorText("Event name is required")));
    }

    /**
     * Verifies that an error is shown when the description is cleared.
     */
    @Test
    public void saveChanges_emptyDescription_showsError() {
        waitForEventToLoad();

        onView(withId(R.id.descriptionInput)).perform(scrollTo(), replaceText(""));
        clickSaveChangesButton();

        onView(withId(R.id.descriptionInput))
                .check(matches(hasErrorText("Description is required")));
    }

    /**
     * Verifies that an error is shown when the address is cleared.
     */
    @Test
    public void saveChanges_emptyAddress_showsError() {
        waitForEventToLoad();

        onView(withId(R.id.addressInput)).perform(scrollTo(), replaceText(""));
        clickSaveChangesButton();

        onView(withId(R.id.addressInput))
                .check(matches(hasErrorText("Address is required")));
    }

    /**
     * Verifies that an error is shown when the capacity is cleared.
     */
    @Test
    public void saveChanges_emptyCapacity_showsError() {
        waitForEventToLoad();

        onView(withId(R.id.capacityInput)).perform(scrollTo(), replaceText(""));
        clickSaveChangesButton();

        onView(withId(R.id.capacityInput))
                .check(matches(hasErrorText("Capacity is required")));
    }

    /**
     * Verifies that an error is shown when the fee is cleared.
     */
    @Test
    public void saveChanges_emptyFee_showsError() {
        waitForEventToLoad();

        onView(withId(R.id.feeInput)).perform(scrollTo(), replaceText(""));
        clickSaveChangesButton();

        onView(withId(R.id.feeInput))
                .check(matches(hasErrorText("Fee is required")));
    }

    /**
     * Verifies that an error is shown when the capacity is zero.
     */
    @Test
    public void saveChanges_zeroCapacity_showsError() {
        waitForEventToLoad();

        onView(withId(R.id.capacityInput)).perform(scrollTo(), clearText(), replaceText("0"));
        clickSaveChangesButton();

        onView(withId(R.id.capacityInput))
                .check(matches(hasErrorText("Capacity must be at least 1")));
    }

    /**
     * Verifies that an error is shown when the capacity is not a valid whole number.
     */
    @Test
    public void saveChanges_nonNumericCapacity_showsError() {
        waitForEventToLoad();

        onView(withId(R.id.capacityInput)).perform(scrollTo(), clearText(), replaceText("abc"));
        clickSaveChangesButton();

        onView(withId(R.id.capacityInput))
                .check(matches(hasErrorText("Enter a valid whole number")));
    }

    /**
     * Verifies that an error is shown when the fee is negative.
     */
    @Test
    public void saveChanges_negativeFee_showsError() {
        waitForEventToLoad();

        onView(withId(R.id.feeInput)).perform(scrollTo(), clearText(), replaceText("-5"));
        clickSaveChangesButton();

        onView(withId(R.id.feeInput))
                .check(matches(hasErrorText("Fee cannot be negative")));
    }

    /**
     * Verifies that an error is shown when the fee is not numeric.
     */
    @Test
    public void saveChanges_nonNumericFee_showsError() {
        waitForEventToLoad();

        onView(withId(R.id.feeInput)).perform(scrollTo(), clearText(), replaceText("abc"));
        clickSaveChangesButton();

        onView(withId(R.id.feeInput))
                .check(matches(hasErrorText("Enter a valid fee")));
    }

    /**
     * Verifies that an error is shown when the waitlist limit is zero.
     */
    @Test
    public void saveChanges_zeroWaitlistLimit_showsError() {
        waitForEventToLoad();

        onView(withId(R.id.waitlistLimitInput)).perform(scrollTo(), clearText(), replaceText("0"));
        clickSaveChangesButton();

        onView(withId(R.id.waitlistLimitInput))
                .check(matches(hasErrorText("Waitlist limit must be at least 1")));
    }

    /**
     * Verifies that an error is shown when the waitlist limit is not a valid whole number.
     */
    @Test
    public void saveChanges_nonNumericWaitlistLimit_showsError() {
        waitForEventToLoad();

        onView(withId(R.id.waitlistLimitInput)).perform(scrollTo(), clearText(), replaceText("abc"));
        clickSaveChangesButton();

        onView(withId(R.id.waitlistLimitInput))
                .check(matches(hasErrorText("Enter a valid whole number")));
    }
}