package com.example.getoutthere.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for administrative management of Event Images.
 * Role: Provides UI to view and delete event posters from Firebase Storage and Firestore.
 * Outstanding issues: Basic UI implemented; polish planned for final sprint.
 */

public class ManageImagesActivity extends AppCompatActivity {

    private List<Event> eventsWithImages = new ArrayList<>();
    private LinearLayout imagesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_images);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imagesContainer = findViewById(R.id.imagesContainer);

        Button ImagesManagerBackButton = findViewById(R.id.ImagesManagerBackButton);
        ImagesManagerBackButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManageImagesActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
        });

        grabData();
    }

    private void loadImageFromUrl(String url, ImageView imageView) {
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());

        executor.execute(() -> {
            try {
                java.io.InputStream in = new java.net.URL(url).openStream();
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(in);

                // Push the bitmap back to the UI thread
                handler.post(() -> imageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                Log.e("ImageLoader", "Error loading image", e);
            }
        });
    }

    private void grabData() {
        FirebaseFirestore.getInstance().collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventsWithImages.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        // ONLY grab events that actually have a poster URL
                        if (event != null && event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                            eventsWithImages.add(event);
                        }
                    }
                    render();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show());
    }

    private void render() {
        imagesContainer.removeAllViews();

        for (int index = 0; index < eventsWithImages.size(); index++) {
            Event currentEvent = eventsWithImages.get(index);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundColor(0xFF59A91E);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(24, 16, 16, 16);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(0, 0, 0, 16);
            row.setLayoutParams(rowParams);

            // Thumbnail Placeholder
            ImageView thumbnail = new ImageView(this);
            thumbnail.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
            thumbnail.setPadding(0, 0, 24, 0);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP); // Makes it look professional

            loadImageFromUrl(currentEvent.getPosterUrl(), thumbnail);

            LinearLayout attributesLayout = new LinearLayout(this);
            attributesLayout.setOrientation(LinearLayout.VERTICAL);
            attributesLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView nameView = new TextView(this);
            nameView.setText("Event: " + currentEvent.getName());
            nameView.setTextColor(0xFFFFFFFF);
            nameView.setTextSize(16f);
            nameView.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView orgView = new TextView(this);
            orgView.setText("Organizer ID: " + currentEvent.getOrganizerId());
            orgView.setTextColor(0xFFDDDDDD);
            orgView.setTextSize(12f);

            attributesLayout.addView(nameView);
            attributesLayout.addView(orgView);

            Button deleteButton = new Button(this);
            deleteButton.setText("DELETE");
            deleteButton.setBackgroundColor(0xFFCC0000);
            deleteButton.setTextColor(0xFFFFFFFF);
            deleteButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Event Image")
                        .setMessage("Permanently delete the poster for '" + currentEvent.getName() + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> {

                            // Delete from Storage
                            FirebaseStorage.getInstance().getReferenceFromUrl(currentEvent.getPosterUrl())
                                    .delete()
                                    .addOnCompleteListener(task -> {
                                        // Regardless of storage success (file might be missing), clear the Firestore link
                                        FirebaseFirestore.getInstance().collection("events")
                                                .document(currentEvent.getId())
                                                .update("posterUrl", null)
                                                .addOnSuccessListener(aVoid -> {
                                                    eventsWithImages.remove(currentEvent);
                                                    render();
                                                    Toast.makeText(v.getContext(), "Image removed", Toast.LENGTH_SHORT).show();
                                                });
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            row.addView(thumbnail);
            row.addView(attributesLayout);
            row.addView(deleteButton);
            imagesContainer.addView(row);
        }
    }
}