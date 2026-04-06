package com.example.getoutthere.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static com.example.getoutthere.TestUtils.withIndex;

import android.view.View;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test to verify the logic and data of deleting events from the database
 */
@RunWith(AndroidJUnit4.class)
public class ManageEventsActivityTest {

    @Rule
    public ActivityScenarioRule<ManageEventsActivity> activityRule =
            new ActivityScenarioRule<>(ManageEventsActivity.class);

    @Test
    public void testEventDeleteDialogSafetyIntent() {
        // Wait for Firestore data (delay for networking)
        try { Thread.sleep(2000); } catch (InterruptedException e) { }

        // Click the first "DELETE" button in the event list
        onView(withIndex(withContentDescription("Delete Action"), 0)).perform(click());
        // Verify that the Dialog for Event deletion specifically appears
        onView(withText("Delete Event")).check(matches(isDisplayed()));

        // Test that the Intent to Cancel is handled correctly
        onView(withText("Cancel")).perform(click());

        // Verify that we are back on the 'All Events' screen
        onView(withText("All Events")).check(matches(isDisplayed()));
    }

}