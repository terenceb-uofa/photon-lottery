package com.example.getoutthere.repositories;
import com.example.getoutthere.event.Event;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * Handles storing, retrieving, and updating Event objects in Firebase.
 * <p>
 * This repository is responsible for creating new events, uploading optional
 * poster images, and fetching or updating event data in Firestore.
 * </p>
 * <p>
 * Outstanding Issues:
 * - None
 * </p>
 *  * @author Yousaf Cheema + Terence Bedell
 *  * @version 1.0
 */

public class EventRepository {

    private final FirebaseFirestore db;

    private final StorageReference storageRef;

    /**
     * Constructs a new {@code EventRepository} and initializes Firebase
     * Firestore and Storage references.
     */
    public EventRepository(){
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    /**
     * Callback interface used to return the result of async repo operations.
     *
     * @param <T> the type of result returned when the operation is successful
     */
    public interface RepositoryCallback<T>{
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    /**
     * Creates a new event in Firestore and optionally uploads a poster image (depending on if user uploads one).
     * A unique event ID is generated and assigned to the event before saving.
     * A unique QR code link is generated and assigned to the event before saving.
     *
     * @param event the event to create
     * @param posterUri the selected poster image URI, or null if no poster is provided
     * @param callback callback used to return the new event ID or an error
     */
    public void createEvent(Event event, @Nullable Uri posterUri, @NonNull RepositoryCallback<String> callback){

        String newEventId = db.collection("events").document().getId();
        event.setId(newEventId);

        // set default waitlist value for new event
        event.setCurrentWaitlistCount(0);

        // create qrcode deeplink for the event using the event id
        if (event.getQrCodeContent() == null || event.getQrCodeContent().isEmpty()) {
            event.setQrCodeContent("getoutthere://event/" + newEventId);
        }

        // save to firestore
        if (posterUri != null){
            uploadPosterAndSave(event,posterUri,callback);
        } else {

            saveEventToFirestore(event, callback);

        }


    }

    /**
     * Uploads a poster image to Firebase Storage and then saves the event.
     * If the image upload fails, the event is still saved but without a poster.
     *
     * @param event the event being saved
     * @param posterUri the poster image URI
     * @param callback callback used to return the event ID or an error
     */
    private void uploadPosterAndSave(Event event,
                                     Uri posterUri,
                                     RepositoryCallback<String> callback) {

        StorageReference posterRef = storageRef.child("event_posters/" + event.getId() + ".jpg");

        posterRef.putFile(posterUri)
                .addOnSuccessListener(taskSnapshot ->
                        posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            event.setPosterUrl(uri.toString());
                            saveEventToFirestore(event, callback);
                        }).addOnFailureListener(callback::onFailure)
                )
                .addOnFailureListener(e -> {
                    // save without the poster if the upload fails
                    saveEventToFirestore(event, callback);
                });
    }

    /**
     * Saves the event object to Firestore using its event ID.
     *
     * @param event the event to save
     * @param callback callback used to return the event ID or an error
     */
    private void saveEventToFirestore(Event event, RepositoryCallback<String> callback) {
        db.collection("events")
                .document(event.getId())
                .set(event)
                .addOnSuccessListener(unused -> callback.onSuccess(event.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Fetches an event from Firestore using its event ID.
     * If the event exists, it is converted into an Event object and returned.
     *
     * @param eventId the ID of the event to retrieve
     * @param callback callback used to return the event or an error
     */
    public void getEventById(@NonNull String eventId, @NonNull RepositoryCallback<Event> callback){

        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onFailure(new Exception("Event not found"));
                    return;
                }

                Event event = documentSnapshot.toObject(Event.class);
                if (event == null){
                    callback.onFailure(new Exception("Failed to parse event"));
                    return;
                }

                event.setId(documentSnapshot.getId());
                callback.onSuccess(event);

                }).addOnFailureListener(callback::onFailure);

    }
    /**
     * Fetches all events for param organizerId from the Firestore.
     * If the organizer exists, all events created by the organizer are added to the event list adn returned.
     *
     * @param organizerId the ID of the event to retrieve
     * @param callback callback used to return the event or an error
     */
    public void getEventsByOrganizerId(@NonNull String organizerId,
                                       @NonNull RepositoryCallback<java.util.List<Event>> callback) {
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<Event> events = new java.util.ArrayList<>();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setId(doc.getId());
                        events.add(event);
                    }

                    callback.onSuccess(events);

                }).addOnFailureListener(callback::onFailure);
    }

    /**
     * Fetches all events where the user is either the primary organizer OR a co-organizer.
     *
     * @param userId the device ID of the user
     * @param callback callback used to return the list of events
     */
    public void getEventsByOrganizerOrCoOrganizer(@NonNull String userId,
                                                  @NonNull RepositoryCallback<java.util.List<Event>> callback) {

        // Using Filter.or to combine the two conditions
        db.collection("events")
                .where(com.google.firebase.firestore.Filter.or(
                        com.google.firebase.firestore.Filter.equalTo("organizerId", userId),
                        com.google.firebase.firestore.Filter.arrayContains("coOrganizerIds", userId)
                ))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<Event> events = new java.util.ArrayList<>();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setId(doc.getId());
                            events.add(event);
                        }
                    }

                    callback.onSuccess(events);
                })
                .addOnFailureListener(callback::onFailure);
    }


    /**
     * Updates an existing event in Firestore.
     * If a new poster image is provided, it uploads the image before saving the updated event.
     *
     * @param event the event containing the updated information
     * @param posterUri the URI of the new poster image, or null if the poster is unchanged
     * @param callback callback used to indicate whether the update was successful
     */
    public void updateEvent(Event event,
                            @Nullable Uri posterUri,
                            @NonNull RepositoryCallback<Void> callback) {

        if (posterUri != null) {
            StorageReference posterRef = storageRef.child("event_posters/" + event.getId() + ".jpg");

            posterRef.putFile(posterUri)
                    .addOnSuccessListener(taskSnapshot ->
                            posterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                event.setPosterUrl(uri.toString());

                                db.collection("events")
                                        .document(event.getId())
                                        .set(event)
                                        .addOnSuccessListener(unused -> callback.onSuccess(null))
                                        .addOnFailureListener(callback::onFailure);
                            }).addOnFailureListener(callback::onFailure)
                    )
                    .addOnFailureListener(callback::onFailure);

        } else {
            db.collection("events")
                    .document(event.getId())
                    .set(event)
                    .addOnSuccessListener(unused -> callback.onSuccess(null))
                    .addOnFailureListener(callback::onFailure);
        }
    }



}
