package com.example.getoutthere.event;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;
import com.example.getoutthere.models.EntrantProfile;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * Displays detailed information about a specific event.
 * <p>
 * This Activity serves as a Control and View class that pulls specific event data
 * from Firestore and populates the UI. It handles the core logic for allowing
 * users to join or leave the event's waiting list, updating both the local UI state
 * and the remote Firestore database atomically.
 * <p>
 * Outstanding Issues:
 * - Direct database access is performed within the UI layer, which loosely violates
 * MVC/MVVM separation of concerns. This could be refactored into a Repository.
 * - The logic allows joining a waitlist but does not yet implement the final lottery selection.
 */
public class EventDetailsActivity extends AppCompatActivity {
    private TextView eventName, eventAddress, eventDateRange, eventCapacity, eventFee, eventDrawDate, eventDescription, eventType;
    private Button btnToggleWaitingList, btnViewComments;
    private TextView tvPrimaryOrganizer;

    private LinearLayout layoutCoOrganizers;
    private TextView labelCoOrganizers;
    private FrameLayout backButton;

    private String eventId;
    Event event;
    private EntrantProfile entrant;

    boolean isOnWaitingList = false;
    boolean isAOrganizer = false;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Initializes the Activity, fetches the Entrant's device ID, parses the event ID
     * passed via Intent, and queries Firestore to populate the UI. Sets up listeners
     * for joining/leaving the waitlist.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);

        // Handle window insets
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
        eventDescription = findViewById(R.id.EventDescription);
        eventType = findViewById(R.id.EventType);
        btnToggleWaitingList = findViewById(R.id.btnToggleWaitingList);

        // Entrant info
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        entrant = new EntrantProfile();
        entrant.setDeviceId(deviceId);

        // Event ID from intent
        eventId = getIntent().getStringExtra("eventId");

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        //  Lottery Info link
        Button btnLotteryInfo = findViewById(R.id.btnLotteryInfo);
        btnLotteryInfo.setOnClickListener(v -> showLotteryCriteriaDialog());

        // Fetch event from Firestore
        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {


                event = documentSnapshot.toObject(Event.class);
                event.setId(documentSnapshot.getId());

                // Set UI
                eventName.setText(event.getName());
                eventDescription.setText(event.getDescription());
                eventType.setText("Type: " + event.getEventType());
                eventAddress.setText(event.getAddress());
                eventFee.setText("Signup fee: $" + event.getSignupFee());

                // Check if the event has a poster
                ImageView eventPoster = findViewById(R.id.EventPoster);
                if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                    Glide.with(EventDetailsActivity.this)
                            .load(event.getPosterUrl())
                            .into(eventPoster);
                }

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

                // Draw date
                if (event.getDrawDate() != null) {
                    String drawStr = new SimpleDateFormat("MM/dd/yyyy").format(event.getDrawDate().toDate());
                    eventDrawDate.setText("Draws on " + drawStr);
                }


                //Display primary organizer name
                if (eventId != null) {
                 displayTeamData(this,event,tvPrimaryOrganizer, labelCoOrganizers,layoutCoOrganizers);
                }

                // Spots available
                updateSpotsUI();

                // Check if user is already on waiting list
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

        // Toggle waiting list button
        btnToggleWaitingList.setOnClickListener(v -> {
            if (event == null) return;

            if (!isOnWaitingList) {

                // Check registration period before allowing join
                if (!isRegistrationOpen()) {
                    return;
                }

                // If user is an organizer, user cannot join waiting list
                if (Objects.equals(event.getOrganizerId(), deviceId) ||
                        (event.getCoOrganizerIds() != null && event.getCoOrganizerIds().contains(deviceId))) {
                    Toast.makeText(EventDetailsActivity.this,
                            "Organizers/Co-organizers cannot join waiting lists for their own events",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // If waitlist is full, user cannot join
                if (event.getWaitlistLimit() != null && event.getCurrentWaitlistCount() >= event.getWaitlistLimit()) {
                    Toast.makeText(EventDetailsActivity.this, "Waitlist is full! Cannot join waiting list.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // CHECK GEOLOCATION REQUIREMENT
                if (event.isRequiresGeolocation()) {
                    fetchLocationAndJoin();
                } else {
                    joinWaitingList(null, null); // Join normally without location
                }

            } else {
                // Leave waiting list
                db.collection("events")
                        .document(event.getId())
                        .collection("waitingList")
                        .document(entrant.getDeviceId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            isOnWaitingList = false;

                            db.collection("events")
                                    .document(event.getId())
                                    .update("currentWaitlistCount", FieldValue.increment(-1))
                                    .addOnSuccessListener(unused -> {
                                        int newCount = Math.max(0, event.getCurrentWaitlistCount() - 1);
                                        event.setCurrentWaitlistCount(newCount);
                                        updateSpotsUI();
                                        updateToggleButton();
                                        Toast.makeText(EventDetailsActivity.this, "Left waiting list!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        isOnWaitingList = true;
                                        Toast.makeText(EventDetailsActivity.this, "Failed to update waitlist count", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(EventDetailsActivity.this, "Failed to leave waiting list", Toast.LENGTH_SHORT).show());
            }
        });

        // View comments fragment button
        btnViewComments = findViewById(R.id.btnViewComments);
        btnViewComments.setOnClickListener(v -> showCommentsDialog());
    


        // Team displays
        tvPrimaryOrganizer = findViewById(R.id.tvPrimaryOrganizer);
        layoutCoOrganizers = findViewById(R.id.layoutCoOrganizers);
        labelCoOrganizers = findViewById(R.id.labelCoOrganizers);
    }

    /**
     * Calculates the remaining spots available by subtracting the current waitlist
     * count from the Waitlist capacity limit, and updates the UI TextView accordingly.
     */
    void updateSpotsUI() {
        if (event.getWaitlistLimit() != null) {
            int spotsAvailable = event.getWaitlistLimit() - event.getCurrentWaitlistCount();
            eventCapacity.setText("Available spots: " + spotsAvailable + "/" + event.getWaitlistLimit());
        } else {
            eventCapacity.setText("There is no waitlist limit for this event.");
        }
    }

    /**
     * Checks whether the current time falls within the event's registration period.
     *
     * @return true if registration is currently open, false otherwise
     */
    private boolean isRegistrationOpen() {
        Timestamp now = Timestamp.now();

        if (event.getRegistrationStart() != null &&
                now.compareTo(event.getRegistrationStart()) < 0) {
            Toast.makeText(this, "Registration has not opened yet.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (event.getRegistrationEnd() != null &&
                now.compareTo(event.getRegistrationEnd()) > 0) {
            Toast.makeText(this, "Registration period has ended.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Displays an info about the lottery selection criteria
     */
    private void showLotteryCriteriaDialog() {
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Lottery Guidelines")
                .setMessage("Selection for this event is processed via a randomized lottery system.\n\n" +
                        "• Joining the waitlist does not guarantee entry.\n" +
                        "• When the draw date occurs, entrants are selected entirely at random up to the event's capacity limit.\n" +
                        "• If selected, you will receive a notification to finalize your enrollment.")
                .setPositiveButton("Understood", null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_nav_glass);
        }

        TextView titleView = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
        if (titleView != null) {
            titleView.setTextColor(getResources().getColor(R.color.white));
        }

        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setTextColor(getResources().getColor(R.color.white));
        }

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getResources().getColor(R.color.white));
    }


    /**
     * Toggles the UI state of the waitlist button, changing its text and color
     * depending on whether the current user is already on the waiting list.
     */
     void updateToggleButton() {
        if (isOnWaitingList) {
            btnToggleWaitingList.setText("Leave Waitlist");
            btnToggleWaitingList.setBackgroundTintList(getResources().getColorStateList(R.color.error, null));
        } else {
            btnToggleWaitingList.setText("Join Waitlist");
            btnToggleWaitingList.setBackgroundTintList(getResources().getColorStateList(R.color.accent, null));
        }
    }

    /**
     * Creates and displays a dialog containing a real-time feed of event comments,
     * along with an input field to submit new comments.
     */
    private void showCommentsDialog() {
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

        com.google.firebase.firestore.ListenerRegistration listener = db.collection("events")
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
                        tv.setTextSize(15f);
                        commentsListLayout.addView(tv);
                    }

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        com.example.getoutthere.models.Comment comment =
                                doc.toObject(com.example.getoutthere.models.Comment.class);

                        if (comment != null) {
                            TextView tv = new TextView(this);

                            android.text.SpannableString formattedText =
                                    new android.text.SpannableString(
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
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            textParams.setMargins(0, 0, 0, 12);
                            tv.setLayoutParams(textParams);

                            commentsListLayout.addView(tv);
                        }
                    }

                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                });

        sendButton.setOnClickListener(v -> {
            String text = commentInput.getText() == null
                    ? ""
                    : commentInput.getText().toString().trim();

            if (text.isEmpty()) return;

            sendButton.setEnabled(false);

            db.collection("profiles").document(entrant.getDeviceId()).get().addOnSuccessListener(doc -> {
                String userName = doc.exists()
                        ? doc.getString("name")
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
                        .addOnFailureListener(err -> {
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
    }

    /**
     * Displays team data the corresponding details.
     * @param tvPrimaryOrganizer
     * @param labelCoOrganizers
     */
    public static void displayTeamData(AppCompatActivity activity, Event event, TextView tvPrimaryOrganizer, TextView labelCoOrganizers, LinearLayout layoutCoOrganizers) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // immediately clear the layout to prevent ghosting from previous calls
        layoutCoOrganizers.removeAllViews();
        labelCoOrganizers.setVisibility(View.GONE);

        if (event.getOrganizerId() != null) {
            db.collection("profiles").document(event.getOrganizerId()).get()
                    .addOnSuccessListener(profileDoc -> {
                        if (profileDoc.exists()) {
                            tvPrimaryOrganizer.setText(profileDoc.getString("name"));
                        } else {
                            tvPrimaryOrganizer.setText("Unknown Host");
                        }
                    });

            List<String> coOrgIds = event.getCoOrganizerIds();
            if (coOrgIds != null && !coOrgIds.isEmpty()) {
                labelCoOrganizers.setVisibility(View.VISIBLE);

                for (String id : coOrgIds) {
                    db.collection("profiles").document(id).get()
                            .addOnSuccessListener(profileDoc -> {
                                if (profileDoc.exists()) {
                                    String name = profileDoc.getString("name");

                                    // 2. ensure we don't add the same name twice

                                    boolean alreadyExists = false;
                                    for (int i = 0; i < layoutCoOrganizers.getChildCount(); i++) {
                                        View child = layoutCoOrganizers.getChildAt(i);
                                        if (child instanceof TextView && ((TextView) child).getText().equals(name)) {
                                            alreadyExists = true;
                                            break;
                                        }
                                    }

                                    if (!alreadyExists) {
                                        TextView bubble = new TextView(activity);
                                        bubble.setText(name);
                                        bubble.setTextColor(activity.getColor(R.color.white));
                                        bubble.setPadding(30, 12, 30, 12);
                                        bubble.setTextSize(14f);
                                        bubble.setBackgroundResource(R.drawable.bg_nav_glass);

                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        params.setMargins(0, 0, 16, 0);
                                        bubble.setLayoutParams(params);

                                        layoutCoOrganizers.addView(bubble);
                                    }
                                }
                            });
                }
            }
        }
    }

    /**
     * Checks for location permissions, fetches the user's current coordinates,
     * and proceeds to join the waitlist with location data attached.
     */
    private void fetchLocationAndJoin() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check if the user has granted location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // If no permission, request it from the user
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        // Fetch the location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        joinWaitingList(location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(this, "Could not find location. Please ensure GPS is on.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Handles the permission request response for location access.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, try fetching and joining again
                fetchLocationAndJoin();
            } else {
                Toast.makeText(this, "Location permission is required to join this event.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Adds the entrant to the Firestore waitingList subcollection and updates the event's capacity limit.
     * @param latitude The user's latitude (null if geolocation is not required)
     * @param longitude The user's longitude (null if geolocation is not required)
     */
    private void joinWaitingList(Double latitude, Double longitude) {
        HashMap<String, Object> waitlistData = new HashMap<>();
        waitlistData.put("timestamp", FieldValue.serverTimestamp());
        waitlistData.put("status", "Waiting");

        if (latitude != null && longitude != null) {
            waitlistData.put("latitude", latitude);
            waitlistData.put("longitude", longitude);
        }

        // Add to waiting list collection
        db.collection("events")
                .document(event.getId())
                .collection("waitingList")
                .document(entrant.getDeviceId())
                .set(waitlistData)
                .addOnSuccessListener(aVoid -> {
                    isOnWaitingList = true;

                    // Increment the waitlist count
                    db.collection("events")
                            .document(event.getId())
                            .update("currentWaitlistCount", FieldValue.increment(1))
                            .addOnSuccessListener(unused -> {
                                int newCount = event.getCurrentWaitlistCount() + 1;
                                event.setCurrentWaitlistCount(newCount);
                                updateSpotsUI();
                                updateToggleButton();
                                Toast.makeText(EventDetailsActivity.this, "Joined waiting list!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Rollback local state if increment fails
                                isOnWaitingList = false;
                                Toast.makeText(EventDetailsActivity.this, "Failed to update waitlist count", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(EventDetailsActivity.this, "Failed to join waiting list", Toast.LENGTH_SHORT).show());
    }

}