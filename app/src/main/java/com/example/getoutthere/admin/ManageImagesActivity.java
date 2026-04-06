
package com.example.getoutthere.admin;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.example.getoutthere.models.EntrantProfile;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows administrators to view the images for all events.
 * Outstanding Issues:
 * - None
 */


/**
 * Represents the screen that can be used to view event posters.
 * This class handles the structuring and displaying of the event posters,
 * as well as a delete functionality that deletes the posters from the database.
 * * @author Hassan Ali + Terence Bedell
 * @version 1.0
 */
public class ManageImagesActivity extends AppCompatActivity {

    private List<Event> eventList = new ArrayList<>();

    private LinearLayout imagesContainer;



    /**
     * Helper method to clear the posterUrl in Firestore and refresh the UI.
     * @param event The event whose image is being removed.
     */
    private void removeImageUrlFromFirestore(Event event) {
        FirebaseFirestore.getInstance().collection("events")
                .document(event.getId())
                .update("posterUrl", null)
                .addOnSuccessListener(aVoid -> {
                    eventList.remove(event);
                    render();
                    Toast.makeText(this, "Image record removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("ManageImages", "Firestore update failed", e));
    }

    /**
     * Initializes the activity and showcases event posters to the
     * administrator. Allows the administrator to delete the posters.
     * Also creates a button element that allows the user to
     * return to the admin dashboard.
     *
     * @param savedInstanceState If the activity is being re-initialized after being
     * shut down then this Bundle contains the data it most recently
     * supplied. Otherwise it is null.
     */
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

        FrameLayout ImagesManagerBackButton = findViewById(R.id.ImagesManagerBackButton);

        ImagesManagerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageImagesActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });

        imagesContainer = findViewById(R.id.imagesContainer);

        grabData();
        render();
    }

    /**
     * Fetches the data from the database and populates the eventList
     * so the data that the event holds regarding the poster can be
     * used to render the poster onto the screen.
     */
    private void grabData(){

        // putting all events into eventList

        FirebaseFirestore.getInstance().collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear(); // Clear old data
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Convert the Firestore document directly into local Event class objects
                        try {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                eventList.add(event);
                            }
                        }catch (Exception e){
                            // If we're here it means the event data is incompatible (probably due to the Timestamp String change)
                            Toast.makeText(ManageImagesActivity.this, "Unable to load" + doc.get("name"), Toast.LENGTH_SHORT).show();
                        }
                    }
                    render(); // Redraw the UI after data is loaded
                })
                .addOnFailureListener(e -> {
                    Log.e("FetchImage", "Error fetching image", e);
                });

    }

    /**
     * Renders the data present in the eventList onto the user's screen,
     * by taking the poster URL and displaying the image associated.
     * Allows for the user to delete the image from the database upon
     * clicking the "Delete" button beside an image.
     */
    private void render() {

        imagesContainer.removeAllViews();

        for (int index = 0; index < eventList.size(); index++) {
            try {
                Event currentEvent = eventList.get(index);

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);

                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                rowParams.setMargins(0, 0, 0, 12);
                row.setLayoutParams(rowParams);

                FrameLayout imageFrame = new FrameLayout(this);
                LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                );
                frameParams.setMargins(0, 0, 16, 0);
                imageFrame.setLayoutParams(frameParams);
                imageFrame.setPadding(10, 10, 10, 10);

                GradientDrawable frameBackground = new GradientDrawable();
                frameBackground.setColor(getResources().getColor(R.color.glass, getTheme()));
                frameBackground.setCornerRadius(32f);
                frameBackground.setStroke(2, getResources().getColor(R.color.glassBorder, getTheme()));
                imageFrame.setBackground(frameBackground);

                ImageView imageView = new ImageView(this);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        300
                );
                imageView.setLayoutParams(imageParams);

                if (currentEvent.getPosterUrl() != null) {
                    Glide.with(this)
                            .load(currentEvent.getPosterUrl())
                            .transform(new com.bumptech.glide.load.resource.bitmap.RoundedCorners(24))
                            .into(imageView);
                } else {
                    Glide.with(this)
                            .load("https://firebasestorage.googleapis.com/v0/b/photon-lottery.firebasestorage.app/o/event_posters%2Felementor-placeholder-image.png?alt=media&token=570ea3b4-1f99-4f52-be3f-86fa2a71f1a5")
                            .transform(new com.bumptech.glide.load.resource.bitmap.RoundedCorners(24))
                            .into(imageView);
                }

                imageFrame.addView(imageView);

                FrameLayout deleteCircle = new FrameLayout(this);
                LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(48, 48);
                deleteCircle.setLayoutParams(deleteParams);
                deleteCircle.setClickable(true);
                deleteCircle.setFocusable(true);

                GradientDrawable deleteBg = new GradientDrawable();
                deleteBg.setColor(getResources().getColor(R.color.glass, getTheme()));
                deleteBg.setShape(GradientDrawable.OVAL);
                deleteBg.setStroke(2, getResources().getColor(R.color.glassBorder, getTheme()));
                deleteCircle.setBackground(deleteBg);

                ImageView deleteIcon = new ImageView(this);
                FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(24, 24);
                iconParams.gravity = Gravity.CENTER;
                deleteIcon.setLayoutParams(iconParams);
                deleteIcon.setImageResource(R.drawable.ic_delete);
                deleteIcon.setColorFilter(getResources().getColor(R.color.error, getTheme()));

                deleteCircle.addView(deleteIcon);

                row.addView(imageFrame);
                row.addView(deleteCircle);
                imagesContainer.addView(row);

                deleteCircle.setOnClickListener(v -> {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete Event Image")
                            .setMessage("Are you sure you want to permanently delete the image for '" + currentEvent.getName() + "'?")
                            .setPositiveButton("Delete", (dialog, which) -> {

                                String imageUrl = currentEvent.getPosterUrl();

                                if (imageUrl != null && !imageUrl.contains("placeholder-image.png")) {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete()
                                            .addOnCompleteListener(task -> removeImageUrlFromFirestore(currentEvent));
                                } else {
                                    removeImageUrlFromFirestore(currentEvent);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

            } catch (Exception e) {
                Log.e("Render", "Skipping corrupted event at index " + index, e);
            }
        }
    }

}



