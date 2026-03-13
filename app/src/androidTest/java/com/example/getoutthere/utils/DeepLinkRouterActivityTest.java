package com.example.getoutthere.utils;

import android.content.Intent;
import android.net.Uri;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.event.EventDetailsActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeepLinkRouterActivityTest {

    @Before
    public void setUp() {
        // Initialize the Intents testing framework
        Intents.init();
    }

    @After
    public void tearDown() {
        // Clear Intents state after the test runs
        Intents.release();
    }

    @Test
    public void testDeepLinkCorrectlyRoutesToEventDetails() {
        // Arrange: Fake a deep link intent containing a dummy event ID
        String testEventId = "xyz098765";
        Intent deepLinkIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://getoutthere.com/event/" + testEventId));

        // Act: Launch the router activity with the fake deep link
        ActivityScenario<DeepLinkRouterActivity> scenario = ActivityScenario.launch(deepLinkIntent);

        // Assert: Verify the router fired an Intent to EventDetailsActivity with the correct ID
        Intents.intended(IntentMatchers.hasComponent(EventDetailsActivity.class.getName()));
        Intents.intended(IntentMatchers.hasExtra("eventId", testEventId));

        scenario.close();
    }
}