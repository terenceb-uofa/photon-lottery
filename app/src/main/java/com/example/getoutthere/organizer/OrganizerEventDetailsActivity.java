package com.example.getoutthere.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.example.getoutthere.repositories.EventRepository;
import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Activity for organizer to view details of a specific event.
 * Displays event information such as name, dates, capacity, and poster.
 * Provides navigation to edit the event, view its QR code, and manage entrants.
 */
public class OrganizerEventDetailsActivity extends AppCompatActivity {

    // UI elements for displaying event details
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

    // Buttons for navigation
    private FrameLayout backButton;
    private Button editButton;
    private Button buttonQRCode;
    private Button buttonManageWaitlist;

    // Event poster image
    private ImageView posterPreview;

    // Repository for fetching event data
    private EventRepository eventRepository;

    // Event ID passed from OrganizerEventListActivity
    private String eventId;


    /**
     * Initializes the activity, connects the input fields, image preview, and buttons
     * to the layout, sets up navigation actions for the back, edit, QR code, and
     * waitlist buttons, retrieves the event ID from the intent, validates it,
     * and loads the event information for display.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     *                           shut down then this Bundle contains the data it most
     *                           recently supplied. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_event_details);

        // Initialize UI elements
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        buttonQRCode = findViewById(R.id.buttonQRCode);
        buttonManageWaitlist = findViewById(R.id.buttonManageWaitlist);

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

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Navigate to edit event screen
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerEventDetailsActivity.this, OrganizerEditEventActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        // Navigate to QR code screen
        buttonQRCode.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerEventDetailsActivity.this, EventQrCodeActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        buttonManageWaitlist.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerEventDetailsActivity.this, OrganizerWaitlistActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        });

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEvent();
    }

    /**
     * Fetches the event data from Firestore and populates the UI.
     */
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

    /**
     * Populates the UI with the event details.
     *
     * @param event The event object containing all event details.
     */
    private void populateEventDetails(Event event) {
        nameInput.setText(event.getName() == null ? "" : event.getName());
        descriptionInput.setText((event.getDescription() == null ? "" : event.getDescription()));
        addressInput.setText((event.getAddress() == null ? "" : event.getAddress()));
        startDateInput.setText(formatTimestamp(event.getStartDate()));
        endDateInput.setText(formatTimestamp(event.getEndDate()));
        drawDateInput.setText(formatTimestamp(event.getDrawDate()));
        registrationStartInput.setText(formatTimestamp(event.getRegistrationStart()));
        registrationEndInput.setText(formatTimestamp(event.getRegistrationEnd()));
        capacityInput.setText(String.valueOf(event.getCapacity()));
        if (event.getSignupFee() <= 0) {
            feeInput.setText("Free");
        } else {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
            feeInput.setText(currencyFormat.format(event.getSignupFee()));
        }
        waitlistLimitInput.setText(event.getWaitlistLimit() == null ? "None" : String.valueOf(event.getWaitlistLimit()));
        String posterUrl = event.getPosterUrl();
        if (!TextUtils.isEmpty(posterUrl)) {
            Glide.with(this)
                    .load(posterUrl)
                    .into(posterPreview);
        } else {
            posterPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    /**
     * Formats a Firestore Timestamp into a readable date string.
     *
     * @param timestamp The Firestore Timestamp to format.
     * @return A formatted date string in the format "YYYY-MM-DD HH:MM".
     */
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

    /**
     * Reloads the event details when the activity resumes.
     * Ensures the UI is up to date after editing.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (eventId != null && !eventId.isEmpty()) {
            loadEvent();
        }
    }
}