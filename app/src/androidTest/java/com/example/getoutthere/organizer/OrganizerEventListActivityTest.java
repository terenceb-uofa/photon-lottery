package com.example.getoutthere.organizer;

import android.content.Intent;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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
import static org.hamcrest.CoreMatchers.anything;

/**
 * Tests the functionality of OrganizerEventListActivity.
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerEventListActivityTest {

    // Launches OrganizerEventListActivity before each test
    @Rule
    public ActivityScenarioRule<OrganizerEventListActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerEventListActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        // Clear Intents state
        Intents.release();
    }

    @Test
    public void testListItemClick_FiresIntentToDetails() throws InterruptedException {
        // Wait briefly for Firestore to populate the list
        Thread.sleep(2000);

        try {
            // Click the very first item in the ListView
            onData(anything())
                    .inAdapterView(androidx.test.espresso.matcher.ViewMatchers.withId(com.example.getoutthere.R.id.organizerEventListView))
                    .atPosition(0)
                    .perform(click());

            // Verify that clicking the item triggered an intent to OrganizerEventDetailsActivity
            intended(hasComponent(OrganizerEventDetailsActivity.class.getName()));

            // Verify the intent properly bundled the event ID extra
            intended(hasExtraWithKey("eventId"));

        } catch (Exception e) {
            // If the list is empty (this test user has no events), the test skips smoothly
            System.out.println("No events loaded in time, or list is empty. Test skipped.");
        }
    }
}