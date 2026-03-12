package com.example.getoutthere.event;

import android.content.Intent;
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

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class EventDetailsActivity extends AppCompatActivity {

    private TextView eventName, eventAddress, eventDateRange, eventCapacity, eventFee, eventDrawDate;
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
        eventDateRange = findViewById(R.id.EventDateRange);
        eventCapacity = findViewById(R.id.EventCapacity);
        eventFee = findViewById(R.id.EventSignupFee);
        eventDrawDate = findViewById(R.id.EventDrawDate);
        btnToggleWaitingList = findViewById(R.id.btnToggleWaitingList);
        btnBack = findViewById(R.id.EventDetailsBackButton);

        // Device ID for entrant
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        entrant = new EntrantProfile();
        entrant.setDeviceId(deviceId);

        // Event ID from intent
        eventId = getIntent().getStringExtra("eventId");

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Fetch event from Firestore
        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                event = documentSnapshot.toObject(Event.class);
                event.setId(documentSnapshot.getId());

                // Event name
                eventName.setText(event.getName());

                // Date & time range
                if (event.getStartDate() != null && event.getEndDate() != null) {
                    SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm a");
                    SimpleDateFormat dateFmt = new SimpleDateFormat("MM/dd");
                    String dateRange = timeFmt.format(event.getStartDate().toDate())
                            + "-" + timeFmt.format(event.getEndDate().toDate())
                            + " from " + dateFmt.format(event.getStartDate().toDate())
                            + "-" + dateFmt.format(event.getEndDate().toDate());
                    eventDateRange.setText(dateRange);
                }

                // Address
                eventAddress.setText(event.getAddress());

                // Signup fee
                eventFee.setText("Signup fee: $" + event.getSignupFee());

                // Spots available
                eventCapacity.setText(event.getCurrentWaitlistCount() + "/" + event.getCapacity() + " spots available");

                // Draw date
                if (event.getDrawDate() != null) {
                    String drawStr = new SimpleDateFormat("MM/dd/yyyy").format(event.getDrawDate().toDate());
                    eventDrawDate.setText("Draws on " + drawStr);
                }

                // Check if user is on waiting list
                db.collection("events")
                        .document(eventId)
                        .collection("waitingList")
                        .document(entrant.getDeviceId())
                        .get()
                        .addOnSuccessListener(doc -> {
                            isOnWaitingList = doc.exists();
                            updateToggleButton();
                        });
            }
        });

        // Toggle waiting list
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
                        .addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Failed to join waiting list", Toast.LENGTH_SHORT).show());
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
                        .addOnFailureListener(e -> Toast.makeText(EventDetailsActivity.this, "Failed to leave waiting list", Toast.LENGTH_SHORT).show());
            }
        });
    }

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