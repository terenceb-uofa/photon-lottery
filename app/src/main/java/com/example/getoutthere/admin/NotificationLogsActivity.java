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

/**
 * Allows administrators to view all notifications that have been
 * sent out to users.
 * Outstanding Issues:
 * - This screen has not been implemented in project part 3.
 */

public class NotificationLogsActivity extends AppCompatActivity {

    /**
     * Initializes the activity and showcases the notifications that have
     * been sent to app users. Also creates a button element that allows
     * the user to return to the admin dashboard.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_logs);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button NotificationLogBackButton = findViewById(R.id.NotificationLogBackButton);

        NotificationLogBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationLogsActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });
    }
}