package com.example.getoutthere.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.example.getoutthere.event.EventDetailsActivity;
import com.example.getoutthere.models.EntrantProfile;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Allows administrators to view details for all events
 * <p>
 *     Showcases events' attributes including organizer name, event name,
 *     event date, and event status to the administrator.
 * </p>
 *
 * Outstanding Issues:
 * - Currently, the event status is defaulted to null. It will be updated
 * as functionality is implemented regarding notifying winners and losers.
 */


/**
 * Represents the screen that can be used to view events.
 * This class handles the structuring and displaying of the event attributes,
 * as well as a delete functionality that deletes the events from the database.
 * * @author Hassan Ali + Terence Bedell
 * @version 1.0
 */

public class ManageEventsActivity extends AppCompatActivity {

    /**
     * Initializes the activity and showcases events to the
     * administrator. Allows the administrator to delete the events.
     * Also creates a button element that allows the user to
     * return to the admin dashboard.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */

    //Replaced Dummy Data with unified eventList
    private List<Event> eventList = new ArrayList<>();

    private LinearLayout eventsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_events);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button EventsManagerBackButton = findViewById(R.id.EventsManagerBackButton);

        EventsManagerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageEventsActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });

        eventsContainer = findViewById(R.id.eventsContainer);

        grabData();
        render();

    }

    /**
     * Fetches the data from the database and populates the eventList
     * so event information can be rendered onto the screen.
     */
    private void grabData() {


        FirebaseFirestore.getInstance().collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear(); // Clear old data
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Convert the Firestore document directly into local Event class objects
                        try {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                eventList.add(event);
                            }
                        }catch (Exception e){
                            // If we're here it means the event data is incompatible (probably due to the Timestamp String change)
                            Toast.makeText(ManageEventsActivity.this, "Unable to load" + doc.get("name"), Toast.LENGTH_SHORT).show();
                        }
                    }
                    render(); // Redraw the UI after data is loaded
                })
                .addOnFailureListener(e -> {
                    Log.e("FetchEvent", "Error fetching event", e);
                });

    }

    /**
     * Renders the data present in the eventList onto the user's screen,
     * and allows for the user to delete the event from the database upon
     * clicking the "Delete" button beside an event.
     */
    private void render() {

        eventsContainer.removeAllViews();  // clearing anything previously present

        for (int index = 0; index < eventList.size(); index++) {
            //Junk data shield, skip invalid entries
            try {
                Event currentEvent = eventList.get(index);

                View row = getLayoutInflater().inflate(R.layout.item_event_admin, eventsContainer, false);

                // map the data (Like passing props)
                TextView name = row.findViewById(R.id.tvEventName);
                TextView date = row.findViewById(R.id.tvEventDate);
                TextView organizer = row.findViewById(R.id.tvOrganizerName);
                TextView btnView = row.findViewById(R.id.btnViewEvent);

                View btnDelete = row.findViewById(R.id.btnDelete);
                View btnComments = row.findViewById(R.id.btnComments);

                name.setText(currentEvent.getName());

                // Safe Date Formatting
                if (currentEvent.getStartDate() != null) {
                    SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    date.setText(fmt.format(currentEvent.getStartDate().toDate()));
                }

                name.setText("EVENT: " + currentEvent.getName());



                String organizerId = currentEvent.getOrganizerId();
                populateOrganizerName(organizerId, organizer);


                int finalIndex = index;
                btnDelete.setOnClickListener(v -> {
                    // Get the specific event for this button
                    Event eventToDelete = eventList.get(finalIndex);
                    //System.out.println("Delete clicked");

                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete Event")
                            .setMessage("Are you sure you want to delete '" + eventToDelete.getName() + "'?")
                            .setPositiveButton("Delete", (dialog, which) -> {

                                //Try deleting document from firebase
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("events").document(eventToDelete.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            //Database delete was successful! Now remove it from the local list
                                            eventList.remove(eventToDelete);

                                            // Redraw the UI so the row disappears
                                            render();
                                        })
                                        .addOnFailureListener(e -> {
                                            //Tell the user if it failed
                                            Log.e("DeleteEvent", "Error deleting document", e);
                                        });

                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                btnView.setOnClickListener(v -> {
                    Intent intent = new Intent(this, EventDetailsActivity.class);
                    intent.putExtra("eventId", currentEvent.getId());
                    startActivity(intent);
                });

                btnComments.setOnClickListener(v -> {
                    // The following code is from Anthropic, Claude, "How do I display comments from every event's collection in a popup dialog", 2026-04-03
                    Event commentEvent = eventList.get(finalIndex);
                    String eventId = commentEvent.getId();

                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                    builder.setTitle("Event Comments");

                    LinearLayout mainLayout = new LinearLayout(this);
                    mainLayout.setOrientation(LinearLayout.VERTICAL);
                    mainLayout.setPadding(40, 24, 40, 24);

                    android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
                    LinearLayout commentsListLayout = new LinearLayout(this);
                    commentsListLayout.setOrientation(LinearLayout.VERTICAL);
                    scrollView.addView(commentsListLayout);

                    LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
                    scrollParams.setMargins(0, 0, 0, 24);
                    scrollView.setLayoutParams(scrollParams);

                    mainLayout.addView(scrollView);
                    builder.setView(mainLayout);
                    builder.setPositiveButton("Close", null);

                    androidx.appcompat.app.AlertDialog dialog = builder.create();

                    // REAL-TIME FIRESTORE LISTENER — fetches all comments for this event
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
                                    com.example.getoutthere.models.Comment comment = doc.toObject(com.example.getoutthere.models.Comment.class);
                                    if (comment != null) {
                                        // Row: [comment text] [delete button]
                                        LinearLayout commentRow = new LinearLayout(this);
                                        commentRow.setOrientation(LinearLayout.HORIZONTAL);
                                        commentRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
                                        LinearLayout.LayoutParams commentRowParams = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        );
                                        commentRow.setLayoutParams(commentRowParams);

                                        TextView tv = new TextView(this);
                                        android.text.SpannableString formattedText = new android.text.SpannableString(
                                                comment.getEntrantName() + ": " + comment.getContent());
                                        formattedText.setSpan(
                                                new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                                0, comment.getEntrantName().length() + 1,
                                                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        tv.setText(formattedText);
                                        tv.setTextSize(16f);
                                        tv.setPadding(0, 8, 0, 16);
                                        // weight 1f so the comment text takes up all space, pushing delete button to the right
                                        tv.setLayoutParams(new LinearLayout.LayoutParams(
                                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                                        Button commentDeleteButton = new Button(this);
                                        commentDeleteButton.setText("DELETE");
                                        commentDeleteButton.setBackgroundColor(0xFFCC0000);
                                        commentDeleteButton.setTextColor(0xFFFFFFFF);
                                        commentDeleteButton.setPadding(16, 4, 16, 4);
                                        commentDeleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));

                                        // The following code is from Anthropic, Claude, "Add a delete button with an onclick listener that deletes the comment from the database upon being clicked", 2026-04-03
                                        // Delete this comment from Firestore when the admin presses DELETE
                                        String commentId = doc.getId();
                                        commentDeleteButton.setOnClickListener(del -> {
                                            new androidx.appcompat.app.AlertDialog.Builder(this)
                                                    .setTitle("Delete Comment")
                                                    .setMessage("Are you sure you want to delete this comment by " + comment.getEntrantName() + "?")
                                                    .setPositiveButton("Delete", (confirmDialog, which) -> {
                                                        FirebaseFirestore.getInstance()
                                                                .collection("events")
                                                                .document(eventId)
                                                                .collection("comments")
                                                                .document(commentId)
                                                                .delete()
                                                                .addOnFailureListener(err -> {
                                                                    Toast.makeText(this, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                                                                });
                                                        // No need to manually update the UI —
                                                        // the snapshot listener above will fire automatically on delete
                                                    })
                                                    .setNegativeButton("Cancel", null)
                                                    .show();
                                        });

                                        commentRow.addView(tv);
                                        commentRow.addView(commentDeleteButton);
                                        commentsListLayout.addView(commentRow);
                                    }
                                }
                                scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                            });

                    // Remove the Firestore listener when the dialog closes to prevent memory leaks
                    dialog.setOnDismissListener(d -> listener.remove());

                    dialog.show();
                });

                eventsContainer.addView(row);
            }catch (Exception e){
                Log.e("Render", "Skipping corrupted event at index " + index, e);
            }
        }

    }

    private void populateOrganizerName(String organizerId, TextView targetView) {
        // 1. Initial State (The "Skeleton" phase)
        targetView.setText("ORGANIZER: Loading...");

        if (organizerId == null || organizerId.isEmpty()) {
            targetView.setText("ORGANIZER: None");
            return;
        }

        // 2. Fetch by Document ID (Direct hit, no search required)
        FirebaseFirestore.getInstance().collection("profiles")
                .document(organizerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        targetView.setText("ORGANIZER: " + (name != null ? name : "Anonymous"));
                    } else {
                        targetView.setText("ORGANIZER: Unknown (" + organizerId.substring(0,4) + "...)");
                    }
                })
                .addOnFailureListener(e -> {
                    targetView.setText("ORGANIZER: Error loading");
                    Log.e("UI_POLISH", "Failed to fetch organizer", e);
                });
    }
}