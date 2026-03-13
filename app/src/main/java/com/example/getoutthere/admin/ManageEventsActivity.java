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

            Event currentEvent = eventList.get(index);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundColor(0xFF59A91E);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(24, 16, 16, 16);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(0, 0, 0, 8);
            row.setLayoutParams(rowParams);

            LinearLayout attributesLayout = new LinearLayout(this);
            attributesLayout.setOrientation(LinearLayout.VERTICAL);
            attributesLayout.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams attributesParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            );
            attributesLayout.setLayoutParams(attributesParams);


            // adding horizontal text views for name, organizer, date, and status

            TextView name = new TextView(this);
            name.setText("EVENT: " + currentEvent.getName());
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(13f);
            name.setPadding(0, 0, 12, 0);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            name.setLayoutParams(nameParams);
            attributesLayout.addView(name);

            TextView organizer = new TextView(this);

            String organizerId = currentEvent.getOrganizerId();
            FirebaseFirestore.getInstance().collection("profiles")
                    .whereEqualTo("deviceId", organizerId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot matching_profile = queryDocumentSnapshots.getDocuments().get(0);
                            String organizerName = matching_profile.getString("name");
                            organizer.setText("ORGANIZER: " + organizerName);
                        } else {
                            organizer.setText("ORGANIZER: Unknown");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Fetch Organizer", "Error finding organizer", e);
                    });


            organizer.setTextColor(0xFFFFFFFF);
            organizer.setTextSize(13f);
            organizer.setPadding(0, 0, 12, 0);
            LinearLayout.LayoutParams organizerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            organizer.setLayoutParams(organizerParams);
            attributesLayout.addView(organizer);

            TextView date = new TextView(this);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            String dateString;
            if (currentEvent.getStartDate() != null) {
                dateString = format.format(currentEvent.getStartDate().toDate());
            } else {
                dateString = "No date set";
            }
            date.setText("DATE: " + dateString);
            date.setTextColor(0xFFFFFFFF);
            date.setTextSize(13f);
            date.setPadding(0, 0, 12, 0);
            LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            date.setLayoutParams(dateParams);
            attributesLayout.addView(date);

            TextView status = new TextView(this);
            status.setText("STATUS: " + currentEvent.getStatus());
            status.setTextColor(0xFFFFFFFF);
            status.setTextSize(13f);
            status.setPadding(0, 0, 12, 0);
            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            status.setLayoutParams(statusParams);
            attributesLayout.addView(status);

            Button deleteButton = new Button(this);
            deleteButton.setText("DELETE");
            deleteButton.setBackgroundColor(0xFFCC0000);
            deleteButton.setTextColor(0xFFFFFFFF);
            deleteButton.setPadding(16, 8, 16, 8);
            deleteButton.setEnabled(true);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            deleteButton.setLayoutParams(btnParams);
            int finalIndex = index;
            deleteButton.setOnClickListener(v -> {
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

            row.addView(attributesLayout);
            row.addView(deleteButton);
            eventsContainer.addView(row);
        }

    }
}