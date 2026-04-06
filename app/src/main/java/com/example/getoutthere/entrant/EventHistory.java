package com.example.getoutthere.entrant;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.example.getoutthere.event.EventDetailsActivity;
import com.example.getoutthere.navigation.NavBottomHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// The following code is from Anthropic, Claude, "Take the given OrganizerEventListActivity, OrganizerEventAdapter, EventListActivity, EventDiscoverAdapter, and rewrite EventHistory and create HistoryEventAdapter it to work for the given history ui xml files", 2026-04-06


public class EventHistory extends AppCompatActivity {
    private ListView listView;
    private FirebaseFirestore db;
    private String deviceId;

    private final List<Event> historyEvents = new ArrayList<>();
    private final Map<String, String> historyStatuses = new HashMap<>();
    private com.example.getoutthere.entrant.HistoryEventAdapter adapter;

    private MaterialButton notificationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        notificationButton = findViewById(R.id.notificationButton);
        if (notificationButton != null) {
            notificationButton.setOnClickListener(v -> {
                Intent intent = new Intent(EventHistory.this, EntrantNotificationActivity.class);
                startActivity(intent);
            });
        }

        listView = findViewById(R.id.historyListView);
        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        adapter = new com.example.getoutthere.entrant.HistoryEventAdapter(this, historyEvents, historyStatuses);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.emptyHistoryText));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = historyEvents.get(position);

            Intent intent = new Intent(EventHistory.this, EventDetailsActivity.class);
            intent.putExtra("eventId", selectedEvent.getId());
            startActivity(intent);
        });

        loadEventHistory();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavBottomHelper.setupBottomNav(this, bottomNav, R.id.nav_history);
    }

    private void loadEventHistory() {
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyEvents.clear();
                    historyStatuses.clear();
                    adapter.notifyDataSetChanged();

                    if (queryDocumentSnapshots.isEmpty()) {
                        return;
                    }

                    for (QueryDocumentSnapshot eventDoc : queryDocumentSnapshots) {
                        Event event = eventDoc.toObject(Event.class);
                        event.setId(eventDoc.getId());

                        // force date fields from Firestore doc
                        Timestamp startDate = eventDoc.getTimestamp("startDate");
                        if (startDate != null) {
                            event.setStartDate(startDate);
                        }

                        eventDoc.getReference()
                                .collection("waitingList")
                                .document(deviceId)
                                .get()
                                .addOnSuccessListener(waitlistDoc -> {
                                    if (waitlistDoc.exists()) {
                                        String status = waitlistDoc.getString("status");
                                        historyEvents.add(event);
                                        historyStatuses.put(event.getId(), status);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show()
                );
    }
}