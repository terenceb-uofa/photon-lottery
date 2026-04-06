package com.example.getoutthere.organizer;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.example.getoutthere.repositories.EventRepository;

import com.google.firebase.Timestamp;

import java.util.Locale;
import java.util.Calendar;

/**
 * Allows organizers to edit an existing event.
 * <p>
 * The code below is based on OrganizerCreateEventActivity.java
 * This activity loads an existing event, displays its information in editable
 * input fields, allows the organizer to update the event details and poster,
 * and saves the updated event to Firebase.
 * <p>
 * Outstanding Issues:
 * - None
 *
 * @author Yousaf Cheema
 * @version 1.0
 */
public class OrganizerEditEventActivity extends AppCompatActivity {

    private String eventId;
    private Event existingEvent;

    //Text Views
    private TextView screenTitle;

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

    private AutoCompleteTextView eventTypeInput;
    private AutoCompleteTextView eventVisibilityInput;

    // Buttons

    private Button uploadPosterButton;
    private Button createEventButton;

    private FrameLayout backButton;


    @Nullable
    private Uri selectedImageUri = null;

    private EventRepository eventRepository;


    // Timestamps
    private Timestamp startDateTimestamp;
    private Timestamp endDateTimestamp;
    private Timestamp drawDateTimestamp;
    private Timestamp registrationStartTimestamp;
    private Timestamp registrationEndTimestamp;

