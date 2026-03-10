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

import com.example.getoutthere.models.EntrantProfile;
import com.example.getoutthere.utils.FirebaseHelper;
import com.example.getoutthere.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

// Shows the details of a single event - name ,description, etc.
// Has functionality to join and leave waiting list.

public class EventDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvDescription, tvDate, tvCapacity;
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
        tvName = findViewById(R.id.tvEventName);
        tvDescription = findViewById(R.id.tvEventDescription);
        tvDate = findViewById(R.id.tvEventDate);
        tvCapacity = findViewById(R.id.tvEventCapacity);
        btnJoin = findViewById(R.id.btnJoinWaitingList);
        btnLeave = findViewById(R.id.btnLeaveWaitingList);

        // Get eventId from intent
        eventId = getIntent().getStringExtra("eventId");

        // Get entrant info
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        entrant = new EntrantProfile();
        entrant.setDeviceId(deviceId);

        // Fetch event from Firestore
        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                event = documentSnapshot.toObject(Event.class);
                event.setId(documentSnapshot.getId());
                tvName.setText(event.getName());
                tvDescription.setText(event.getDescription());
                tvDate.setText(event.getDate());
                tvCapacity.setText("Capacity: " + event.getCapacity());
            }
        });

        // Join button
        btnJoin.setOnClickListener(v -> {
            FirebaseHelper.joinWaitingList(event, entrant);
            Toast.makeText(this, "Joined waiting list!", Toast.LENGTH_SHORT).show();
        });

        // Leave button
        btnLeave.setOnClickListener(v -> {
            FirebaseHelper.leaveWaitingList(event, entrant);
            Toast.makeText(this, "Left waiting list!", Toast.LENGTH_SHORT).show();
        });
    }
}
