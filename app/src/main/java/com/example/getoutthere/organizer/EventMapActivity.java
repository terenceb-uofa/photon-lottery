package com.example.getoutthere.organizer;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.getoutthere.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * An Activity that displays a map showing the locations of entrants on a waiting list for a specific event.
 * <p>
 * This activity utilizes the osmdroid library to render the map and fetches location data
 * (latitude and longitude) from Firebase Firestore. Markers are placed on the map to represent
 * the geographical locations of the users.
 * </p>
 */
public class EventMapActivity extends AppCompatActivity {

    /**
     * The map view used to display the geographical data.
     */
    private MapView map;

    /**
     * The unique identifier for the event. Used to query the correct Firestore document.
     */
    private String eventId;

    /**
     * Instance of FirebaseFirestore used to retrieve entrant data.
     */
    private FirebaseFirestore db;

    /**
     * Called when the activity is starting. Initializes the osmdroid configuration,
     * sets up the map view, extracts the event ID from the intent, and triggers
     * the retrieval of entrant locations.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in {@link #onSaveInstanceState}. <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize osmdroid configuration using standard Android preferences
        Configuration.getInstance().load(getApplicationContext(),
                getApplicationContext().getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE));

        setContentView(R.layout.activity_event_map);

        eventId = getIntent().getStringExtra("eventId");
        db = FirebaseFirestore.getInstance();

        map = findViewById(R.id.mapView);
        map.setMultiTouchControls(true);
        map.getController().setZoom(10.0);

        loadEntrantLocations();

        FrameLayout backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Queries Firebase Firestore for the "waitingList" subcollection of the current event.
     * Iterates through the documents to extract the latitude, longitude, and name of each entrant.
     * If valid coordinates are found, a marker is created and added to the map.
     * The map's camera is automatically centered on the first valid location found.
     * Displays a Toast message if data retrieval fails or if no location data is available.
     */
    private void loadEntrantLocations() {
        if (eventId == null) return;

        db.collection("events").document(eventId).collection("waitingList").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean movedCamera = false;

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        String name = doc.getString("name");

                        if (lat != null && lng != null) {
                            GeoPoint point = new GeoPoint(lat, lng);
                            Marker startMarker = new Marker(map);
                            startMarker.setPosition(point);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            startMarker.setTitle(name != null ? name : "Unknown Entrant");

                            map.getOverlays().add(startMarker);

                            // Center the camera on the first entrant's location
                            if (!movedCamera) {
                                map.getController().setCenter(point);
                                movedCamera = true;
                            }
                        }
                    }
                    map.invalidate(); // Refresh the map to display the new markers

                    if (!movedCamera) {
                        Toast.makeText(this, "No location data available yet.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load map data.", Toast.LENGTH_SHORT).show());
    }

    /**
     * Dispatch onResume() to fragments. Resumes the map view rendering and tracking.
     */
    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    /**
     * Dispatch onPause() to fragments. Pauses the map view rendering and tracking to conserve resources.
     */
    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}