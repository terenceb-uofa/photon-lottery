package com.example.getoutthere.navigation;

import android.app.Activity;
import android.content.Intent;

import com.example.getoutthere.R;
import com.example.getoutthere.entrant.EventHistory;
import com.example.getoutthere.entrant.ProfileActivity;
import com.example.getoutthere.event.EventListActivity;
import com.example.getoutthere.organizer.OrganizerEventListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.getoutthere.admin.NotificationLogsActivity;

public class NavBottomHelper {

    public static void setupBottomNav(Activity activity, BottomNavigationView bottomNav, int selectedItemId) {
        bottomNav.setSelectedItemId(selectedItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == selectedItemId) {
                return true;
            }

            Intent intent = null;

            if (itemId == R.id.nav_home) {
                intent = new Intent(activity, EventListActivity.class);
            } else if (itemId == R.id.nav_my_events) {
                intent = new Intent(activity, OrganizerEventListActivity.class);
            } else if (itemId == R.id.nav_history) {
                intent = new Intent(activity, EventHistory.class);
            } else if (itemId == R.id.nav_notifications) {
                intent = new Intent(activity, NotificationLogsActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(activity, ProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}