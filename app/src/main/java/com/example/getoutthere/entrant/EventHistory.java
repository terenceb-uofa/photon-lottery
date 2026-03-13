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

/**
 * Displays a history of events the Entrant has interacted with, specifically
 * focusing on events where they are currently on the waiting list.
 * <p>
 * This Activity acts as a control class for the Event History screen. It queries
 * Firestore for event data and populates a ListView with the entrant's current status.
 * <p>
 * Outstanding Issues:
 * - None
 */
public class EventHistory extends AppCompatActivity {
    private ListView listView;
    private Button backButton;
    private FirebaseFirestore db;
    private String deviceId;

    private List<String> historyDisplayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    /**
     * Initializes the Activity, sets up the user interface elements (ListView and Back Button),
     * and triggers the loading of the user's event history from the database.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);

        listView = findViewById(R.id.historyListView);
        backButton = findViewById(R.id.HistoryBackButton);
        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        adapter = new ArrayAdapter<>(this, R.layout.item_history_row, historyDisplayList);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.emptyHistoryText));
        backButton.setOnClickListener(v -> finish()); // Closes the activity and goes back

        loadEventHistory();
    }

    /**
     * Fetches all events from Firestore and iterates through their waiting lists
     * to determine if the current device ID is registered. If found, it updates
     * the ListView to show the user is on the waiting list.
     */
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