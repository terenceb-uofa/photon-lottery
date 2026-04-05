package com.example.getoutthere.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.example.getoutthere.navigation.NavBottomHelper;
import com.example.getoutthere.repositories.EventRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private final List<Event> myEvents = new ArrayList<>();
    private OrganizerEventAdapter adapter;
    private final EventRepository eventRepository = new EventRepository();

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_event_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.organizerEventListView);
        adapter =  new OrganizerEventAdapter(this, myEvents);
        listView.setAdapter(adapter);
        String currentUserId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Fetch only events created by specific organizer
        eventRepository.getEventsByOrganizerId(currentUserId,
                new EventRepository.RepositoryCallback<List<Event>>() {
                    @Override
                    public void onSuccess(List<Event> result) {
                        myEvents.clear();
                        myEvents.addAll(result);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(
                                OrganizerEventListActivity.this,
                                "Failed to load events",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

        // Click an event to see details/edit
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = myEvents.get(position);
            Intent intent = new Intent(this, OrganizerEventDetailsActivity.class);
            intent.putExtra("eventId", selectedEvent.getId());
            startActivity(intent);
        });


        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavBottomHelper.setupBottomNav(this, bottomNav, R.id.nav_manage_events);
    }
}