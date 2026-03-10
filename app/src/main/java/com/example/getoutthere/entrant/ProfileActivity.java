package com.example.getoutthere.entrant;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;

public class ProfileActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput;
    private Button saveButton, deleteButton;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        nameInput = findViewById(R.id.editTextName);
        emailInput = findViewById(R.id.editTextEmail);
        phoneInput = findViewById(R.id.editTextPhone);
        saveButton = findViewById(R.id.buttonSaveProfile);
        deleteButton = findViewById(R.id.buttonDeleteProfile);

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email are required!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Profile ready to save for ID: " + deviceId, Toast.LENGTH_LONG).show();
            }
        });

        deleteButton.setOnClickListener(v -> {
            Toast.makeText(this, "Simulating profile deletion...", Toast.LENGTH_SHORT).show();
            nameInput.setText("");
            emailInput.setText("");
            phoneInput.setText("");
        });
    }
}
