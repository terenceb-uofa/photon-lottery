package com.example.getoutthere.entrant;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.getoutthere.R;
import com.example.getoutthere.models.EntrantProfile;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    private EditText nameInput, emailInput, phoneInput;
    private Button createAccountBtn;
    private String deviceId;
    private FirebaseFirestore db;


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
    private void createAccount() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();


        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        EntrantProfile profile = new EntrantProfile(deviceId, name, email, phone, "user");

        db.collection("profiles").document(deviceId).set(profile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignUpActivity.this, EntrantDashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show());
    }
}


