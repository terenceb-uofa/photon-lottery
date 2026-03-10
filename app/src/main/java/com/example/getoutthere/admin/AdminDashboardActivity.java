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

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    Button NavToEventManager = findViewById(R.id.NavToCreateEventManager);

        NavToEventManager.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageEventsActivity.class);
            startActivity(intent);
        }
    });

    Button NavToProfileManager = findViewById(R.id.NavToNotificationBoardManager);

        NavToProfileManager.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageProfilesActivity.class);
            startActivity(intent);
        }
    });

    Button NavToImageManager = findViewById(R.id.NavToOpenEventsManager);

        NavToImageManager.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageImagesActivity.class);
            startActivity(intent);
        }
    });

    Button NavToOrganizerManager = findViewById(R.id.NavToEventHistoryManager);

        NavToOrganizerManager.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageOrganizersActivity.class);
            startActivity(intent);
        }
    });

    Button NavToNotificationLogs = findViewById(R.id.NavToProfile);

        NavToNotificationLogs.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AdminDashboardActivity.this, NotificationLogsActivity.class);
            startActivity(intent);
        }
    });


    }
}