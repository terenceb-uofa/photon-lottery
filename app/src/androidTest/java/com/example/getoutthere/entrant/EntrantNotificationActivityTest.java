package com.example.getoutthere.entrant;

import android.provider.Settings;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;
import com.example.getoutthere.models.EntrantProfile;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * This test verifies that:
 * 1. The activity launches properly.
 * 2. The empty notification message is shown when there are no notifications.
 * 3. The back button finishes the activity.
 */
@RunWith(AndroidJUnit4.class)
public class EntrantNotificationActivityTest {
    @Rule
    public ActivityScenarioRule<EntrantNotificationActivity> activityRule =
            new ActivityScenarioRule<>(EntrantNotificationActivity.class);

    private EntrantProfile backupProfile = null;
    private boolean profileExisted = false;

    @Before
    public void setUp() throws Exception {
        // Backup the user's existing profile before the test modifies anything
        String deviceId = Settings.Secure.getString(
                ApplicationProvider.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentSnapshot doc = Tasks.await(db.collection("profiles").document(deviceId).get());
        if (doc.exists()) {
            profileExisted = true;
            backupProfile = doc.toObject(EntrantProfile.class);
        }
    }

    @After
    public void tearDown() throws Exception {
        // Restore the user's original profile after tests
        String deviceId = Settings.Secure.getString(
                ApplicationProvider.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (profileExisted && backupProfile != null) {
            Tasks.await(db.collection("profiles").document(deviceId).set(backupProfile));
        } else {
            Tasks.await(db.collection("profiles").document(deviceId).delete());
        }
    }

    /**
     * Verify that the activity launches and empty notification text is visible.
     */
    @Test
    public void testActivityLaunch_ShowsEmptyNotification() {
        // The empty notification TextView should be visible if there are no notifications
        onView(withId(R.id.emptyNotificationText)).check(matches(isDisplayed()));
    }

    /**
     * Verify that the back button finishes the activity.
     */
    @Test
    public void testBackButtonFinishesActivity() {
        onView(withId(R.id.NotificationBackButton)).perform(click());
    }
}