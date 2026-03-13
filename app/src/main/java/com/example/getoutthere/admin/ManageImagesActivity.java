package com.example.getoutthere.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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

        Button ImagesManagerBackButton = findViewById(R.id.ImagesManagerBackButton);

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

        imagesContainer.removeAllViews();  // clearing anything previously present

        for (int index = 0; index < eventList.size(); index++) {

            Event currentEvent = eventList.get(index);

            // creating linear layout for image + delete button
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundColor(0xFF59A91E);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(24, 16, 16, 16);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(0, 0, 0, 8);
            row.setLayoutParams(rowParams);


            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f);
            imageParams.height = 300;
            imageView.setLayoutParams(imageParams);

            if (currentEvent.getPosterUrl() != null) {
                Glide.with(this).load(currentEvent.getPosterUrl()).into(imageView);  // converting image url into actual image
            } else {
                Glide.with(this).load("https://firebasestorage.googleapis.com/v0/b/photon-lottery.firebasestorage.app/o/event_posters%2Felementor-placeholder-image.png?alt=media&token=570ea3b4-1f99-4f52-be3f-86fa2a71f1a5").into(imageView);
            }

            Button deleteButton = new Button(this);
            deleteButton.setText("DELETE");
            deleteButton.setBackgroundColor(0xFFCC0000);
            deleteButton.setTextColor(0xFFFFFFFF);
            deleteButton.setPadding(16, 8, 16, 8);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            btnParams.setMargins(8, 0, 0, 0);
            deleteButton.setLayoutParams(btnParams);

            row.addView(imageView);
            row.addView(deleteButton);
            imagesContainer.addView(row);
        }
    }

}