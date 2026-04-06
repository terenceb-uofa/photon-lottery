package com.example.getoutthere.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NotificationLogsActivityTest {

    @Rule
    public ActivityScenarioRule<NotificationLogsActivity> activityRule =
            new ActivityScenarioRule<>(NotificationLogsActivity.class);

    @Test
    public void testNotificationLogsTitleIsDisplayed() {
        onView(withId(R.id.NotificationLogsTitle))
                .check(matches(isDisplayed()))
                .check(matches(withText("Notification Log")));
    }

    @Test
    public void testRecyclerViewIsDisplayed() {
        onView(withId(R.id.rvNotificationLogs))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testBackButtonIsDisplayed() {
        onView(withId(R.id.NotificationLogBackButton))
                .check(matches(isDisplayed()));
    }
}