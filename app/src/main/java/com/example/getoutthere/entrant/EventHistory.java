package com.example.getoutthere.entrant;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.example.getoutthere.event.EventListActivity;
import com.example.getoutthere.navigation.NavBottomHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
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
    //private Button backButton;
    private FirebaseFirestore db;
    private String deviceId;

    private List<String> historyDisplayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;


    private MaterialButton notificationButton;


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
        //backButton = findViewById(R.id.HistoryBackButton);
        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        adapter = new ArrayAdapter<>(this, R.layout.item_history_row, historyDisplayList);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.emptyHistoryText));
        //backButton.setOnClickListener(v -> finish()); // Closes the activity and goes back

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
                                        String status = waitlistDoc.getString("status");
                                        historyDisplayList.add(event.getName() + " - Status: " + status);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show());


        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavBottomHelper.setupBottomNav(this, bottomNav, R.id.nav_history);
    }
}