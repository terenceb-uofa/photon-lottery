package com.example.getoutthere.utils;

import com.example.getoutthere.models.EntrantProfile;
import com.example.getoutthere.event.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// Firebase Functions

public class FirebaseHelper {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Add entrant to waiting list
    public static void joinWaitingList(Event event, EntrantProfile entrant) {
        CollectionReference waitingList = db.collection("events")
                .document(event.getId())
                .collection("waitingList");

        Map<String, Object> data = new HashMap<>();
        data.put("name", entrant.getName());
        data.put("email", entrant.getEmail());
        data.put("phone", entrant.getPhoneNumber());

        waitingList.document(entrant.getDeviceId()).set(data);
    }

    // Remove entrant from waiting list
    public static void leaveWaitingList(Event event, EntrantProfile entrant) {
        db.collection("events")
                .document(event.getId())
                .collection("waitingList")
                .document(entrant.getDeviceId())
                .delete();
    }
}