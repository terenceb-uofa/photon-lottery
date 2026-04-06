package com.example.getoutthere.entrant;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;
import com.example.getoutthere.models.EntrantProfile;
import com.example.getoutthere.navigation.NavBottomHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Allows the user to manage their personal Entrant Profile.
 * <p>
 * This Activity serves as a control class for the user profile screen. It handles
 * retrieving, updating, and deleting user information (name, email, phone) from the
 * Firestore database using the device's unique Android ID. This class directly
 * satisfies User Stories 01.02.01, 01.02.02, US 01.02.04.
 * <p>
 * Outstanding Issues:
 * - Input validation is very basic (only checks for empty strings) and does not
 * verify proper email or phone number formatting.
 */
public class ProfileActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput;
    private SwitchMaterial notificationSwitch;
    private Button saveButton, deleteButton;
    private String deviceId;
    private FirebaseFirestore db;

    /**
     * Initializes the Activity, sets up the UI elements, fetches the
     * Android device ID, and binds click listeners to the Save and Delete buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
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
        notificationSwitch = findViewById(R.id.switchNotifications);
        saveButton = findViewById(R.id.buttonSaveProfile);
        deleteButton = findViewById(R.id.buttonDeleteProfile);

        // Setup the switch visual listener
        setupSwitchVisuals();

        loadProfile();
        saveButton.setOnClickListener(v -> saveProfile());
        deleteButton.setOnClickListener(v -> deleteProfile());


        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavBottomHelper.setupBottomNav(this, bottomNav, R.id.nav_profile);
    }

    /**
     * Updates the color of the notification switch thumb based on its state.
     */
    private void setupSwitchVisuals() {
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchThumbColor(isChecked);
        });
    }

    /**
     * Fetches the user's existing profile data from the Firestore collection
     * using their device ID and populates the EditText fields if a profile exists.
     */
    private void loadProfile() {
        db.collection("profiles").document(deviceId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        EntrantProfile profile = documentSnapshot.toObject(EntrantProfile.class);
                        if (profile != null) {
                            nameInput.setText(profile.getName());
                            emailInput.setText(profile.getEmail());
                            phoneInput.setText(profile.getPhoneNumber());
                            notificationSwitch.setChecked(profile.isNotificationsEnabled());
                            // Trigger visual update for initial load
                            updateSwitchThumbColor(profile.isNotificationsEnabled());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the thumb color of the switch (Green for enabled, Red for disabled).
     */
    private void updateSwitchThumbColor(boolean isEnabled) {
        if (isEnabled) {
            notificationSwitch.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success)));
        } else {
            notificationSwitch.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error)));
        }
    }

    /**
     * Validates the input fields, creates a new EntrantProfile object, and saves
     * it to the Firestore collection under the user's device ID.
     */
    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        boolean notificationsEnabled = notificationSwitch.isChecked();

        // Basic Validation
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Email Format Validation
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // STRICT Phone Format Validation
        // This Regex requires 10 digits and optionally allows country codes, dashes, spaces, and parentheses.
        String phoneRegex = "^(\\+\\d{1,3}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$";
        if (!phone.isEmpty() && !phone.matches(phoneRegex)) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Satisfies US 01.02.01 & US 01.02.02
        EntrantProfile profile = new EntrantProfile(deviceId, name, email, phone, "user", notificationsEnabled);

        db.collection("profiles").document(deviceId).set(profile)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show());
    }

    /**
     * Deletes the user's profile from the Firestore database and kicks them back
     * to the SignUpActivity screen.
     */
    private void deleteProfile() {
        // Satisfies US 01.02.04
        db.collection("profiles").document(deviceId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Deleted", Toast.LENGTH_SHORT).show();

                    // Kick user back to the sign-up page
                    Intent intent = new Intent(ProfileActivity.this, SignUpActivity.class);
                    // Clear the back stack so they can't press back to return to their deleted profile
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting profile", Toast.LENGTH_SHORT).show());
    }
}