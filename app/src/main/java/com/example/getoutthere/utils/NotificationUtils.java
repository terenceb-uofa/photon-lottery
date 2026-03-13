package com.example.getoutthere.utils;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for building notification data.
 */
public class NotificationUtils {

    /**
     * Builds a notification data map to be stored in Firestore.
     *
     * @param message  The notification message from the organizer.
     * @param eventId  The ID of the event the notification is for.
     * @return         A map containing the notification data.
     */
    public static Map<String, Object> buildNotification(String message, String eventId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("eventId", eventId);
        notification.put("timestamp", Timestamp.now());
        notification.put("read", false);
        return notification;
    }
}