package com.example.getoutthere.event;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;
import com.example.getoutthere.models.EntrantProfile;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView eventName, eventAddress, eventDate, eventCapacity, eventFee;
    private Button btnToggleWaitingList, btnBack;

    private String eventId;
    private Event event;
    private EntrantProfile entrant;

    private boolean isOnWaitingList = false;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // UI elements
        eventName = findViewById(R.id.EventName);
        eventAddress = findViewById(R.id.EventAddress);
        eventDate = findViewById(R.id.EventDate);
        eventCapacity = findViewById(R.id.EventCapacity);
        eventFee = findViewById(R.id.EventSignupFee);
        btnToggleWaitingList = findViewById(R.id.btnToggleWaitingList);
        btnBack = findViewById(R.id.EventDetailsBackButton);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Get event ID from intent
        eventId = getIntent().getStringExtra("eventId");

        // Get device ID for entrant
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        entrant = new EntrantProfile();
        entrant.setDeviceId(deviceId);

        // Fetch event from Firestore
        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                event = documentSnapshot.toObject(Event.class);
                event.setId(documentSnapshot.getId());

                eventName.setText(event.getName());
                eventAddress.setText("Address: " + event.getDescription());
                eventDate.setText("Lottery Draw Date: " + event.getDrawDate());
                eventCapacity.setText("Spots Available: " + event.getCapacity());
                eventFee.setText("Signup Fee: $" + event.getSignupFee());

                // Check if user is already on waiting list
                db.collection("events")
                        .document(eventId)
                        .collection("waitingList")
                        .document(entrant.getDeviceId())
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                isOnWaitingList = true;
                            }
                            updateToggleButton();
                        });
            }
        });

        // Toggle waiting list button
        btnToggleWaitingList.setOnClickListener(v -> {
            if (event == null) return;

            if (!isOnWaitingList) {
                // Join waiting list
                db.collection("events")
                        .document(event.getId())
                        .collection("waitingList")
                        .document(entrant.getDeviceId())
                        .set(new HashMap<String, Object>() {{
                            put("name", entrant.getName());
                            put("email", entrant.getEmail());
                            put("phone", entrant.getPhoneNumber());
                        }})
                        .addOnSuccessListener(aVoid -> {
                            isOnWaitingList = true;
                            updateToggleButton();
                            Toast.makeText(EventDetailsActivity.this, "Joined waiting list!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(EventDetailsActivity.this, "Failed to join waiting list", Toast.LENGTH_SHORT).show()
                        );
            } else {
                // Leave waiting list
                db.collection("events")
                        .document(event.getId())
                        .collection("waitingList")
                        .document(entrant.getDeviceId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            isOnWaitingList = false;
                            updateToggleButton();
                            Toast.makeText(EventDetailsActivity.this, "Left waiting list!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(EventDetailsActivity.this, "Failed to leave waiting list", Toast.LENGTH_SHORT).show()
                        );
            }
        });
    }

    // Update button text and color based on status
    private void updateToggleButton() {
        if (isOnWaitingList) {
            btnToggleWaitingList.setText("Leave Waiting List");
            btnToggleWaitingList.setBackgroundTintList(getResources().getColorStateList(R.color.red, null));
        } else {
            btnToggleWaitingList.setText("Join Waiting List");
            btnToggleWaitingList.setBackgroundTintList(getResources().getColorStateList(R.color.green, null));
        }
    }
}