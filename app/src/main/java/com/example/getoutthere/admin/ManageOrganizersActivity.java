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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.example.getoutthere.models.EntrantProfile;
import com.example.getoutthere.utils.DeletionUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity for administrative management of Organizers.
 * Outstanding issues:
 * - Currently uses basic UI; polish planned for final sprint.
 */

/**
 * Represents the screen that can be used to view event and delete
 * event organizers as required by the administrator.
 * * @author Hassan Ali + Terence Bedell
 * @version 1.0
 */
public class ManageOrganizersActivity extends AppCompatActivity {
    private Map<String, List<Event>> organizerEventsMap = new HashMap<>(); // Cache the events organizers own, indexed by organizer
    private List<EntrantProfile> organizersList = new ArrayList<>();

    private LinearLayout organizersContainer;

    /**
     * Initializes the activity and showcases event organizers to the
     * administrator. Allows the administrator to delete the organizers.
     * Also creates a button element that allows the user to
     * return to the admin dashboard.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_organizers);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button OrganizerManagerBackButton = findViewById(R.id.OrganizerManagerBackButton);

        OrganizerManagerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageOrganizersActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });


        organizersContainer = findViewById(R.id.organizersContainer);

        grabData();
        render();

    }
<<<<<<< HEAD
=======

    /**
     * Fetches the data from the database and populates the organizersList
     * and organizerEventsMap so the data that the event holds
     * regarding the organizer can be used to render the organizer
     * name onto the screen.
     */
>>>>>>> 8c6977f9ed1ffa8ebaf98457a7ea5586168b5e9d
    private void grabData() {
        FirebaseFirestore.getInstance().collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    organizerEventsMap.clear(); // Reset map
                    Set<String> uniqueOrgIds = new HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            String orgId = e.getOrganizerId();
                            // Add event to map
                            organizerEventsMap.computeIfAbsent(orgId, k -> new ArrayList<>()).add(e);
                            if (e.getOrganizerId() != null) uniqueOrgIds.add(e.getOrganizerId());
                        }
                    }

                    // Fetch profiles for the unique IDs found
                    for (String id : uniqueOrgIds) {
                        FirebaseFirestore.getInstance().collection("profiles").document(id).get()
                                .addOnSuccessListener(doc -> {
                                    EntrantProfile profile = doc.toObject(EntrantProfile.class);
                                    if (profile != null) {
                                        organizersList.add(profile);
                                        render();
                                    }
                                });
                    }
                });
    }

    /**
     * Renders the data present in the eventList onto the user's screen,
     * by taking deviceId and using it to find the host name.
     * Allows for the user to delete the organizer from the database upon
     * clicking the "Delete" button beside an image.
     */
    private void render(){

        organizersContainer.removeAllViews();  // clearing anything previously present

        for (int index = 0; index < organizersList.size(); index++) {

            EntrantProfile currentProfile = organizersList.get(index);
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

            // adding vertical views for name and role (vertical)
            TextView nameView = new TextView(this);
            nameView.setText(currentProfile.getName());
            nameView.setTextColor(0xFFFFFFFF);
            nameView.setTextSize(16f);
            nameView.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f
            );
            nameView.setLayoutParams(nameParams);

            TextView roleView = new TextView(this);
            roleView.setText(currentProfile.getRole());
            roleView.setTextColor(0xFFFFFFFF);
            roleView.setTextSize(14f);
            LinearLayout.LayoutParams roleParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f
            );

            List<Event> myEvents = organizerEventsMap.get(currentProfile.getDeviceId());

            TextView eventsView = new TextView(this);
            if (myEvents != null && !myEvents.isEmpty()) {
                StringBuilder eventNames = new StringBuilder();
                for (Event e : myEvents) {
                    eventNames.append(e.getName()).append(", ");
                }


                eventsView.setText("Events: " + eventNames.toString());
                eventsView.setTextColor(0xFFCCCCCC);
                eventsView.setTextSize(12f);
            }


            roleView.setLayoutParams(roleParams);

            Button deleteButton = new Button(this);
            deleteButton.setText("DELETE");
            deleteButton.setBackgroundColor(0xFFCC0000);
            deleteButton.setTextColor(0xFFFFFFFF);
            deleteButton.setPadding(16, 8, 16, 8);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            deleteButton.setLayoutParams(btnParams);

            int finalIndex = index;
            deleteButton.setOnClickListener(v -> {
                // Get the specific profile for this button
                EntrantProfile profileToDelete = organizersList.get(finalIndex);
                //System.out.println("Delete clicked");

                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Organizer Profile")
                        .setMessage("Are you sure you want to delete '" + profileToDelete.getName() + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> {

                            //Try deleting document from firebase

                            DeletionUtils.deleteProfileAndCascadeEvents(
                                profileToDelete.getDeviceId(),
                                () -> {
                                    // Success: Remove from list and re-render
                                    organizersList.remove(profileToDelete);
                                    render();
                                    Toast.makeText(v.getContext(), "Deleted profile and associated events", Toast.LENGTH_SHORT).show();
                                },
                                () -> {
                                    // Failure
                                    Toast.makeText(v.getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                                );

                                        // Redraw the UI so the row disappears
                                        render();

                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            row.addView(nameView);
            row.addView(roleView);
            row.addView(eventsView);
            row.addView(deleteButton);
            organizersContainer.addView(row);
        }
    }
}