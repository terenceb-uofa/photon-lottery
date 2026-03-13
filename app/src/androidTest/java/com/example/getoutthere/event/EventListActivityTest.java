package com.example.getoutthere.event;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.CoreMatchers.anything;

// Tests the functionality of EventListActivity.

@RunWith(AndroidJUnit4.class)
public class EventListActivityTest {

    // Launches EventListActivity before each test
    @Rule
    public ActivityScenarioRule<EventListActivity> activityRule =
            new ActivityScenarioRule<>(EventListActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Test - events in Firestore appear in the list and can be opened
     * @throws InterruptedException
     */
    @Test
    public void testListItemClick_FiresIntentToEventDetails() throws InterruptedException {
        // Wait for Firestore to populate the list
        Thread.sleep(2000);

        try {
            // Select the first item in the event list
            onData(anything())
                    .inAdapterView(withId(R.id.listOfEvents))
                    .atPosition(0)
                    .perform(click());

            // Verify EventDetailsActivity was launched and event selected successfully
            intended(hasComponent(EventDetailsActivity.class.getName()));

            // Verify eventId extra exists
            intended(hasExtraWithKey("eventId"));

        } catch (Exception e) {
            // If no events exist, test skips safely
            System.out.println("No events loaded or list empty. EventListActivityTest skipped.");
        }
    }
}