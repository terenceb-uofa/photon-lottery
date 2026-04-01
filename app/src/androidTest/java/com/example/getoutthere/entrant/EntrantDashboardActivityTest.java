package com.example.getoutthere.entrant;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;
import com.example.getoutthere.event.EventListActivity;
import com.example.getoutthere.organizer.OrganizerEventListActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Test to verify navigation intents fired from the EntrantDashboardActivity.
 */
@RunWith(AndroidJUnit4.class)
public class EntrantDashboardActivityTest {

    @Rule
    public ActivityScenarioRule<EntrantDashboardActivity> activityRule =
            new ActivityScenarioRule<>(EntrantDashboardActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testEventListButton_FiresIntentToEventListActivity() {
        // Find button3 and click it
        onView(withId(R.id.button3)).perform(click());

        // Verify it attempts to open EventListActivity
        intended(hasComponent(EventListActivity.class.getName()));
    }

}