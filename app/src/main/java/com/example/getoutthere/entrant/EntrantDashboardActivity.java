package com.example.getoutthere.entrant;

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
import com.example.getoutthere.admin.ManageEventsActivity;
import com.example.getoutthere.admin.ManageOrganizersActivity;
import com.example.getoutthere.admin.ManageProfilesActivity;
import com.example.getoutthere.admin.NotificationLogsActivity;
import com.example.getoutthere.event.EventListActivity;
import com.example.getoutthere.organizer.OrganizerCreateEventActivity;
import com.example.getoutthere.organizer.OrganizerEventListActivity;

/**
 * Acts as the primary navigation dashboard for Entrant users.
 * <p>
 * This serves as a control class that provides a menu interface for entrants
 * to access various parts of the application, such as viewing events, managing their
 * profile, and viewing event history.
 * <p>
 * Outstanding Issues:
 * - Many navigation buttons are currently using placeholder intents and redirecting to
 * incorrect or Admin-level activities instead of their proper Entrant destinations.
 */
public class EntrantDashboardActivity extends AppCompatActivity {

    /**
     * Initializes the dashboard Activity, sets up the user interface, and binds
     * click listeners to the navigation buttons to launch other activities via Intents.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrant_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Create Events
        Button NavToEventManager = findViewById(R.id.button1);
        NavToEventManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrantDashboardActivity.this, OrganizerCreateEventActivity.class);
                startActivity(intent);
            }
        });

        // Notifications
        Button NavToProfileManager = findViewById(R.id.button2);
        NavToProfileManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrantDashboardActivity.this, EntrantNotificationActivity.class);
                startActivity(intent);
            }
        });

        // Open Events
        Button NavToEventList = findViewById(R.id.button3);
        NavToEventList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrantDashboardActivity.this, EventListActivity.class);
                startActivity(intent);
            }
        });

        // Event History
        Button NavToOrganizerManager = findViewById(R.id.button4);
        NavToOrganizerManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrantDashboardActivity.this, EventHistory.class);
                startActivity(intent);
            }
        });

        // User Profile
        Button NavToNotificationLogs = findViewById(R.id.button5);
        NavToNotificationLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrantDashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // My Events
        Button btnMyEvents = findViewById(R.id.btnMyEvents);
        btnMyEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrantDashboardActivity.this, OrganizerEventListActivity.class);
                startActivity(intent);
            }
        });
    }
}