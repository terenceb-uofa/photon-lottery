package com.example.getoutthere.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.example.getoutthere.repositories.EventRepository;
import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.Locale;

public class OrganizerEventDetailsActivity extends AppCompatActivity {

    private TextView nameInput;
    private TextView descriptionInput;
    private TextView addressInput;
    private TextView startDateInput;
    private TextView endDateInput;
    private TextView drawDateInput;
    private TextView registrationStartInput;
    private TextView registrationEndInput;
    private TextView capacityInput;
    private TextView feeInput;
    private TextView waitlistLimitInput;

    private Button backButton;
    private Button editButton;
    private Button buttonQRCode;
    private Button buttonViewWaitlist;

    private ImageView posterPreview;

    private EventRepository eventRepository;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_event_details);

        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        buttonQRCode = findViewById(R.id.buttonQRCode);
        buttonViewWaitlist = findViewById(R.id.buttonViewWaitlist);

        nameInput = findViewById(R.id.nameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        addressInput = findViewById(R.id.addressInput);
        startDateInput = findViewById(R.id.startDateInput);
        endDateInput = findViewById(R.id.endDateInput);
        drawDateInput = findViewById(R.id.drawDateInput);
        registrationStartInput = findViewById(R.id.registrationStartInput);
        registrationEndInput = findViewById(R.id.registrationEndInput);
        capacityInput = findViewById(R.id.capacityInput);
        feeInput = findViewById(R.id.feeInput);
        waitlistLimitInput = findViewById(R.id.waitlistLimitInput);

        posterPreview = findViewById(R.id.posterPreview);

        eventRepository = new EventRepository();
        eventId = getIntent().getStringExtra("eventId");

        backButton.setOnClickListener(v -> finish());

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerEventDetailsActivity.this, OrganizerEditEventActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        buttonQRCode.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerEventDetailsActivity.this, EventQrCodeActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

//        buttonViewWaitlist.setOnClickListener(v -> {
//            Intent intent = new Intent(OrganizerEventDetailsActivity.this, OrganizerWaitlistActivity.class);
//            intent.putExtra("eventId", eventId);
//            startActivity(intent);
//        });

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEvent();
    }

    private void loadEvent() {
        eventRepository.getEventById(eventId, new EventRepository.RepositoryCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                runOnUiThread(() -> populateEventDetails(event));
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(OrganizerEventDetailsActivity.this,
                            "Failed to load event",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void populateEventDetails(Event event) {
        nameInput.setText(event.getName() == null ? "" : event.getName());
        descriptionInput.setText("Description: " + (event.getDescription() == null ? "" : event.getDescription()));
        addressInput.setText("Address: " + (event.getAddress() == null ? "" : event.getAddress()));
        startDateInput.setText("Start Date: " + formatTimestamp(event.getStartDate()));
        endDateInput.setText("End Date: " + formatTimestamp(event.getEndDate()));
        drawDateInput.setText("Draw Date: " + formatTimestamp(event.getDrawDate()));
        registrationStartInput.setText("Registration Start: " + formatTimestamp(event.getRegistrationStart()));
        registrationEndInput.setText("Registration End: " + formatTimestamp(event.getRegistrationEnd()));
        capacityInput.setText("Capacity: " + event.getCapacity());
        feeInput.setText("Signup Fee: " + event.getSignupFee());
        waitlistLimitInput.setText(
                "Waitlist Limit: " + (event.getWaitlistLimit() == null ? "None" : event.getWaitlistLimit())
        );

        String posterUrl = event.getPosterUrl();
        if (!TextUtils.isEmpty(posterUrl)) {
            Glide.with(this)
                    .load(posterUrl)
                    .into(posterPreview);
        } else {
            posterPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }



    private String formatTimestamp(Timestamp timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp.toDate());

        return String.format(
                Locale.getDefault(),
                "%04d-%02d-%02d %02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
        );
    }

    // update event when edited
    @Override
    protected void onResume() {
        super.onResume();

        if (eventId != null && !eventId.isEmpty()) {
            loadEvent();
        }
    }
}