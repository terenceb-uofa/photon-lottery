package com.example.getoutthere.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;

import java.util.ArrayList;
import java.util.List;

public class ManageEventsActivity extends AppCompatActivity {

    private List<Integer> Ids   = new ArrayList<>();
    private List<String>  Names = new ArrayList<>();
    private List<String>  Organizers = new ArrayList<>();
    private List<String>  Dates = new ArrayList<>();
    private List<String>  Statuses = new ArrayList<>();

    private LinearLayout eventsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_events);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button EventsManagerBackButton = findViewById(R.id.EventsManagerBackButton);

        EventsManagerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageEventsActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });

        eventsContainer = findViewById(R.id.eventsContainer);

        grabData();
        render();

    }


    private void grabData() {

        // TODO: Add to lists based on data from database rather than random data
        Ids.add(1);
        Ids.add(2);
        Ids.add(3);

        Names.add("Birthday");
        Names.add("Funeral");
        Names.add("Fundraiser");

        Organizers.add("Bob");
        Organizers.add("Joe");
        Organizers.add("Alice");

        Dates.add("11-03-2026");
        Dates.add("01-03-2026");
        Dates.add("05-03-2026");

        Statuses.add("Open");
        Statuses.add("Past");
        Statuses.add("Closed");

    }

    private void render() {

        eventsContainer.removeAllViews();  // clearing anything previously present

        for (int index = 0; index < Ids.size(); index++) {

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

            LinearLayout attributesLayout = new LinearLayout(this);
            attributesLayout.setOrientation(LinearLayout.VERTICAL);
            attributesLayout.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams attributesParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            );
            attributesLayout.setLayoutParams(attributesParams);


            // adding horizontal text views for name, organizer, date, and status

            TextView name = new TextView(this);
            name.setText("EVENT: " + Names.get(index));
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(13f);
            name.setPadding(0, 0, 12, 0);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            name.setLayoutParams(nameParams);
            attributesLayout.addView(name);

            TextView organizer = new TextView(this);
            organizer.setText("ORGANIZER: " + Organizers.get(index));
            organizer.setTextColor(0xFFFFFFFF);
            organizer.setTextSize(13f);
            organizer.setPadding(0, 0, 12, 0);
            LinearLayout.LayoutParams organizerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            organizer.setLayoutParams(organizerParams);
            attributesLayout.addView(organizer);

            TextView date = new TextView(this);
            date.setText("DATE: " + Dates.get(index));
            date.setTextColor(0xFFFFFFFF);
            date.setTextSize(13f);
            date.setPadding(0, 0, 12, 0);
            LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            date.setLayoutParams(dateParams);
            attributesLayout.addView(date);

            TextView status = new TextView(this);
            status.setText("STATUS: " + Statuses.get(index));
            status.setTextColor(0xFFFFFFFF);
            status.setTextSize(13f);
            status.setPadding(0, 0, 12, 0);
            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            status.setLayoutParams(statusParams);
            attributesLayout.addView(status);

            Button deleteButton = new Button(this);
            deleteButton.setText("DELETE");
            deleteButton.setBackgroundColor(0xFFCC0000);
            deleteButton.setTextColor(0xFFFFFFFF);
            deleteButton.setPadding(16, 8, 16, 8);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            deleteButton.setLayoutParams(btnParams);

            deleteButton.setOnClickListener(v -> {
                // TODO: Delete items from database upon clicking of this button (Terence)
            });

            row.addView(attributesLayout);
            row.addView(deleteButton);
            eventsContainer.addView(row);
        }

    }
}