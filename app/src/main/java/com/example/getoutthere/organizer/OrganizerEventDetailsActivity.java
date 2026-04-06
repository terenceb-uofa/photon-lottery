package com.example.getoutthere.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.example.getoutthere.event.EventDetailsActivity;
import com.example.getoutthere.models.EntrantProfile;
import com.example.getoutthere.repositories.EventRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private Button buttonManageComments;


    private TextView tvPrimaryOrganizer;
    private LinearLayout layoutCoOrganizers;
    private TextView labelCoOrganizers;

    private EntrantProfile entrant;

    // Event poster image
    private ImageView posterPreview;

    // Repository for fetching event data
    private EventRepository eventRepository;

    // Event ID passed from OrganizerEventListActivity
    private String eventId;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


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

        // Team displays
        tvPrimaryOrganizer = findViewById(R.id.tvPrimaryOrganizer);
        layoutCoOrganizers = findViewById(R.id.layoutCoOrganizers);
        labelCoOrganizers = findViewById(R.id.labelCoOrganizers);

        // Entrant info
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        entrant = new EntrantProfile();
        entrant.setDeviceId(deviceId);

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

        // The following code is from Anthropic, Claude, "Check Firestore eventVisibility before navigating to QR code screen", 2026-04-05
        // Navigate to QR code screen only if event is Public
        buttonQRCode.setOnClickListener(v -> {
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(doc -> {
                        String visibility = doc.getString("eventVisibility");
                        if ("Public".equalsIgnoreCase(visibility)) {
                            Intent intent = new Intent(OrganizerEventDetailsActivity.this, EventQrCodeActivity.class);
                            intent.putExtra("eventId", eventId);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Event is Private. No promotional QR code exists.", Toast.LENGTH_SHORT).show();
                        }
                    });
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

        // View comments fragment button
        // The following code is from Anthropic, Claude, "How do I display comments from every event's collection in a popup dialog", 2026-04-03
        buttonManageComments = findViewById(R.id.buttonManageComments);
        buttonManageComments.setOnClickListener(v -> {

            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Event Comments");

            View dialogView = getLayoutInflater().inflate(R.layout.modal_event_comments, null);
            builder.setView(dialogView);
            builder.setPositiveButton("Close", null);

            ScrollView scrollView = dialogView.findViewById(R.id.commentsScrollView);
            LinearLayout commentsListLayout = dialogView.findViewById(R.id.commentsListLayout);
            com.google.android.material.textfield.TextInputEditText commentInput =
                    dialogView.findViewById(R.id.commentInput);
            com.google.android.material.button.MaterialButton sendButton =
                    dialogView.findViewById(R.id.sendButton);

            androidx.appcompat.app.AlertDialog dialog = builder.create();


            com.google.firebase.firestore.ListenerRegistration listener = FirebaseFirestore.getInstance()
                    .collection("events")
                    .document(eventId)
                    .collection("comments")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null || snapshots == null) return;

                        commentsListLayout.removeAllViews();

                        if (snapshots.isEmpty()) {
                            TextView tv = new TextView(this);
                            tv.setText("No comments yet.");
                            tv.setTextColor(0xFF888888);
                            commentsListLayout.addView(tv);
                        }

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            com.example.getoutthere.models.Comment comment =
                                    doc.toObject(com.example.getoutthere.models.Comment.class);

                            if (comment != null) {
                                LinearLayout commentRow = new LinearLayout(this);
                                commentRow.setOrientation(LinearLayout.HORIZONTAL);
                                commentRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

                                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                rowParams.setMargins(0, 0, 0, 12);
                                commentRow.setLayoutParams(rowParams);

                                TextView tv = new TextView(this);
                                android.text.SpannableString formattedText = new android.text.SpannableString(
                                        comment.getEntrantName() + ": " + comment.getContent()
                                );
                                formattedText.setSpan(
                                        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                        0,
                                        comment.getEntrantName().length() + 1,
                                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                );
                                tv.setText(formattedText);
                                tv.setTextSize(16f);
                                tv.setTextColor(getResources().getColor(android.R.color.white));
                                tv.setPadding(24, 20, 24, 20);
                                tv.setBackgroundResource(R.drawable.bg_nav_glass);

                                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1f
                                );
                                textParams.setMargins(0, 0, 12, 0);
                                tv.setLayoutParams(textParams);

                                com.google.android.material.button.MaterialButton commentDeleteButton =
                                        new com.google.android.material.button.MaterialButton(this);

                                int size = (int) (40 * getResources().getDisplayMetrics().density);

                                LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(size, size);
                                deleteParams.setMargins(12, 0, 0, 0);
                                commentDeleteButton.setLayoutParams(deleteParams);

                                commentDeleteButton.setMinWidth(0);
                                commentDeleteButton.setMinHeight(0);
                                commentDeleteButton.setMinimumWidth(0);
                                commentDeleteButton.setMinimumHeight(0);

                                commentDeleteButton.setInsetTop(0);
                                commentDeleteButton.setInsetBottom(0);
                                commentDeleteButton.setPadding(0, 0, 0, 0);

                                commentDeleteButton.setCornerRadius(size / 2);

                                commentDeleteButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                commentDeleteButton.setGravity(android.view.Gravity.CENTER);

                                commentDeleteButton.setText("");
                                commentDeleteButton.setContentDescription("Delete comment");

                                commentDeleteButton.setIconResource(R.drawable.ic_delete);
                                commentDeleteButton.setIconTintResource(R.color.error);
                                commentDeleteButton.setIconSize((int) (20 * getResources().getDisplayMetrics().density));
                                commentDeleteButton.setIconPadding(0);
                                commentDeleteButton.setIconGravity(
                                        com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START
                                );

                                commentDeleteButton.setBackgroundTintList(
                                        androidx.core.content.ContextCompat.getColorStateList(this, R.color.navBackground)
                                );
                                commentDeleteButton.setStrokeColor(
                                        androidx.core.content.ContextCompat.getColorStateList(this, R.color.greyAccent)
                                );
                                commentDeleteButton.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));

                                String commentId = doc.getId();
                                commentDeleteButton.setOnClickListener(del -> {
                                    androidx.appcompat.app.AlertDialog confirmDialog =
                                            new androidx.appcompat.app.AlertDialog.Builder(this)
                                                    .setTitle("Delete Comment")
                                                    .setMessage("Are you sure you want to delete this comment by " + comment.getEntrantName() + "?")
                                                    .setPositiveButton("Delete", (dialogInterface, which) -> {
                                                        FirebaseFirestore.getInstance()
                                                                .collection("events")
                                                                .document(eventId)
                                                                .collection("comments")
                                                                .document(commentId)
                                                                .delete()
                                                                .addOnFailureListener(err ->
                                                                        Toast.makeText(this, "Failed to delete comment", Toast.LENGTH_SHORT).show()
                                                                );
                                                    })
                                                    .setNegativeButton("Cancel", null)
                                                    .create();

                                    confirmDialog.show();

                                    if (confirmDialog.getWindow() != null) {
                                        confirmDialog.getWindow().setBackgroundDrawableResource(R.color.sepBackground);
                                    }

                                    confirmDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                                            .setTextColor(getResources().getColor(R.color.error));

                                    confirmDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                                            .setTextColor(getResources().getColor(R.color.white));
                                });

                                commentRow.addView(tv);
                                commentRow.addView(commentDeleteButton);
                                commentsListLayout.addView(commentRow);
                            }
                        }

                        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                    });

            sendButton.setOnClickListener(view -> {
                String text = commentInput.getText() == null
                        ? ""
                        : commentInput.getText().toString().trim();

                if (text.isEmpty()) return;

                sendButton.setEnabled(false);

                db.collection("profiles").document(entrant.getDeviceId()).get().addOnSuccessListener(doc -> {
                    String userName = doc.exists()
                            ? "[ORGANIZER] " + doc.getString("name")
                            : "Anonymous User";

                    com.example.getoutthere.models.Comment newComment =
                            new com.example.getoutthere.models.Comment(
                                    entrant.getDeviceId(),
                                    userName,
                                    text,
                                    Timestamp.now()
                            );

                    db.collection("events").document(eventId).collection("comments")
                            .add(newComment)
                            .addOnSuccessListener(docRef -> {
                                commentInput.setText("");
                                sendButton.setEnabled(true);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                                sendButton.setEnabled(true);
                            });
                });
            });

            dialog.setOnDismissListener(d -> listener.remove());
            dialog.show();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        });
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

        //Display primary organizer name and co-organizers
        if (eventId != null) {
            EventDetailsActivity.displayTeamData(this,event,tvPrimaryOrganizer, labelCoOrganizers,layoutCoOrganizers);
        }


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