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
 * Instrumented test to verify proper UI functionality of image management
 */

@RunWith(AndroidJUnit4.class)
public class ManageImagesActivityTest {

    @Rule
    public ActivityScenarioRule<ManageImagesActivity> activityRule =
            new ActivityScenarioRule<>(ManageImagesActivity.class);

    @Test
    public void testDeleteDialogAppearsOnImageClick() {
        // Wait for Firestore data (delay for networking)
        try { Thread.sleep(2000); } catch (InterruptedException e) { }

        // Select the FIRST "DELETE" button found in the list (index 0)
        onView(withIndex(withText("DELETE"), 0)).perform(click());

        // Check to see if the correct dialog title is displayed
        onView(withText("Delete Event Image")).check(matches(isDisplayed()));

        // Verify that clicking Cancel returns to the list
        onView(withText("Cancel")).perform(click());
        onView(withId(R.id.imagesContainer)).check(matches(isDisplayed()));
    }


}