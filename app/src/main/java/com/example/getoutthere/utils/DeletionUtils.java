package com.example.getoutthere.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Utility class for handling complex cascade deletions to ensure data integrity.
 * <p>
 * Outstanding Issues:
 * - This method deletes documents individually rather than doing it in a batch.
 * If the app loses internet connection during the loop, it could result in a
 * partial deletion state where some events are deleted but the profile remains.
 */
public class DeletionUtils {

    /**
     * Deletes a user profile and cascades the deletion to all events associated
     * with that user (where the user is the organizer).
     * * @param userId    The unique document ID of the user's profile to be deleted.
     * @param onSuccess A Runnable callback to execute if the entire deletion process succeeds.
     * @param onFailure A Runnable callback to execute if any part of the deletion process fails.
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


    /**
     * Revokes the Organizer status of a profile, and cascades the deletion of all events associated.
     * * @param userId    The unique document ID of the organizer's profile to be revoked.
     * @param onSuccess A Runnable callback to execute if the entire deletion process succeeds.
     * @param onFailure A Runnable callback to execute if any part of the deletion process fails.
     */
    public static void banOrganizerAndCascadeEvents(String userId, Runnable onSuccess, Runnable onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //  Disable their ability to organize in their profile
        db.collection("profiles").document(userId)
                .update("organizingEnabled", false) // This is the new "Ban" flag
                .addOnSuccessListener(aVoid -> {

                    // Find and delete all events they own (The Cascade)
                    db.collection("events").whereEqualTo("organizerId", userId).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    db.collection("events").document(doc.getId()).delete();
                                }
                                onSuccess.run();
                            })
                            .addOnFailureListener(e -> onFailure.run());
                })
                .addOnFailureListener(e -> onFailure.run());
    }
}