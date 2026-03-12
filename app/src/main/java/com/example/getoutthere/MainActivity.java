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

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String deviceId;

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

        Button NavToAdminDashboard = findViewById(R.id.NavToAdminDashboard);
        NavToAdminDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });

        Button NavToEntrantDashboard = findViewById(R.id.NavToEntrantDashboard);
        NavToEntrantDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEntrantProfileAndNavigate();
            }
        });
    }
    private void checkEntrantProfileAndNavigate() {
        db.collection("profiles").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        startActivity(new Intent(MainActivity.this, EntrantDashboardActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error connecting to database", Toast.LENGTH_SHORT).show();
                });
    }
}