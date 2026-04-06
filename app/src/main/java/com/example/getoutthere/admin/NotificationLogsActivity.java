package com.example.getoutthere.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getoutthere.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Allows administrators to view all notifications that have been
 * sent out to users.
 * Outstanding Issues:
 * - Requires a Firestore Composite Index on the "notifications" collection group to function.
 */
public class NotificationLogsActivity extends AppCompatActivity {

    private RecyclerView rvNotificationLogs;
    private NotificationLogAdapter adapter;
    private List<DocumentSnapshot> logList = new ArrayList<>();
    private FirebaseFirestore db;

    /**
     * Initializes the activity and showcases the notifications that have
     * been sent to app users. Also creates a button element that allows
     * the user to return to the admin dashboard.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_logs);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        rvNotificationLogs = findViewById(R.id.rvNotificationLogs);
        rvNotificationLogs.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationLogAdapter();
        rvNotificationLogs.setAdapter(adapter);

        // Fetch logs from Firestore
        loadNotificationLogs();

        FrameLayout NotificationLogBackButton = findViewById(R.id.NotificationLogBackButton);

        NotificationLogBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationLogsActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Uses a Collection Group query to fetch all documents in any collection
     * named "notifications" across the entire database, sorted by time.
     */
    private void loadNotificationLogs() {
        db.collectionGroup("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("NotificationLogs", "Error fetching logs", error);
                        Toast.makeText(this, "Failed to load logs: missing Firestore permissions.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    logList.clear();
                    if (value != null) {
                        logList.addAll(value.getDocuments());
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // --- INNER ADAPTER CLASS ---

    private class NotificationLogAdapter extends RecyclerView.Adapter<NotificationLogAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage, tvEventId, tvDate;

            ViewHolder(View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvLogMessage);
                tvEventId = itemView.findViewById(R.id.tvLogEventId);
                tvDate = itemView.findViewById(R.id.tvLogDate);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DocumentSnapshot doc = logList.get(position);

            String message = doc.getString("message");
            String eventId = doc.getString("eventId");
            Timestamp timestamp = doc.getTimestamp("timestamp");

            holder.tvMessage.setText(message != null ? message : "No message provided");
            holder.tvEventId.setText("Event ID: " + (eventId != null ? eventId : "N/A"));

            if (timestamp != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
                holder.tvDate.setText(sdf.format(timestamp.toDate()));
            } else {
                holder.tvDate.setText("Unknown date");
            }
        }

        @Override
        public int getItemCount() {
            return logList.size();
        }
    }
}