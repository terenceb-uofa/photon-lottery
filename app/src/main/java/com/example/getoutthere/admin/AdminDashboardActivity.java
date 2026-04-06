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
import com.example.getoutthere.entrant.EntrantNotificationActivity;


/**
 * Acts as the primary navigation dashboard for Administrators.
 * <p>
 *     Allows an administrator to access different parts of the app by
 *     clicking on different buttons, taking them to screens that showcase
 *     different features of the app's participants.
 * </p>
 *
 * Outstanding Issues:
 * - Currently, some buttons such as "View Notification Logs" lead to screens who's
 * functionality is yet to be implemented.
 */


/**
 * Represents the screen that can be used to view navigation options.
 * This class handles the structuring and displaying of the navigation options.
 * * @author Hassan Ali + Terence Bedell
 * @version 1.0
 */

public class AdminDashboardActivity extends AppCompatActivity {

    /**
     * Initializes the activity and showcases navigation options for the
     * administrator.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
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

    Button NavToEventManager = findViewById(R.id.button1);
        NavToEventManager.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageEventsActivity.class);
            startActivity(intent);
        }
    });

    Button NavToProfileManager = findViewById(R.id.button2);
        NavToProfileManager.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageProfilesActivity.class);
            startActivity(intent);
        }
    });

    Button NavToImageManager = findViewById(R.id.button3);
        NavToImageManager.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageImagesActivity.class);
            startActivity(intent);
        }
    });

    Button NavToOrganizerManager = findViewById(R.id.button4);
        NavToOrganizerManager.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageOrganizersActivity.class);
            startActivity(intent);
        }
    });

    Button NavToNotificationLogs = findViewById(R.id.button5);
        NavToNotificationLogs.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AdminDashboardActivity.this, NotificationLogsActivity.class);
            startActivity(intent);
        }
    });


    }
}