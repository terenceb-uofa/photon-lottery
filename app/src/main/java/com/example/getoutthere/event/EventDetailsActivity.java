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
import com.example.getoutthere.utils.FirebaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView eventName, eventAddress, eventDate, eventCapacity, eventFee;
    private Button btnJoin, btnLeave;

    private String eventId;
    private Event event;
    private EntrantProfile entrant;

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

        btnJoin = findViewById(R.id.btnJoinWaitingList);
        btnLeave = findViewById(R.id.btnLeaveWaitingList);

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
            }
        });

        // Join waiting list
        btnJoin.setOnClickListener(v -> {
            if (event != null) {
                db.collection("events")
                        .document(event.getId())
                        .collection("waitingList")
                        .document(entrant.getDeviceId())
                        .set(new java.util.HashMap<String, Object>() {{
                            put("name", entrant.getName());
                            put("email", entrant.getEmail());
                            put("phone", entrant.getPhoneNumber());
                        }})
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(EventDetailsActivity.this, "Joined waiting list!", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(EventDetailsActivity.this, "Failed to join waiting list", Toast.LENGTH_SHORT).show()
                        );
            }
        });

        // Leave waiting list
        btnLeave.setOnClickListener(v -> {
            if (event != null) {
                db.collection("events")
                        .document(event.getId())
                        .collection("waitingList")
                        .document(entrant.getDeviceId())
                        .delete()
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(EventDetailsActivity.this, "Left waiting list!", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(EventDetailsActivity.this, "Failed to leave waiting list", Toast.LENGTH_SHORT).show()
                        );
            }
        });
    }
}