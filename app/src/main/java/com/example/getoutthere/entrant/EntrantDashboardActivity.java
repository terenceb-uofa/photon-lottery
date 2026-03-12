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

// Dashboard for entrant
public class EntrantDashboardActivity extends AppCompatActivity {

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


        // TODO: Change the buttons so they redirect to the proper activities
        // TODO: Should redirect to Create Events
        Button NavToEventManager = findViewById(R.id.button1);
        NavToEventManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrantDashboardActivity.this, OrganizerCreateEventActivity.class);
                startActivity(intent);
            }
        });

        // TODO: Should redirect to Notification Board
        Button NavToProfileManager = findViewById(R.id.button2);
        NavToProfileManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrantDashboardActivity.this, ManageProfilesActivity.class);
                startActivity(intent);
            }
        });

        // TODO: Should redirect to Open Events (Events list) [Caleb]
        Button NavToEventList = findViewById(R.id.button3);
        NavToEventList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrantDashboardActivity.this, EventListActivity.class);
                startActivity(intent);
            }
        });

        // TODO: Should redirect to Event History
        Button NavToOrganizerManager = findViewById(R.id.button4);
        NavToOrganizerManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrantDashboardActivity.this, ManageOrganizersActivity.class);
                startActivity(intent);
            }
        });

        // TODO: Should redirect to User Profile
        Button NavToNotificationLogs = findViewById(R.id.button5);
        NavToNotificationLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrantDashboardActivity.this, NotificationLogsActivity.class);
                startActivity(intent);
            }
        });

    }
}