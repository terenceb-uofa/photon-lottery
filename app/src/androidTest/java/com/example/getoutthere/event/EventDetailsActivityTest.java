package com.example.getoutthere.event;

import android.content.Intent;
import android.widget.Button;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

// Tests the functionality of EventDetailsActivityTest

@RunWith(AndroidJUnit4.class)
public class EventDetailsActivityTest {

    /**
     * Test - EventDetailsActivity launches. UI and event info displayed.
     */
    @Test
    public void testActivityLaunchesAndDisplaysViews() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailsActivity.class
        );
        intent.putExtra("eventId", "testEvent123");

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            // Check important UI components are displayed
            Espresso.onView(ViewMatchers.withId(R.id.EventName)).check(ViewAssertions.matches(isDisplayed()));
            Espresso.onView(ViewMatchers.withId(R.id.EventAddress)).check(ViewAssertions.matches(isDisplayed()));
            Espresso.onView(ViewMatchers.withId(R.id.EventDateRange)).check(ViewAssertions.matches(isDisplayed()));
            Espresso.onView(ViewMatchers.withId(R.id.btnToggleWaitingList)).check(ViewAssertions.matches(isDisplayed()));
        }
    }

    /**
     * Test - can click Join/Leave Waiting List button.
     */
    @Test
    public void testJoinWaitingListButtonClick() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailsActivity.class
        );
        intent.putExtra("eventId", "testEvent123");

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            Espresso.onView(ViewMatchers.withId(R.id.btnToggleWaitingList))
                    .perform(ViewActions.click());
        }
    }

    /**
     * Test - user can't join the waiting list if the event is full
     */
    @Test
    public void testCantJoinFullEvent() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailsActivity.class
        );
        intent.putExtra("eventId", "testEvent123");

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                // Makes the event full
                Event fullEvent = new Event();
                fullEvent.setCapacity(5);
                fullEvent.setCurrentWaitlistCount(5);
                activity.event = fullEvent;

                // Update UI
                activity.updateSpotsUI();

                // User clicks join button. Should not join.
                Button joinBtn = activity.findViewById(R.id.btnToggleWaitingList);
                joinBtn.performClick();
                assertFalse(activity.isOnWaitingList);
            });
        }
    }

    /**
     * Test - back button closes the activity
     */
    @Test
    public void testBackButtonClosesActivity() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailsActivity.class
        );
        intent.putExtra("eventId", "testEvent123");

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            Espresso.onView(ViewMatchers.withId(R.id.EventDetailsBackButton))
                    .perform(ViewActions.click());
        }
    }
}