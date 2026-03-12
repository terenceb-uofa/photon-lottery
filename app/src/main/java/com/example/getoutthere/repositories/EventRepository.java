package com.example.getoutthere.repositories;
import com.example.getoutthere.event.Event;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EventRepository {

    private final FirebaseFirestore db;

    private final StorageReference storageRef;


    public EventRepository(){
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public interface RepositoryCallback<T>{
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public void createEvent(Event event, @Nullable Uri posterUri, @NonNull RepositoryCallback<String> callback){

        String newEventId = db.collection("events").document().getId();
        event.setId(newEventId);

        // set default waitlist value for new event
        event.setCurrentWaitlistCount(0);

        // Qrcode
        if (event.getQrCodeContent() == null || event.getQrCodeContent().isEmpty()) {
            event.setQrCodeContent("event:" + newEventId);
        }

        // save to firestore
        if (posterUri != null){
            uploadPosterAndSave(event,posterUri,callback);
        } else {

            saveEventToFirestore(event, callback);

        }


    }

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
                    // Save without poster if upload fails
                    saveEventToFirestore(event, callback);
                });
    }


    private void saveEventToFirestore(Event event, RepositoryCallback<String> callback) {
        db.collection("events")
                .document(event.getId())
                .set(event)
                .addOnSuccessListener(unused -> callback.onSuccess(event.getId()))
                .addOnFailureListener(callback::onFailure);
    }


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

    public void updateEvent(@NonNull Event event, @NonNull RepositoryCallback<Void> callback){

        db.collection("events")
                .document(event.getId())
                .set(event)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);

    }



}
