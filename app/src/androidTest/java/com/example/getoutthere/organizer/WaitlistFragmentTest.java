package com.example.getoutthere.organizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.os.Bundle;
import android.os.SystemClock;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getoutthere.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


// This test is AI generated


/**
 * Instrumented tests for WaitlistFragment.
 *
 * These tests verify that the fragment launches correctly,
 * receives the event ID argument, and displays the main UI elements.
 *
 * Before running these tests, replace TEST_EVENT_ID with a real
 * event ID from Firebase.
 */
@RunWith(AndroidJUnit4.class)
public class WaitlistFragmentTest {

    /**
     * Replace this with a real event ID from Firebase before running tests.
     */
    private static final String TEST_EVENT_ID = "staYBy7EYit3QDjyE0rf";

    /**
     * Time to wait for Firestore data to load.
     */
    private static final long LOAD_WAIT_MS = 3000;

    private FragmentScenario<WaitlistFragment> scenario;

    /**
     * Launches the fragment before each test.
     */
    @Before
    public void setUp() {
        Bundle args = new Bundle();
        args.putString("eventId", TEST_EVENT_ID);

        scenario = FragmentScenario.launchInContainer(WaitlistFragment.class, args);
    }

    /**
     * Waits briefly for Firestore data to load.
     */
    private void waitForDataToLoad() {
        SystemClock.sleep(LOAD_WAIT_MS);
    }

    /**
     * Verifies that the fragment receives the event ID argument.
     */
    @Test
    public void fragment_receivesEventIdArgument() {
        scenario.onFragment(fragment ->
                assertEquals(TEST_EVENT_ID, fragment.getArguments().getString("eventId")));
    }

    /**
     * Verifies that the RecyclerView is displayed.
     */
    @Test
    public void waitlistRecyclerView_isDisplayed() {
        onView(withId(R.id.rvWaitlist))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that the notify waitlist button is displayed.
     */
    @Test
    public void notifyWaitlistButton_isDisplayed() {
        onView(withId(R.id.btnNotifyWaitlist))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that the draw lottery button is displayed.
     */
    @Test
    public void drawLotteryButton_isDisplayed() {
        onView(withId(R.id.btnDrawLottery))
                .check(matches(isDisplayed()));
    }

    /**
     * Verifies that the notify waitlist button is enabled.
     */
    @Test
    public void notifyWaitlistButton_isEnabled() {
        onView(withId(R.id.btnNotifyWaitlist))
                .check(matches(isEnabled()));
    }

    /**
     * Verifies that the draw lottery button is enabled.
     */
    @Test
    public void drawLotteryButton_isEnabled() {
        onView(withId(R.id.btnDrawLottery))
                .check(matches(isEnabled()));
    }

    /**
     * Verifies that the RecyclerView has an adapter attached.
     */
    @Test
    public void waitlistRecyclerView_hasAdapter() {
        scenario.onFragment(fragment -> {
            RecyclerView recyclerView = fragment.requireView().findViewById(R.id.rvWaitlist);
            assertNotNull(recyclerView.getAdapter());
        });
    }

    /**
     * Verifies that clicking the draw lottery button does not crash the fragment.
     */
    @Test
    public void drawLotteryButton_canBeClicked() {
        waitForDataToLoad();

        onView(withId(R.id.btnDrawLottery)).perform(click());
    }

    /**
     * Verifies that clicking the notify waitlist button does not crash the fragment.
     */
    @Test
    public void notifyWaitlistButton_canBeClicked() {
        waitForDataToLoad();

        onView(withId(R.id.btnNotifyWaitlist)).perform(click());
    }


}