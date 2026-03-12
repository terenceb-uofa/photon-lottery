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
import com.example.getoutthere.models.EntrantProfile;
import com.example.getoutthere.utils.DeletionUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ManageProfilesActivity extends AppCompatActivity {

    //Replaced Dummy Data with unified profileList
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

        Button ProfilesManagerBackButton = findViewById(R.id.ProfilesManagerBackButton);

        ProfilesManagerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageProfilesActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });
    }

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
                    //TODO: Toast Message for Error
                });
    }

    private void render(){

        profilesContainer.removeAllViews();  // clearing anything previously present

        for (int index = 0; index < profileList.size(); index++) {

            EntrantProfile currentProfile = profileList.get(index);
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
                EntrantProfile profileToDelete = profileList.get(finalIndex);
                //System.out.println("Delete clicked");

                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Profile")
                        .setMessage("Are you sure you want to delete '" + profileToDelete.getName() + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> {

                            //Try deleting document from firebase
                            DeletionUtils.deleteProfileAndCascadeEvents(
                                    profileToDelete.getDeviceId(),
                                    () -> {
                                        // Success: Remove from list and re-render
                                        profileList.remove(profileToDelete);
                                        render();
                                        Toast.makeText(v.getContext(), "Deleted profile and associated events", Toast.LENGTH_SHORT).show();
                                    },
                                    () -> {
                                        // Failure
                                        Toast.makeText(v.getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                                    }
                            );

                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            row.addView(nameView);
            row.addView(roleView);
            row.addView(deleteButton);
            profilesContainer.addView(row);
        }
    }
}