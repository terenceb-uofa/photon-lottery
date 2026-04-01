package com.example.getoutthere.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

// The tests in this file have been generated through Claude AI

@RunWith(AndroidJUnit4.class)
public class AdminDashboardActivityTest {

    @Before
    public void setUp() {
        Intents.init();
        ActivityScenario.launch(AdminDashboardActivity.class);
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Clicking button1 should navigate to ManageEventsActivity.
     */
    @Test
    public void testButton1_navigatesToManageEventsActivity() {
        onView(withId(R.id.button1)).perform(click());
        intended(hasComponent(ManageEventsActivity.class.getName()));
    }

    /**
     * Clicking button2 should navigate to ManageProfilesActivity.
     */
    @Test
    public void testButton2_navigatesToManageProfilesActivity() {
        onView(withId(R.id.button2)).perform(click());
        intended(hasComponent(ManageProfilesActivity.class.getName()));
    }

    /**
     * Clicking button3 should navigate to ManageImagesActivity.
     */
    @Test
    public void testButton3_navigatesToManageImagesActivity() {
        onView(withId(R.id.button3)).perform(click());
        intended(hasComponent(ManageImagesActivity.class.getName()));
    }

    /**
     * Clicking button4 should navigate to ManageOrganizersActivity.
     */
    @Test
    public void testButton4_navigatesToManageOrganizersActivity() {
        onView(withId(R.id.button4)).perform(click());
        intended(hasComponent(ManageOrganizersActivity.class.getName()));
    }

    /**
     * Clicking button5 should navigate to NotificationLogsActivity.
     */
    @Test
    public void testButton5_navigatesToNotificationLogsActivity() {
        onView(withId(R.id.button5)).perform(click());
        intended(hasComponent(NotificationLogsActivity.class.getName()));
    }
}