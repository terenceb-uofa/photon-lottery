package com.example.getoutthere.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of Events created by the current user.
 * <p>
 * This Activity queries the Firebase database to retrieve events matching the
 * user's device ID. It populates a ListView with the event names, and
 * allows the organizer to click on an event to view or edit its details.
 * <p>
 * Outstanding Issues:
 * None
 */
public class OrganizerEventListActivity extends AppCompatActivity {

    private ListView listView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Event> myEvents = new ArrayList<>();
    private List<String> eventNames = new ArrayList<>();

    /**
     * Initializes the Activity, sets up the list view, and fetches the organizer's events.
     * Sets an onItemClickListener to fire an Intent to OrganizerEventDetailsActivity.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_event_list);

        listView = findViewById(R.id.organizerEventListView);
        String currentUserId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Fetch only events created by specific organizer
        db.collection("events")
                .whereEqualTo("organizerId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myEvents.clear();
                    eventNames.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setId(doc.getId());
                        myEvents.add(event);
                        eventNames.add(event.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, eventNames);
                    listView.setAdapter(adapter);
                });

        // Click an event to see details/edit
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = myEvents.get(position);
            Intent intent = new Intent(this, OrganizerEventDetailsActivity.class);
            intent.putExtra("eventId", selectedEvent.getId());
            startActivity(intent);
        });
    }
}