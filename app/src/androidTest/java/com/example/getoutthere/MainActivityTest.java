package com.example.getoutthere;

import android.provider.Settings;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.admin.AdminDashboardActivity;
import com.example.getoutthere.entrant.SignUpActivity;
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
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Instrumented test to verify navigation routing from the MainActivity.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private EntrantProfile backupProfile = null;
    private boolean profileExisted = false;

    @Before
    public void setUp() throws Exception {
        Intents.init();

        // Save the current user's profile to memory before testing the database routing
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
        // Put the user's profile back exactly how it was
        String deviceId = Settings.Secure.getString(
                ApplicationProvider.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (profileExisted && backupProfile != null) {
            Tasks.await(db.collection("profiles").document(deviceId).set(backupProfile));
        }

        Intents.release();
    }

    @Test
    public void testAdminButton_FiresIntentToAdminDashboard() {
        // Click the Admin Dashboard button
        onView(withId(R.id.NavToAdminDashboard)).perform(click());

        // Verify it fired the Intent instantly without needing the database
        intended(hasComponent(AdminDashboardActivity.class.getName()));
    }

    @Test
    public void testEntrantButton_WithNoProfile_RoutesToSignUp() throws Exception {
        // Force the database to act like a brand new user by temporarily deleting the profile
        String deviceId = Settings.Secure.getString(
                ApplicationProvider.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Tasks.await(db.collection("profiles").document(deviceId).delete());

        // Click the Entrant Dashboard button
        onView(withId(R.id.NavToEntrantDashboard)).perform(click());

        // Wait 3 seconds for Firebase to respond saying "User doesn't exist"
        Thread.sleep(3000);

        // Verify the app successfully routed the new user to the SignUp screen
        intended(hasComponent(SignUpActivity.class.getName()));
    }
}