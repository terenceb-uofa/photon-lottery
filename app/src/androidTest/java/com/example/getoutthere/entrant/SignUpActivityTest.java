package com.example.getoutthere.entrant;

import android.provider.Settings;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
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
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Instrumented test to verify user registration and navigation intents
 * in the SignUpActivity.
 */
@RunWith(AndroidJUnit4.class)
public class SignUpActivityTest {

    @Rule
    public ActivityScenarioRule<SignUpActivity> activityRule =
            new ActivityScenarioRule<>(SignUpActivity.class);

    private EntrantProfile backupProfile = null;
    private boolean profileExisted = false;

    @Before
    public void setUp() throws Exception {
        Intents.init();

        // Backup the user's existing profile before the test deletes it
        String deviceId = Settings.Secure.getString(
                ApplicationProvider.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentSnapshot doc = Tasks.await(db.collection("profiles").document(deviceId).get());
        if (doc.exists()) {
            profileExisted = true;
            // Save a copy of the real profile to memory
            backupProfile = doc.toObject(EntrantProfile.class);
        }
    }

    @After
    public void tearDown() throws Exception {
        // Restore the user's original profile to the database
        String deviceId = Settings.Secure.getString(
                ApplicationProvider.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (profileExisted && backupProfile != null) {
            // Put the real profile back
            Tasks.await(db.collection("profiles").document(deviceId).set(backupProfile));
        } else {
            // If they didn't have a profile before the test, delete the test one
            Tasks.await(db.collection("profiles").document(deviceId).delete());
        }

        Intents.release();
    }

    @Test
    public void testSuccessfulSignUp_FiresIntentToDashboard() throws InterruptedException {
        // Forcefully replace the text in the sign-up fields with test data
        onView(withId(R.id.signUpName)).perform(replaceText("New Test User"), closeSoftKeyboard());
        onView(withId(R.id.signUpEmail)).perform(replaceText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.signUpPhone)).perform(replaceText("1112223333"), closeSoftKeyboard());

        // Click the Create Account button
        onView(withId(R.id.buttonCreateAccount)).perform(click());

        // Wait 3 seconds for Firebase to process the save over the internet
        Thread.sleep(3000);

        // Verify that the successful save triggered an Intent to EntrantDashboardActivity
        intended(hasComponent(EntrantDashboardActivity.class.getName()));
    }
}