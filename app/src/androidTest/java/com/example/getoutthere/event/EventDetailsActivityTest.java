package com.example.getoutthere.event;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class EventDetailsActivityTest {

    /**
     * Test that the activity launches and all key UI elements are displayed.
     */
    @Test
    public void testActivityLaunchesAndDisplaysViews() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("eventId", "testEvent123");

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            // Check key UI elements
            Espresso.onView(ViewMatchers.withId(R.id.EventName)).check(ViewAssertions.matches(isDisplayed()));
            Espresso.onView(ViewMatchers.withId(R.id.EventAddress)).check(ViewAssertions.matches(isDisplayed()));
            Espresso.onView(ViewMatchers.withId(R.id.EventDateRange)).check(ViewAssertions.matches(isDisplayed()));
            Espresso.onView(ViewMatchers.withId(R.id.btnToggleWaitingList)).check(ViewAssertions.matches(isDisplayed()));
            Espresso.onView(ViewMatchers.withId(R.id.btnViewComments)).check(ViewAssertions.matches(isDisplayed()));
        }
    }

    /**
     * Test clicking Join/Leave Waiting List button.
     */
    @Test
    public void testJoinLeaveWaitingListButtonClick() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("eventId", "testEvent123");

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            // Click to join
            Espresso.onView(ViewMatchers.withId(R.id.btnToggleWaitingList))
                    .perform(ViewActions.click());

            // Click again to leave
            Espresso.onView(ViewMatchers.withId(R.id.btnToggleWaitingList))
                    .perform(ViewActions.click());
        }
    }

    /**
     * Test that user cannot join if the waitlist is full.
     */
    @Test
    public void testCantJoinFullEvent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("eventId", "testEvent123");

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                // Simulate full event
                Event fullEvent = new Event();
                fullEvent.setWaitlistLimit(2);
                fullEvent.setCurrentWaitlistCount(2);
                activity.event = fullEvent;

                activity.updateSpotsUI();
                activity.updateToggleButton();
            });

            // Attempt to join waitlist
            Espresso.onView(ViewMatchers.withId(R.id.btnToggleWaitingList))
                    .perform(ViewActions.click());

            // Verify the button text did not change (still says Join Waitlist)
            Espresso.onView(ViewMatchers.withId(R.id.btnToggleWaitingList))
                    .check(ViewAssertions.matches(ViewMatchers.withText("Join Waitlist")));
        }
    }

    /**
     * Test that the comments button opens the dialog.
     */
    @Test
    public void testViewCommentsDialog() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("eventId", "testEvent123");

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            Espresso.onView(ViewMatchers.withId(R.id.backButton))
                    .perform(ViewActions.click());

            // Verify the dialog title is displayed
            Espresso.onView(ViewMatchers.withText("Event Comments"))
                    .check(ViewAssertions.matches(isDisplayed()));
        }
    }
}