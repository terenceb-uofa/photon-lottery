package com.example.getoutthere.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
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
 * Represents the screen that can be used to view organizer profiless.
 * This class handles the structuring and displaying of the organizers,
 * as well as a delete functionality that deletes the organizers from the database.
 * * @author Hassan Ali + Terence Bedell
 * @version 1.0
 */

public class ManageOrganizersActivity extends AppCompatActivity {
    /**
     * Initializes the activity and showcases organizers to the
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
    }
}