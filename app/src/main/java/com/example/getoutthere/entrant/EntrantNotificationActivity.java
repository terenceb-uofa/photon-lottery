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

            // If it's a private invite, show the buttons. US 01.05.06
            if ("private_invite".equals(type)) {
                holder.layoutInviteButtons.setVisibility(View.VISIBLE);

                holder.btnAccept.setOnClickListener(v -> acceptInvite(eventId, notifId));
                holder.btnDecline.setOnClickListener(v -> declineInvite(notifId));
            } else {
                holder.layoutInviteButtons.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return notificationList.size();
        }

        // US 01.05.07 - Accept
        private void acceptInvite(String eventId, String notifId) {
            if (currentProfile == null || eventId == null) return;

            // Push entrant to event's waitlist
            db.collection("events").document(eventId).collection("waitingList").document(deviceId)
                    .set(new HashMap<String, Object>() {{
                        put("name", currentProfile.getName());
                        put("email", currentProfile.getEmail());
                        put("phone", currentProfile.getPhoneNumber());
                    }})
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EntrantNotificationActivity.this, "Invite Accepted!", Toast.LENGTH_SHORT).show();
                        deleteNotification(notifId); // Remove from inbox
                    })
                    .addOnFailureListener(e -> Toast.makeText(EntrantNotificationActivity.this, "Failed to join event", Toast.LENGTH_SHORT).show());
        }

        // US 01.05.07 - Decline
        private void declineInvite(String notifId) {
            deleteNotification(notifId);
            Toast.makeText(EntrantNotificationActivity.this, "Invite Declined", Toast.LENGTH_SHORT).show();
        }

        private void deleteNotification(String notifId) {
            db.collection("profiles").document(deviceId).collection("notifications").document(notifId).delete();
        }
    }
}