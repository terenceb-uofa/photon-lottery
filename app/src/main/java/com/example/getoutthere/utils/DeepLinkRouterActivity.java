package com.example.getoutthere.utils;

// The following code used information from https://developer.android.com/training/app-links/create-deeplinks#java and https://www.geeksforgeeks.org/android/deep-linking-in-android-with-example/, 2026-03-12

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.getoutthere.event.EventDetailsActivity;

public class DeepLinkRouterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();

        if (data != null) {
            String eventId = data.getLastPathSegment();

            if (eventId != null && !eventId.isEmpty()) {
                Intent intent = new Intent(this, EventDetailsActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            }
        }

        finish();
    }
}