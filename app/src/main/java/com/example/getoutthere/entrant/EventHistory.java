package com.example.getoutthere.entrant;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
public class EventHistory extends AppCompatActivity {
    private ListView listView;
    private Button backButton;
    private FirebaseFirestore db;
    private String deviceId;

    private List<String> historyDisplayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);

        listView = findViewById(R.id.historyListView);
        backButton = findViewById(R.id.HistoryBackButton);
        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyDisplayList);
        listView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish()); // Closes the activity and goes back

        loadEventHistory();
    }

    private void loadEventHistory() {
        // Fetch all events to see which ones the user is registered for
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyDisplayList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        historyDisplayList.add("No event history found.");
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    // Check each event's waiting list for the user's deviceId
                    for (QueryDocumentSnapshot eventDoc : queryDocumentSnapshots) {
                        Event event = eventDoc.toObject(Event.class);

                        eventDoc.getReference().collection("waitingList").document(deviceId).get()
                                .addOnSuccessListener(waitlistDoc -> {
                                    if (waitlistDoc.exists()) {
                                        // User is on this waitlist! Add it to the list view
                                        historyDisplayList.add(event.getName() + " - Status: Waiting List");
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show());
    }
}
