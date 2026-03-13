package com.example.getoutthere;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.admin.AdminDashboardActivity;
import com.example.getoutthere.entrant.EntrantDashboardActivity;
import com.example.getoutthere.entrant.SignUpActivity;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The initial entry point of the Get Out There application.
 * <p>
 * This Activity acts as the main routing hub. It provides navigation to the Admin
 * Dashboard and handles Entrant routing by querying the database to check if the
 * current device already has a registered profile.
 * <p>
 * Outstanding Issues:
 * None
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String deviceId;

    /**
     * Initializes the Activity, binds UI elements, and sets up click listeners
     * for the main navigation buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup Admin Navigation
        Button NavToAdminDashboard = findViewById(R.id.NavToAdminDashboard);
        NavToAdminDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });

        // Setup Entrant Navigation
        Button NavToEntrantDashboard = findViewById(R.id.NavToEntrantDashboard);
        NavToEntrantDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEntrantProfileAndNavigate();
            }
        });
    }

    /**
     * Queries the Firestore "profiles" collection using the device's unique Android ID.
     * If a profile exists, the user is routed to the EntrantDashboardActivity.
     * If no profile is found, the user is routed to the SignUpActivity to register.
     */
    private void checkEntrantProfileAndNavigate() {
        db.collection("profiles").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Profile found, go to Dashboard
                        startActivity(new Intent(MainActivity.this, EntrantDashboardActivity.class));
                    } else {
                        // No profile found, force user to Sign Up
                        startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error connecting to database", Toast.LENGTH_SHORT).show();
                });
    }
}