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
import com.example.getoutthere.models.EntrantProfile;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows administrators to view the profiles for all users.
 * Outstanding Issues:
 * - None
 */

/**
 * Represents the screen that can be used to view profiles.
 * This class handles the structuring and displaying of the profiles,
 * as well as a delete functionality that deletes the profiles from the database.
 * * @author Hassan Ali + Terence Bedell
 * @version 1.0
 */
public class ManageProfilesActivity extends AppCompatActivity {

    /**
     * Initializes the activity and showcases user profiles to the
     * administrator. Allows the administrator to delete the profiles.
     * Also creates a button element that allows the user to
     * return to the admin dashboard.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
    private List<EntrantProfile> profileList = new ArrayList<>();

    private LinearLayout profilesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_profiles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profilesContainer = findViewById(R.id.profilesContainer);

        grabData();
        render();

        FrameLayout ProfilesManagerBackButton = findViewById(R.id.ProfilesManagerBackButton);

        ProfilesManagerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageProfilesActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Fetches the data from the database and populates the profileList
     * so the profile attributes can be rendered onto the screen.
     */
    private void grabData() {

        FirebaseFirestore.getInstance().collection("profiles").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    profileList.clear(); // Clear old data
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Convert the Firestore document directly into local profile class objects
                        try {
                            EntrantProfile profile = doc.toObject(EntrantProfile.class);
                            if (profile != null) {
                                profileList.add(profile);
                            }
                        }catch (Exception e){
                            // If we're here it means the profile data is incompatible (probably due to the Timestamp String change)
                            Toast.makeText(ManageProfilesActivity.this, "Unable to load" + doc.get("name"), Toast.LENGTH_SHORT).show();
                        }
                    }
                    render(); // Redraw the UI after data is loaded
                })
                .addOnFailureListener(e -> {
                    Log.e("FetchProfile", "Error fetching profile", e);
                });
    }


    /**
     * Renders the data present in the profileList onto the user's screen,
     * and allows for the user to delete the profile from the database upon
     * clicking the "Delete" button beside a profile.
     */
    private void render() {

        profilesContainer.removeAllViews();

        for (int index = 0; index < profileList.size(); index++) {
            try {
                EntrantProfile currentProfile = profileList.get(index);

                View row = getLayoutInflater().inflate(R.layout.item_profile_admin, profilesContainer, false);

                TextView nameView = row.findViewById(R.id.tvProfileName);
                TextView roleView = row.findViewById(R.id.tvProfileRole);
                View deleteButton = row.findViewById(R.id.btnDelete);

                nameView.setText(currentProfile.getName());
                roleView.setText(currentProfile.getRole());

                int finalIndex = index;
                deleteButton.setOnClickListener(v -> {
                    EntrantProfile profileToDelete = profileList.get(finalIndex);

                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete Profile")
                            .setMessage("Are you sure you want to delete '" + profileToDelete.getName() + "'?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("profiles").document(profileToDelete.getDeviceId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            profileList.remove(profileToDelete);
                                            render();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("DeleteProfile", "Error deleting document", e);
                                        });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                profilesContainer.addView(row);

            } catch (Exception e) {
                Log.e("Render", "Skipping corrupted profile at index " + index, e);
            }
        }
    }
}