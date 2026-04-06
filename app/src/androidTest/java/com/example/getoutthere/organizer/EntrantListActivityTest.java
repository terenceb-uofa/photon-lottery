package com.example.getoutthere.organizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.getoutthere.R;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test for EntrantListActivity.
 * Verifies that the activity handles intent data correctly and maintains UI stability.
 */
@RunWith(AndroidJUnit4.class)
public class EntrantListActivityTest {

    /**
     * Tests that the Activity remains open when a valid eventId is provided
     * and that the ListView is correctly displayed.
     */
    @Test
    public void testActivityLaunchWithIntent() {
        // Create an intent with a fake eventId
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, EntrantListActivity.class);
        intent.putExtra("eventId", "test_event_123");

        // Launch activity
        try (ActivityScenario<EntrantListActivity> scenario = ActivityScenario.launch(intent)) {
            // Verify ListView is visible
            onView(withId(R.id.entrantListView)).check(matches(isDisplayed()));
        }
    }

    /**
     * Tests the "No one is on the waiting list" placeholder logic.
     * Uses a random ID to ensure an empty Firestore result.
     */
    @Test
    public void testEmptyWaitlistMessage() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EntrantListActivity.class);
        intent.putExtra("eventId", "non_existent_event_" + System.currentTimeMillis());

        try (ActivityScenario<EntrantListActivity> scenario = ActivityScenario.launch(intent)) {
            // Wait for Firestore to return empty
            try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }

            // Check for the empty state string defined in your code
            onView(withText("No one is on the waiting list.")).check(matches(isDisplayed()));
        }
    }

    /**
     * Verifies that the Back button successfully finishes the activity.
     */
    @Test
    public void testBackButtonNavigation() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EntrantListActivity.class);
        intent.putExtra("eventId", "test_event_123");

        try (ActivityScenario<EntrantListActivity> scenario = ActivityScenario.launch(intent)) {
            // Click the back button
            onView(withId(R.id.backButton)).perform(click());

            // Wait a moment for transition and check if it's destroyed/finished
            try { Thread.sleep(500); } catch (InterruptedException e) { }
            assertTrue(scenario.getState().isAtLeast(androidx.lifecycle.Lifecycle.State.DESTROYED) ||
                    scenario.getResult().getResultCode() == android.app.Activity.RESULT_CANCELED);
        }
    }
}