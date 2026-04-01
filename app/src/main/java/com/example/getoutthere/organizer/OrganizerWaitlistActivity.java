package com.example.getoutthere.organizer;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.getoutthere.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Activity for organizer to manage event entrants.
 * Displays tabbed fragments for Invited, Enrolled, Cancelled, and Waitlist entrants.
 * Navigated to from OrganizerEventDetailsActivity via the "View Waitlist" button.
 */
public class OrganizerWaitlistActivity extends AppCompatActivity {

    // UI elements
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Button btnBack;

    // Event ID passed from OrganizerEventDetailsActivity
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_waitlist);

        // Get eventId passed from previous screen
        eventId = getIntent().getStringExtra("eventId");

        // Initialize UI elements
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btnBack = findViewById(R.id.btnBack);

        // Set up ViewPager2 with tab fragments
        OrganizerTabAdapter adapter = new OrganizerTabAdapter(this, eventId);
        viewPager.setAdapter(adapter);

        // Connect TabLayout to ViewPager2 with tab titles
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Invited"); break;
                case 1: tab.setText("Enrolled"); break;
                case 2: tab.setText("Cancelled"); break;
                case 3: tab.setText("Waitlist"); break;
            }
        }).attach();

        // Back button
        btnBack.setOnClickListener(v -> finish());
    }
}