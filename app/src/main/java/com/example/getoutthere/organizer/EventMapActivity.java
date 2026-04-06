package com.example.getoutthere.organizer;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.getoutthere.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class EventMapActivity extends AppCompatActivity {

    private MapView map;
    private String eventId;
    private FirebaseFirestore db;

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
    }

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

                            if (!movedCamera) {
                                map.getController().setCenter(point);
                                movedCamera = true;
                            }
                        }
                    }
                    map.invalidate(); // Refresh the map

                    if (!movedCamera) {
                        Toast.makeText(this, "No location data available yet.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load map data.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}
