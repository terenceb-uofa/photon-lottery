package com.example.getoutthere.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static com.example.getoutthere.TestUtils.withIndex;


import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.getoutthere.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test to verify proper UI functionality of profile management
 */

@RunWith(AndroidJUnit4.class)
public class ManageProfilesActivityTest {

    @Rule
    public ActivityScenarioRule<ManageProfilesActivity> activityRule =
            new ActivityScenarioRule<>(ManageProfilesActivity.class);

    @Test
    public void testProfileDeleteDialogIntent() {
        // Wait for Firestore data (delay for networking)
        try { Thread.sleep(2000); } catch (InterruptedException e) { }

        // Click first "DELETE" button
        onView(withIndex(withText("DELETE"), 0)).perform(click());

        // Verify correct UI display for Profiles specifically (organizer status doesn't matter)
        onView(withText("Delete Profile")).check(matches(isDisplayed()));

        // Verify the UI state remains preserved for the activity
        onView(withText("Cancel")).perform(click());
        onView(withId(R.id.profilesContainer)).check(matches(isDisplayed()));
    }
}