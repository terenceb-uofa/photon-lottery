package com.example.getoutthere.entrant;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.getoutthere.R;
import com.example.getoutthere.event.EventListActivity;
import com.example.getoutthere.models.EntrantProfile;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Handles the registration process for new users (Entrants).
 * <p>
 * This Activity acts as a control class for the Sign-Up screen. It captures user
 * input, performs basic validation, and saves the new user's profile data to the
 * Firestore database using their device ID. Upon success, it redirects the user
 * to the main dashboard.
 * <p>
 * Outstanding Issues:
 * - Input validation only checks for empty strings; it does not verify proper
 * email formats or phone number lengths.
 */
public class SignUpActivity extends AppCompatActivity {
    private EditText nameInput, emailInput, phoneInput;
    private Button createAccountBtn;
    private String deviceId;
    private FirebaseFirestore db;

    /**
     * Initializes the Activity, sets up the user interface elements, fetches the
     * unique Android device ID, and binds a click listener to the Create Account button.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        nameInput = findViewById(R.id.signUpName);
        emailInput = findViewById(R.id.signUpEmail);
        phoneInput = findViewById(R.id.signUpPhone);
        createAccountBtn = findViewById(R.id.buttonCreateAccount);

        createAccountBtn.setOnClickListener(v -> createAccount());
    }

    /**
     * Validates the input fields, creates a new EntrantProfile, and uploads it to
     * the database. If the upload is successful, it fires an Intent to navigate
     * the user to the EntrantDashboardActivity and clears the back-stack.
     */
    private void createAccount() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Updated constructor call to include default notification preference (true)
        EntrantProfile profile = new EntrantProfile(deviceId, name, email, phone, "user", true);

        db.collection("profiles").document(deviceId).set(profile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignUpActivity.this, EventListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show());
    }
}