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
 * Instrumented test to verify proper UI functionality of organizer management
 */
@RunWith(AndroidJUnit4.class)

public class ManageOrganizersActivityTest {

    @Rule
    public ActivityScenarioRule<ManageOrganizersActivity> activityRule =
            new ActivityScenarioRule<>(ManageOrganizersActivity.class);

    @Test
    public void testOrganizerDeleteDialogIntent() {
        // Wait for Firestore data (delay for networking)
        try { Thread.sleep(2000); } catch (InterruptedException e) { }

        // Click first "DELETE" button
        onView(withIndex(withText("BAN"), 0)).perform(click());

        // Verify correct UI display for Organizers specifically
        onView(withText("Ban Organizer")).check(matches(isDisplayed()));

        // Verify the UI state remains preserved for the activity
        onView(withText("Cancel")).perform(click());
        onView(withId(R.id.organizersContainer)).check(matches(isDisplayed()));
    }

}