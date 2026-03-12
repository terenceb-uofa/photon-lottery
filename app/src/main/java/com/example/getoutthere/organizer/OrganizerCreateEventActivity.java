package com.example.getoutthere.organizer;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.example.getoutthere.repositories.EventRepository;

import com.google.firebase.Timestamp;

import java.util.Locale;
import java.util.Calendar;


public class OrganizerCreateEventActivity extends AppCompatActivity {

    // Image Views
    private ImageView posterPreview;

    // Inputs
    private EditText nameInput;
    private EditText descriptionInput;
    private EditText addressInput;
    private EditText startDateInput;
    private EditText endDateInput;
    private EditText drawDateInput;
    private EditText registrationStartInput;
    private EditText registrationEndInput;
    private EditText capacityInput;
    private EditText feeInput;
    private EditText waitlistLimitInput;

    // Buttons

    private Button uploadPosterButton;
    private Button createEventButton;


    @Nullable
    private Uri selectedImageUri = null;

    private EventRepository eventRepository;


    // Timestamps
    private Timestamp startDateTimestamp;
    private Timestamp endDateTimestamp;
    private Timestamp drawDateTimestamp;
    private Timestamp registrationStartTimestamp;
    private Timestamp registrationEndTimestamp;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    posterPreview.setImageURI(uri);
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_create_event);

        eventRepository = new EventRepository();

        posterPreview = findViewById(R.id.posterPreview);

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

        uploadPosterButton = findViewById(R.id.uploadPosterButton);
        createEventButton = findViewById(R.id.createEventButton);

        // date pickers
        startDateInput.setKeyListener(null);
        endDateInput.setKeyListener(null);
        drawDateInput.setKeyListener(null);
        registrationStartInput.setKeyListener(null);
        registrationEndInput.setKeyListener(null);

        startDateInput.setOnClickListener(v ->
                showDateTimePicker(startDateInput, timestamp -> startDateTimestamp = timestamp));

        endDateInput.setOnClickListener(v ->
                showDateTimePicker(endDateInput, timestamp -> endDateTimestamp = timestamp));

        drawDateInput.setOnClickListener(v ->
                showDateTimePicker(drawDateInput, timestamp -> drawDateTimestamp = timestamp));

        registrationStartInput.setOnClickListener(v ->
                showDateTimePicker(registrationStartInput, timestamp -> registrationStartTimestamp = timestamp));

        registrationEndInput.setOnClickListener(v ->
                showDateTimePicker(registrationEndInput, timestamp -> registrationEndTimestamp = timestamp));

        uploadPosterButton.setOnClickListener(v ->
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );


        uploadPosterButton.setOnClickListener(v ->
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );

        createEventButton.setOnClickListener(v -> saveEvent());
    }


    private void saveEvent() {
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();
        String drawDate = drawDateInput.getText().toString().trim();
        String registrationStart = registrationStartInput.getText().toString().trim();
        String registrationEnd = registrationEndInput.getText().toString().trim();
        String capacityText = capacityInput.getText().toString().trim();
        String feeText = feeInput.getText().toString().trim();
        String waitlistLimitText = waitlistLimitInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Event name is required");
            nameInput.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            descriptionInput.setError("Description is required");
            descriptionInput.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            addressInput.setError("Address is required");
            addressInput.requestFocus();
            return;
        }

        if (startDate.isEmpty()) {
            startDateInput.setError("Start date is required");
            startDateInput.requestFocus();
            return;
        }

        if (endDate.isEmpty()) {
            endDateInput.setError("End date is required");
            endDateInput.requestFocus();
            return;
        }

        if (drawDate.isEmpty()) {
            drawDateInput.setError("Draw date is required");
            drawDateInput.requestFocus();
            return;
        }

        if (registrationStart.isEmpty()) {
            registrationStartInput.setError("Registration start is required");
            registrationStartInput.requestFocus();
            return;
        }

        if (registrationEnd.isEmpty()) {
            registrationEndInput.setError("Registration end is required");
            registrationEndInput.requestFocus();
            return;
        }

        if (capacityText.isEmpty()) {
            capacityInput.setError("Capacity is required");
            capacityInput.requestFocus();
            return;
        }

        if (feeText.isEmpty()) {
            feeInput.setError("Fee is required");
            feeInput.requestFocus();
            return;
        }

        int capacity;
        double signupFee;
        Integer waitlistLimit = null;

        try {
            capacity = Integer.parseInt(capacityText);
            if (capacity < 1) {
                capacityInput.setError("Capacity must be at least 1");
                capacityInput.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            capacityInput.setError("Enter a valid whole number");
            capacityInput.requestFocus();
            return;
        }

        try {
            signupFee = Double.parseDouble(feeText);
            if (signupFee < 0) {
                feeInput.setError("Fee cannot be negative");
                feeInput.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            feeInput.setError("Enter a valid fee");
            feeInput.requestFocus();
            return;
        }

        if (!waitlistLimitText.isEmpty()) {
            try {
                waitlistLimit = Integer.parseInt(waitlistLimitText);
                if (waitlistLimit < 1) {
                    waitlistLimitInput.setError("Waitlist limit must be at least 1");
                    waitlistLimitInput.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                waitlistLimitInput.setError("Enter a valid whole number");
                waitlistLimitInput.requestFocus();
                return;
            }
        }

        Event event = new Event();
        event.setName(name);
        event.setOrganizerId(getCurrentUserId());
        event.setDescription(description);
        event.setAddress(address);
        event.setStartDate(startDateTimestamp);
        event.setEndDate(endDateTimestamp);
        event.setDrawDate(drawDateTimestamp);
        event.setRegistrationStart(registrationStartTimestamp);
        event.setRegistrationEnd(registrationEndTimestamp);
        event.setCapacity(capacity);
        event.setSignupFee(signupFee);
        event.setWaitlistLimit(waitlistLimit);

        setSavingState(true);

        eventRepository.createEvent(event, selectedImageUri, new EventRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String eventId) {
                runOnUiThread(() -> {
                    Toast.makeText(OrganizerCreateEventActivity.this,
                            "Event created successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    setSavingState(false);
                    Toast.makeText(OrganizerCreateEventActivity.this,
                            "Failed to create event: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setSavingState(boolean isSaving) {
        createEventButton.setEnabled(!isSaving);
        uploadPosterButton.setEnabled(!isSaving);
        createEventButton.setText(isSaving ? "Saving..." : "Create Event");
    }


    private interface TimestampSelectionListener {
        void onTimestampSelected(Timestamp timestamp);
    }


    private void showDateTimePicker(EditText targetInput, TimestampSelectionListener listener) {
        Calendar calendar = Calendar.getInstance();

        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                Calendar selectedCalendar = Calendar.getInstance();
                                selectedCalendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                selectedCalendar.set(Calendar.MILLISECOND, 0);

                                Timestamp timestamp = new Timestamp(selectedCalendar.getTime());

                                String formattedDateTime = String.format(
                                        Locale.getDefault(),
                                        "%04d-%02d-%02d %02d:%02d",
                                        year,
                                        month + 1,
                                        dayOfMonth,
                                        hourOfDay,
                                        minute
                                );

                                targetInput.setText(formattedDateTime);
                                listener.onTimestampSelected(timestamp);
                            },
                            currentHour,
                            currentMinute,
                            false
                    );

                    timePickerDialog.show();
                },
                currentYear,
                currentMonth,
                currentDay
        );

        datePickerDialog.show();
    }

    private String getCurrentUserId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }


}
