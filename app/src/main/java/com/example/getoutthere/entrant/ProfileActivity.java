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
import com.example.getoutthere.models.EntrantProfile;
import com.google.firebase.firestore.FirebaseFirestore;


public class ProfileActivity extends AppCompatActivity {


    private EditText nameInput, emailInput, phoneInput;
    private Button saveButton, deleteButton;
    private String deviceId;
    private FirebaseFirestore db;




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
        db = FirebaseFirestore.getInstance();


        nameInput = findViewById(R.id.editTextName);
        emailInput = findViewById(R.id.editTextEmail);
        phoneInput = findViewById(R.id.editTextPhone);
        saveButton = findViewById(R.id.buttonSaveProfile);
        deleteButton = findViewById(R.id.buttonDeleteProfile);


        loadProfile();
        saveButton.setOnClickListener(v -> saveProfile());
        deleteButton.setOnClickListener(v -> deleteProfile());
    }


    private void loadProfile() {
        db.collection("profiles").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        EntrantProfile profile = documentSnapshot.toObject(EntrantProfile.class);
                        if (profile != null) {
                            nameInput.setText(profile.getName());
                            emailInput.setText(profile.getEmail());
                            phoneInput.setText(profile.getPhoneNumber());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }


    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();


        // Basic Validation
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required!", Toast.LENGTH_SHORT).show();
            return;
        }


        // Satisfies US 01.02.01 & US 01.02.02
        EntrantProfile profile = new EntrantProfile(deviceId, name, email, phone, "user");


        db.collection("profiles").document(deviceId).set(profile)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show());
    }


    private void deleteProfile() {
        // Satisfies US 01.02.04
        db.collection("profiles").document(deviceId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Deleted", Toast.LENGTH_SHORT).show();
                    // Clear UI
                    nameInput.setText("");
                    emailInput.setText("");
                    phoneInput.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting profile", Toast.LENGTH_SHORT).show());
    }
}
