package com.example.getoutthere.organizer;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.getoutthere.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the list of entrants currently on the event's waiting list.
 * <p>
 * This activity retrieves all entrants from the selected event's waiting list,
 * looks up their profile names in Firestore, and displays the names in a ListView.
 * <p>
 * Outstanding Issues:
 * - None
 *
 * @author Yousaf Cheema
 * @version 1.0
 */
public class EntrantListActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<String> entrantNames = new ArrayList<>();
    private final List<String> entrantDeviceIds = new ArrayList<>();

    private String eventId;

    private Button backButton;

    /**
     * Initializes the activity, connects the ListView and button to the layout,
     * retrieves the event ID, and loads the entrants on the waiting list.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_list);

        listView = findViewById(R.id.entrantListView);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                entrantNames
        );
        listView.setAdapter(adapter);

        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "No event ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadWaitlistEntrants();

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

    }

    /**
     * Fetches all entrants from the selected event's waiting list collection in Firestore.
     * It stores each entrant's device ID and loads their profile name so it can
     * be displayed in the ListView.
     * If the waiting list is empty it displays "No one is on the waiting list."
     */
    private void loadWaitlistEntrants() {
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    entrantNames.clear();
                    entrantDeviceIds.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        entrantNames.add("No one is on the waiting list.");
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String deviceId = doc.getId();

                        entrantDeviceIds.add(deviceId);
                        entrantNames.add("Loading...");
                        int index = entrantNames.size() - 1;

                        loadProfileName(deviceId, index);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load waiting list", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Fetches the entrant's profile name from Firestore using their device ID.
     * If a name is found, it replaces the temporary loading text in the ListView.
     * If the profile cannot be loaded, the entrant is shown as an unknown user.
     *
     * @param deviceId the device ID of the entrant whose profile is being loaded
     * @param index the position in the list where the entrant's name should be updated
     */
    private void loadProfileName(String deviceId, int index) {
        db.collection("profiles")
                .document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String name = "Unknown user";

                    if (documentSnapshot.exists()) {
                        String fetchedName = documentSnapshot.getString("name");
                        if (fetchedName != null && !fetchedName.isEmpty()) {
                            name = fetchedName;
                        }
                    }

                    entrantNames.set(index, name);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    entrantNames.set(index, "Unknown user");
                    adapter.notifyDataSetChanged();
                });
    }
}