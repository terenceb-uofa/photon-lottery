package com.example.getoutthere.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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

        FrameLayout OrganizerManagerBackButton = findViewById(R.id.OrganizerManagerBackButton);

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


    /**
     * Fetches the data from the database and populates the organizersList
     * and organizerEventsMap so the data that the event holds
     * regarding the organizer can be used to render the organizer
     * name onto the screen.
     */

    private void grabData() {
        FirebaseFirestore.getInstance().collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    organizerEventsMap.clear(); // Reset map
                    organizersList.clear();
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


                    // Tracker to only render when the last profile is fetched

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
    private void render() {
        organizersContainer.removeAllViews();

        for (int index = 0; index < organizersList.size(); index++) {
            try {
                EntrantProfile currentProfile = organizersList.get(index);

                View row = getLayoutInflater().inflate(R.layout.item_organizer_admin, organizersContainer, false);

                TextView nameView = row.findViewById(R.id.tvOrganizerName);
                TextView eventsView = row.findViewById(R.id.tvOrganizerEvents);
                View banButton = row.findViewById(R.id.btnBan);

                nameView.setText(currentProfile.getName());

                List<Event> myEvents = organizerEventsMap.get(currentProfile.getDeviceId());
                if (myEvents != null && !myEvents.isEmpty()) {
                    StringBuilder eventNames = new StringBuilder("Events: ");
                    for (Event e : myEvents) {
                        eventNames.append(e.getName()).append(", ");
                    }
                    String displayStr = eventNames.toString().replaceAll(", $", "");
                    eventsView.setText(displayStr);
                } else {
                    eventsView.setText("Events: None");
                }

                banButton.setOnClickListener(v -> {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Ban Organizer")
                            .setMessage("Ban '" + currentProfile.getName() + "'? This will delete all their events but keep their entrant profile")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                DeletionUtils.banOrganizerAndCascadeEvents(
                                        currentProfile.getDeviceId(),
                                        () -> {
                                            organizersList.remove(currentProfile);
                                            render();
                                            Toast.makeText(this, "Organizer banned and events removed", Toast.LENGTH_SHORT).show();
                                        },
                                        () -> Toast.makeText(this, "Ban failed", Toast.LENGTH_SHORT).show()
                                );
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                organizersContainer.addView(row);

            } catch (Exception e) {
                Log.e("Render", "Error rendering profile at index " + index, e);
            }
        }
    }
}