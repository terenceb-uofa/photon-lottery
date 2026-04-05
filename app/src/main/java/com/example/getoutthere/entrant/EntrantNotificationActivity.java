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

public class EntrantNotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private TextView emptyNotificationText;
    private Button btnBack;

    private FirebaseFirestore db;
    private String deviceId;
    private EntrantProfile currentProfile;

    private NotificationAdapter adapter;
    private List<DocumentSnapshot> notificationList = new ArrayList<>();

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

    private void loadUserProfile() {
        db.collection("profiles").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentProfile = documentSnapshot.toObject(EntrantProfile.class);
            }
        });
    }

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
    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

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

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

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
                holder.btnDecline.setOnClickListener(v -> declinePrivateInvite(notifId));
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

        @Override
        public int getItemCount() {
            return notificationList.size();
        }

        //  Private invite logic
        private void acceptPrivateInvite(String eventId, String notifId) {
            if (currentProfile == null || eventId == null) return;
            db.collection("events").document(eventId).collection("waitingList").document(deviceId)
                    .set(getProfileMap())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EntrantNotificationActivity.this, "Private Invite Accepted!", Toast.LENGTH_SHORT).show();
                        deleteNotification(notifId);
                    });
        }

        private void declinePrivateInvite(String notifId) {
            deleteNotification(notifId);
            Toast.makeText(EntrantNotificationActivity.this, "Private Invite Declined", Toast.LENGTH_SHORT).show();
        }

        //  Lottery invite logic
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

        private void declineCoOrganizerInvite(String notifId) {
            deleteNotification(notifId);
            Toast.makeText(EntrantNotificationActivity.this, "Declined Co-Organizer Invite", Toast.LENGTH_SHORT).show();
        }

        // Helper to format profile map
        private HashMap<String, Object> getProfileMap() {
            return new HashMap<String, Object>() {{
                put("name", currentProfile.getName());
                put("email", currentProfile.getEmail());
                put("phone", currentProfile.getPhoneNumber());
            }};
        }

        private void deleteNotification(String notifId) {
            db.collection("profiles").document(deviceId).collection("notifications").document(notifId).delete();
        }
    }
}