package com.example.getoutthere.utils;

import com.example.getoutthere.models.EntrantProfile;
import com.example.getoutthere.event.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface WaitlistCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public static void joinWaitingList(Event event, EntrantProfile entrant, WaitlistCallback callback) {
        DocumentReference eventRef = db.collection("events").document(event.getId());
        DocumentReference entrantRef = eventRef.collection("waitingList").document(entrant.getDeviceId());

        db.runTransaction((Transaction.Function<Void>) transaction -> {

            DocumentSnapshot eventSnapshot = transaction.get(eventRef);
            DocumentSnapshot entrantSnapshot = transaction.get(entrantRef);

            if (!eventSnapshot.exists()) {
                throw new FirebaseFirestoreException("Event not found", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            // Are they already on the list?
            if (entrantSnapshot.exists()) {
                throw new FirebaseFirestoreException("Already on waitlist", FirebaseFirestoreException.Code.ALREADY_EXISTS);
            }

            // Is the waitlist full?
            Long waitlistLimit = eventSnapshot.getLong("waitlistLimit");
            Long currentWaitlistCount = eventSnapshot.getLong("currentWaitlistCount");

            if (currentWaitlistCount == null) {
                currentWaitlistCount = 0L;
            }

            if (waitlistLimit != null && currentWaitlistCount >= waitlistLimit) {
                throw new FirebaseFirestoreException("Waitlist is full", FirebaseFirestoreException.Code.ABORTED);
            }

            // Update the count
            transaction.update(eventRef, "currentWaitlistCount", currentWaitlistCount + 1);

            // Add the entrant's data
            Map<String, Object> data = new HashMap<>();
            data.put("name", entrant.getName());
            data.put("email", entrant.getEmail());
            data.put("phone", entrant.getPhoneNumber());

            transaction.set(entrantRef, data);

            return null; // success

        }).addOnSuccessListener(aVoid -> {
            callback.onSuccess();
        }).addOnFailureListener(e -> {
            // Handle the different types of rejections
            if (e.getMessage() != null && e.getMessage().contains("Already on waitlist")) {
                callback.onFailure("You are already on the waiting list!");
            } else if (e.getMessage() != null && e.getMessage().contains("Waitlist is full")) {
                callback.onFailure("Cannot join. The waitlist is currently full!");
            } else {
                callback.onFailure("Error joining waitlist: " + e.getMessage());
            }
        });
    }

    public static void leaveWaitingList(Event event, EntrantProfile entrant) {
    }
}