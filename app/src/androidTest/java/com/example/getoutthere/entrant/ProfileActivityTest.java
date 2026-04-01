package com.example.getoutthere.entrant;

import android.provider.Settings;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test to verify End-to-End database operations (Save & Delete)
 * for the ProfileActivity.
 */
@RunWith(AndroidJUnit4.class)
public class ProfileActivityTest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    @Test
    public void testProfileSaveAndDeleteInDatabase() throws Exception {
        // Get the device ID and a reference to the real Firebase Database
        String deviceId = Settings.Secure.getString(
                ApplicationProvider.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Thread.sleep(3000);
        // Forcefully replace the text in the fields
        onView(withId(R.id.editTextName)).perform(replaceText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.editTextEmail)).perform(replaceText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.editTextPhone)).perform(replaceText("1234567890"), closeSoftKeyboard());

        // Click the Save button
        onView(withId(R.id.buttonSaveProfile)).perform(androidx.test.espresso.action.ViewActions.click());

        // Wait 3 seconds for Firebase to finish uploading over the internet
        Thread.sleep(3000);

        // Query the actual database and verify the profile exists and matches!
        DocumentSnapshot savedDoc = Tasks.await(db.collection("profiles").document(deviceId).get());
        assertTrue("The profile document should exist in the database", savedDoc.exists());
        assertEquals("Test User", savedDoc.getString("name"));
        assertEquals("test@example.com", savedDoc.getString("email"));

        // Click the Delete button
        onView(withId(R.id.buttonDeleteProfile)).perform(androidx.test.espresso.action.ViewActions.click());

        // Wait 3 seconds for Firebase to finish the deletion
        Thread.sleep(3000);

        // Query the database again and verify the profile is gone
        DocumentSnapshot deletedDoc = Tasks.await(db.collection("profiles").document(deviceId).get());
        assertFalse("The profile document should be deleted from the database", deletedDoc.exists());
    }
}