package com.example.getoutthere.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Utility class for handling complex cascade deletions to ensure data integrity.
 */
public class DeletionUtils {
    /**
     * Deletes a profile and all events associated with that organizer.
     */
    public static void deleteProfileAndCascadeEvents(String userId, Runnable onSuccess, Runnable onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Find all events owned by this user.
        db.collection("events").whereEqualTo("organizerId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Delete all found events
                    for (QueryDocumentSnapshot eventDoc : queryDocumentSnapshots) {
                        db.collection("events").document(eventDoc.getId()).delete();
                    }

                    // Finally, delete the profile itself
                    db.collection("profiles").document(userId).delete()
                            .addOnSuccessListener(aVoid -> onSuccess.run())
                            .addOnFailureListener(e -> onFailure.run());
                })
                .addOnFailureListener(e -> onFailure.run());
    }
}