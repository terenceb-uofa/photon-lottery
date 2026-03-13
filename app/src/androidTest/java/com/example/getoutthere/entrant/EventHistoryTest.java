package com.example.getoutthere.entrant;

import android.widget.ListView;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * Instrumented test to verify the logic and data fetching of the EventHistory screen.
 */
@RunWith(AndroidJUnit4.class)
public class EventHistoryTest {

    // Launches the EventHistory Activity before each test
    @Rule
    public ActivityScenarioRule<EventHistory> activityRule =
            new ActivityScenarioRule<>(EventHistory.class);

    @Test
    public void testHistoryListPopulatesFromFirebase() throws InterruptedException {
        // Wait for 3 seconds to give Firebase time to fetch the data
        Thread.sleep(3000);

        activityRule.getScenario().onActivity(activity -> {

            // Find the ListView on the screen
            ListView listView = activity.findViewById(R.id.historyListView);

            // Get the number of items currently loaded into the list
            int itemCount = listView.getAdapter().getCount();

            // Verify the list isn't empty.
            assertTrue("The history list should have populated with at least one item", itemCount > 0);
        });
    }
}