    /**
     * Launches the system image picker so the organizer can select a poster image.
     * If an image is selected, it is stored and previewed on the screen.
     * If no image is selected, a message is shown to the user.
     */
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    posterPreview.setImageURI(uri);
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            });


    /**
     * Initializes the activity, connects the input fields and buttons to the layout,
     * sets up the date and time pickers, retrieves the event ID, and loads the
     * event information for editing.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
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

        // The following code is from Anthropic, Claude, "Create Event Android XML layout with event type dropdown", 2026-04-01
        eventTypeInput = findViewById(R.id.eventTypeInput);
        ArrayAdapter<CharSequence> eventTypeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.event_types,
                android.R.layout.simple_dropdown_item_1line
        );
        eventTypeInput.setAdapter(eventTypeAdapter);

        eventVisibilityInput = findViewById(R.id.eventVisibilityInput);
        ArrayAdapter<CharSequence> eventVisibilityAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.visibility_types,
                android.R.layout.simple_dropdown_item_1line
        );
        eventVisibilityInput.setAdapter(eventVisibilityAdapter);

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


        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        createEventButton.setText("Save Changes");

        loadEventForEditing();

        createEventButton.setOnClickListener(v -> updateExistingEvent());

        FrameLayout backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        TextView screenTitle = findViewById(R.id.screenTitle);
        screenTitle.setText("Edit Event");
    }

    /**
     * Reads the updated event information from the input fields, validates the input,
     * creates an updated Event object, and saves the changes to Firebase.
     * If the update is successful, the activity closes.
     */
    private void updateExistingEvent() {
        if (existingEvent == null) {
            Toast.makeText(this, "Event not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String capacityText = capacityInput.getText().toString().trim();
        String feeText = feeInput.getText().toString().trim();
        String waitlistLimitText = waitlistLimitInput.getText().toString().trim();
        String eventType = eventTypeInput.getText().toString().trim();
        String eventVisibility = eventVisibilityInput.getText().toString().trim();

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

        if (startDateTimestamp == null) {
            startDateInput.setError("Start date is required");
            startDateInput.requestFocus();
            return;
        }

        if (endDateTimestamp == null) {
            endDateInput.setError("End date is required");
            endDateInput.requestFocus();
            return;
        }

        if (drawDateTimestamp == null) {
            drawDateInput.setError("Draw date is required");
            drawDateInput.requestFocus();
            return;
        }

        if (registrationStartTimestamp == null) {
            registrationStartInput.setError("Registration start is required");
            registrationStartInput.requestFocus();
            return;
        }

        if (registrationEndTimestamp == null) {
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

        if (eventType.isEmpty()) {
            eventTypeInput.setError("Event type is required");
            eventTypeInput.requestFocus();
            return;
        }

        if (eventVisibility.isEmpty()) {
            eventVisibilityInput.setError("Event visibility is required");
            eventVisibilityInput.requestFocus();
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

        Event updatedEvent = new Event();

        // keep existing input fields
        updatedEvent.setId(existingEvent.getId());
        updatedEvent.setOrganizerId(existingEvent.getOrganizerId());
        updatedEvent.setCurrentWaitlistCount(existingEvent.getCurrentWaitlistCount());

        updatedEvent.setQrCodeContent(existingEvent.getQrCodeContent());

        // update editable input fields
        updatedEvent.setName(name);
        updatedEvent.setDescription(description);
        updatedEvent.setAddress(address);
        updatedEvent.setStartDate(startDateTimestamp);
        updatedEvent.setEndDate(endDateTimestamp);
        updatedEvent.setDrawDate(drawDateTimestamp);
        updatedEvent.setRegistrationStart(registrationStartTimestamp);
        updatedEvent.setRegistrationEnd(registrationEndTimestamp);
        updatedEvent.setCapacity(capacity);
        updatedEvent.setSignupFee(signupFee);
        updatedEvent.setWaitlistLimit(waitlistLimit);
        updatedEvent.setEventType(eventType);
        updatedEvent.setEventVisibility(eventVisibility);

        if (selectedImageUri == null) {
            updatedEvent.setPosterUrl(existingEvent.getPosterUrl());
        }

        setSavingState(true);

        eventRepository.updateEvent(updatedEvent, selectedImageUri, new EventRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(OrganizerEditEventActivity.this,
                            "Event updated successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    setSavingState(false);
                    Toast.makeText(OrganizerEditEventActivity.this,
                            "Failed to update event: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Updates the screen while the event is being saved.
     * This disables or enables buttons and changes the button text.
     *
     * @param isSaving true if the event is currently being saved, false otherwise
     */
    private void setSavingState(boolean isSaving) {
        createEventButton.setEnabled(!isSaving);
        uploadPosterButton.setEnabled(!isSaving);
        createEventButton.setText(isSaving ? "Saving..." : "Save Changes");
    }

    /**
     * Listener interface used to return the Timestamp chosen in the date and time picker.
     */
    private interface TimestampSelectionListener {
        void onTimestampSelected(Timestamp timestamp);
    }

    /**
     * Opens a date picker followed by a time picker, then stores the selected
     * date and time as a Firebase Timestamp and displays it in the chosen input field.
     *
     * @param targetInput the input field that will display the selected date and time
     * @param listener listener used to return the selected Timestamp
     */

    // Used https://developer.android.com/develop/ui/views/components/pickers as a resource
    // Used https://www.geeksforgeeks.org/android/datepicker-in-android/ as a resource
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

    /**
     * Gets the current device ID to use as the organizer's user ID.
     *
     * @return the Android device ID for the current user
     */
    private String getCurrentUserId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Fetches the event from Firebase using the event ID and fills the input fields
     * with the existing event information so it can be edited.
     */
    private void loadEventForEditing() {
        eventRepository.getEventById(eventId, new EventRepository.RepositoryCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                runOnUiThread(() -> {
                    existingEvent = event;

                    nameInput.setText(event.getName());
                    descriptionInput.setText(event.getDescription());
                    addressInput.setText(event.getAddress());

                    if (event.getStartDate() != null) {
                        startDateTimestamp = event.getStartDate();
                        startDateInput.setText(formatTimestamp(event.getStartDate()));
                    }

                    if (event.getEndDate() != null) {
                        endDateTimestamp = event.getEndDate();
                        endDateInput.setText(formatTimestamp(event.getEndDate()));
                    }

                    if (event.getDrawDate() != null) {
                        drawDateTimestamp = event.getDrawDate();
                        drawDateInput.setText(formatTimestamp(event.getDrawDate()));
                    }

                    if (event.getRegistrationStart() != null) {
                        registrationStartTimestamp = event.getRegistrationStart();
                        registrationStartInput.setText(formatTimestamp(event.getRegistrationStart()));
                    }

                    if (event.getRegistrationEnd() != null) {
                        registrationEndTimestamp = event.getRegistrationEnd();
                        registrationEndInput.setText(formatTimestamp(event.getRegistrationEnd()));
                    }

                    capacityInput.setText(String.valueOf(event.getCapacity()));
                    feeInput.setText(String.valueOf(event.getSignupFee()));

                    if (event.getWaitlistLimit() != null) {
                        waitlistLimitInput.setText(String.valueOf(event.getWaitlistLimit()));
                    }

                    if (event.getEventType() != null) {
                        eventTypeInput.setText(event.getEventType(), false);
                    }

                    if (event.getEventVisibility() != null) {
                        eventVisibilityInput.setText(event.getEventVisibility(), false);
                    }


                    String posterUrl = event.getPosterUrl();
                    if (!TextUtils.isEmpty(posterUrl)) {
                        Glide.with(OrganizerEditEventActivity.this)
                                .load(posterUrl)
                                .into(posterPreview);
                    } else {
                        posterPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                    }

                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(OrganizerEditEventActivity.this,
                            "Failed to load event: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }


    /**
     * Converts a Firebase Timestamp into a formatted date and time string
     * so it can be displayed in the input fields.
     *
     * @param timestamp the timestamp to format
     * @return the formatted date and time string
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

}
