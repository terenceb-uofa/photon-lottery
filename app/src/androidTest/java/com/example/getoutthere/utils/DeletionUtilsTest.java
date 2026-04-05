package com.example.getoutthere.utils;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


// This test is AI generated with Anthropics Claude Code, Prompt: "Generate an android test for the following code", Date: Mar 13, 2026


/**
 * Instrumented test to verify the cascading deletion logic in DeletionUtils.
 */
@RunWith(AndroidJUnit4.class)
public class DeletionUtilsTest {

    @Test
    public void testDeleteProfileAndCascadeEvents() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String fakeUserId = "test_cascade_user_123";
        String fakeEventId = "test_cascade_event_123";

        //  Create a fake profile in the database
        Map<String, Object> fakeProfile = new HashMap<>();
        fakeProfile.put("name", "Cascade Tester");
        Tasks.await(db.collection("profiles").document(fakeUserId).set(fakeProfile));

        //  Create a fake event linked to that profile (organizerId)
        Map<String, Object> fakeEvent = new HashMap<>();
        fakeEvent.put("organizerId", fakeUserId);
        fakeEvent.put("eventName", "Cascade Test Event");
        Tasks.await(db.collection("events").document(fakeEventId).set(fakeEvent));

        // Verify they were actually created before we test deleting them
        assertTrue(Tasks.await(db.collection("profiles").document(fakeUserId).get()).exists());
        assertTrue(Tasks.await(db.collection("events").document(fakeEventId).get()).exists());

        // Call the DeletionUtils method using a CountDownLatch to wait for the callback
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] successFlag = {false};

        DeletionUtils.deleteProfileAndCascadeEvents(fakeUserId,
                () -> { // onSuccess Runnable
                    successFlag[0] = true;
                    latch.countDown();
                },
                () -> { // onFailure Runnable
                    latch.countDown();
                }
        );

        // Wait up to 5 seconds for the async deletion callbacks to fire
        latch.await(5, TimeUnit.SECONDS);

        // Verify the success callback was triggered
        assertTrue("The onSuccess callback should have been triggered", successFlag[0]);

        // Verify the profile and the event are physically gone from the database
        DocumentSnapshot deletedProfile = Tasks.await(db.collection("profiles").document(fakeUserId).get());
        DocumentSnapshot deletedEvent = Tasks.await(db.collection("events").document(fakeEventId).get());

        assertFalse("The profile should be deleted", deletedProfile.exists());
        assertFalse("The cascaded event should be deleted", deletedEvent.exists());
    }
}