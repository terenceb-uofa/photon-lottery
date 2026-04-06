package com.example.getoutthere.entrant;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getoutthere.R;
import com.example.getoutthere.models.EntrantProfile;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Acts as the notification center for Entrants.
 * <p>
 * Retrieves the user's notifications from Firebase Firestore and displays them in a RecyclerView.
 * Allows users to accept or decline various types of invitations, such as private invites,
 * lottery invites, and co-organizer requests, and handles the respective database updates.
 * </p>
 *
 * Outstanding Issues:
 * - None at the moment.
 */

/**
 * Represents the screen where entrants can view and interact with their notifications.
 * This class handles the structuring, fetching, and processing of user invitations and alerts.
 * @version 1.0
 */
public class EntrantNotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private TextView emptyNotificationText;
    private Button btnBack;

    private FirebaseFirestore db;
    private String deviceId;
    private EntrantProfile currentProfile;

    private NotificationAdapter adapter;
    private List<DocumentSnapshot> notificationList = new ArrayList<>();

    /**
     * Initializes the activity, sets up the RecyclerView for notifications,
     * and initiates data loading for the user's profile and notifications.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_notification);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        rvNotifications = findViewById(R.id.rvNotifications);
        emptyNotificationText = findViewById(R.id.emptyNotificationText);
        btnBack = findViewById(R.id.NotificationBackButton);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        rvNotifications.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        // We need the user's profile info ready in case they accept an invite
        loadUserProfile();
        loadNotifications();
    }

    /**
     * Retrieves the current user's profile information from Firestore using their device ID
     * and stores it in the currentProfile variable.
     */
    private void loadUserProfile() {
        db.collection("profiles").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentProfile = documentSnapshot.toObject(EntrantProfile.class);
            }
        });
    }

    /**
     * Attaches a real-time snapshot listener to the user's notifications collection in Firestore.
     * Updates the RecyclerView dynamically based on the presence or absence of notifications.
     */
    private void loadNotifications() {
        db.collection("profiles").document(deviceId).collection("notifications")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    notificationList.clear();
                    if (value != null && !value.isEmpty()) {
                        notificationList.addAll(value.getDocuments());
                        emptyNotificationText.setVisibility(View.GONE);
                        rvNotifications.setVisibility(View.VISIBLE);
                    } else {
                        emptyNotificationText.setVisibility(View.VISIBLE);
                        rvNotifications.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // --- INNER CLASS ADAPTER ---

    /**
     * Inner class adapter responsible for binding notification data to the RecyclerView.
     * Handles displaying message text and wiring up interactive buttons for invitations.
     */
    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

        /**
         * ViewHolder for notification items.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage;
            LinearLayout layoutInviteButtons;
            Button btnAccept, btnDecline;

            ViewHolder(View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
                layoutInviteButtons = itemView.findViewById(R.id.layoutInviteButtons);
                btnAccept = itemView.findViewById(R.id.btnAccept);
                btnDecline = itemView.findViewById(R.id.btnDecline);
            }
        }

        /**
         * Called when the RecyclerView needs a new {@link ViewHolder} of the given type to represent an item.
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Called by RecyclerView to display the data at the specified position.
         * Adjusts button visibility and attaches appropriate click listeners based on notification type.
         *
         * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DocumentSnapshot notification = notificationList.get(position);
            String message = notification.getString("message");
            String eventId = notification.getString("eventId");
            String type = notification.getString("type");
            String notifId = notification.getId();

            holder.tvMessage.setText(message);

            // Handle both Private Invites and Lottery Invites
            if ("private_invite".equals(type)) {
                holder.layoutInviteButtons.setVisibility(View.VISIBLE);
                holder.btnAccept.setOnClickListener(v -> acceptPrivateInvite(eventId, notifId));
                holder.btnDecline.setOnClickListener(v -> declinePrivateInvite(eventId, notifId));
            }
            else if ("lottery_invite".equals(type)) {
                holder.layoutInviteButtons.setVisibility(View.VISIBLE);
                holder.btnAccept.setOnClickListener(v -> acceptLotteryInvite(eventId, notifId));
                holder.btnDecline.setOnClickListener(v -> declineLotteryInvite(eventId, notifId));
            }
            else if ("co_organizer_invite".equals(type)) {
                holder.layoutInviteButtons.setVisibility(View.VISIBLE);

                holder.btnAccept.setOnClickListener(v -> acceptCoOrganizerInvite(eventId, notifId));
                holder.btnDecline.setOnClickListener(v -> declineCoOrganizerInvite(notifId));
            }
            else {
                holder.layoutInviteButtons.setVisibility(View.GONE);
            }
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of notifications.
         */
        @Override
        public int getItemCount() {
            return notificationList.size();
        }

        /**
         * Accepts a private invitation to an event, updating the user's status from Pending
         * to Waitlist in Firestore, and removes the notification.
         */
        private void acceptPrivateInvite(String eventId, String notifId) {
            if (eventId == null) return;

            // Update the status to "Waitlist"
            db.collection("events").document(eventId).collection("waitingList").document(deviceId)
                    .update("status", "Waitlist")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EntrantNotificationActivity.this, "Added to Waitlist!", Toast.LENGTH_SHORT).show();
                        deleteNotification(notifId);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(EntrantNotificationActivity.this, "Failed to join waitlist.", Toast.LENGTH_SHORT).show();
                    });
        }

        /**
         * Declines a private invitation by updating the status to Cancelled and deleting the notification.
         */
        private void declinePrivateInvite(String eventId, String notifId) {
            if (eventId == null) return;

            // Update the status to "Cancelled"
            db.collection("events").document(eventId).collection("waitingList").document(deviceId)
                    .update("status", "Cancelled")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EntrantNotificationActivity.this, "Private Invite Declined", Toast.LENGTH_SHORT).show();
                        deleteNotification(notifId);
                    });
        }

        /**
         * Accepts a lottery invitation by updating the user's status in the event's waiting list
         * to "Enrolled", and deletes the notification.
         *
         * @param eventId The ID of the event the user won the lottery for.
         * @param notifId The ID of the notification to be deleted.
         */
        private void acceptLotteryInvite(String eventId, String notifId) {
            if (currentProfile == null || eventId == null) return;

            // update the status to "Enrolled"
            db.collection("events").document(eventId).collection("waitingList").document(deviceId)
                    .update("status", "Enrolled")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EntrantNotificationActivity.this, "Successfully Enrolled!", Toast.LENGTH_SHORT).show();
                        deleteNotification(notifId);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(EntrantNotificationActivity.this, "Failed to enroll.", Toast.LENGTH_SHORT).show();
                    });
        }

        /**
         * Declines a lottery invitation by updating the user's status to "Cancelled",
         * deleting the notification, and triggering a replacement draw.
         *
         * @param eventId The ID of the event the user is declining.
         * @param notifId The ID of the notification to be deleted.
         */
        private void declineLotteryInvite(String eventId, String notifId) {
            if (currentProfile == null || eventId == null) return;

            // update the status to "Cancelled"
            db.collection("events").document(eventId).collection("waitingList").document(deviceId)
                    .update("status", "Cancelled")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EntrantNotificationActivity.this, "Invitation Declined.", Toast.LENGTH_SHORT).show();
                        deleteNotification(notifId);

                        // Trigger Redraw
                        drawReplacement(eventId);
                    });
        }

        /**
         * Automatically selects a replacement from the remaining waitlist if a selected user
         * declines their lottery invitation, updating the new winner's status to "Invited"
         * and dispatching a new notification to them.
         *
         * @param eventId The ID of the event for which a replacement is being drawn.
         */
        private void drawReplacement(String eventId) {
            // Find people who have the status "Waitlist"
            db.collection("events").document(eventId).collection("waitingList")
                    .whereEqualTo("status", "Waitlist")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> waitlistDocs = queryDocumentSnapshots.getDocuments();

                            // Randomly select one replacement
                            java.util.Collections.shuffle(waitlistDocs);
                            DocumentSnapshot winnerDoc = waitlistDocs.get(0);
                            String winnerId = winnerDoc.getId();

                            // Update the new winner's status to Invited
                            db.collection("events").document(eventId).collection("waitingList").document(winnerId)
                                    .update("status", "Invited");

                            // Send notification to the new winner
                            HashMap<String, Object> notificationData = new HashMap<>();
                            notificationData.put("eventId", eventId);
                            notificationData.put("message", "You have been selected for an event from the waitlist!");
                            notificationData.put("type", "lottery_invite");

                            db.collection("profiles").document(winnerId).collection("notifications").document().set(notificationData);
                        }
                    });
        }

        /**
         * Accepts an invitation to become a co-organizer for an event. Adds the user's ID
         * to the event's coOrganizerIds array, removes them from the event's waiting list,
         * decrements the waitlist count, and deletes the notification.
         *
         * @param eventId The ID of the event the user will co-organize.
         * @param notifId The ID of the notification to be deleted.
         */
        private void acceptCoOrganizerInvite(String eventId, String notifId) {
            if (eventId == null) return;

            // add user to the coOrganizerIds array in the Event document
            db.collection("events").document(eventId)
                    .update("coOrganizerIds", com.google.firebase.firestore.FieldValue.arrayUnion(deviceId))
                    .addOnSuccessListener(aVoid -> {

                        // remove them from the waiting list so they can't be an entrant anymore
                        db.collection("events").document(eventId).collection("waitingList").document(deviceId).delete();

                        // fix the waitlist count
                        db.collection("events").document(eventId).update("currentWaitlistCount", com.google.firebase.firestore.FieldValue.increment(-1));

                        Toast.makeText(EntrantNotificationActivity.this, "Accepted Co-Organizer Invite!", Toast.LENGTH_SHORT).show();
                        deleteNotification(notifId);
                    });
        }

        /**
         * Declines a co-organizer invitation by deleting the notification from Firestore.
         *
         * @param notifId The ID of the notification to be deleted.
         */
        private void declineCoOrganizerInvite(String notifId) {
            deleteNotification(notifId);
            Toast.makeText(EntrantNotificationActivity.this, "Declined Co-Organizer Invite", Toast.LENGTH_SHORT).show();
        }

        /**
         * Helper method to map the current profile's attributes to a HashMap for Firestore storage.
         *
         * @return A HashMap containing the entrant's name, email, and phone number.
         */
        private HashMap<String, Object> getProfileMap() {
            return new HashMap<String, Object>() {{
                put("name", currentProfile.getName());
                put("email", currentProfile.getEmail());
                put("phone", currentProfile.getPhoneNumber());
            }};
        }

        /**
         * Deletes a specified notification document from the user's notifications collection in Firestore.
         *
         * @param notifId The document ID of the notification to delete.
         */
        private void deleteNotification(String notifId) {
            db.collection("profiles").document(deviceId).collection("notifications").document(notifId).delete();
        }
    }
}