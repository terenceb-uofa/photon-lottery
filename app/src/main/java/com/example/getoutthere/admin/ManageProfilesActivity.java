package com.example.getoutthere.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;

import java.util.ArrayList;
import java.util.List;

public class ManageProfilesActivity extends AppCompatActivity {

    private List<Integer> Ids   = new ArrayList<>();
    private List<String>  Names = new ArrayList<>();
    private List<String>  Roles = new ArrayList<>();

    private LinearLayout profilesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_profiles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profilesContainer = findViewById(R.id.profilesContainer);

        grabData();
        render();

        Button ProfilesManagerBackButton = findViewById(R.id.ProfilesManagerBackButton);

        ProfilesManagerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageProfilesActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });
    }

    private void grabData() {

        // TODO: Add to lists based on data from database rather than random data
        Ids.add(1);
        Ids.add(2);
        Ids.add(3);

        Names.add("Roy Harper");
        Names.add("Dick Grayson");
        Names.add("Kory Anders");

        Roles.add("Admin");
        Roles.add("Admin");
        Roles.add("User");

    }

    private void render(){

        profilesContainer.removeAllViews();  // clearing anything previously present

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

            // adding vertical views for name and role (vertical)
            TextView nameView = new TextView(this);
            nameView.setText(Names.get(index));
            nameView.setTextColor(0xFFFFFFFF);
            nameView.setTextSize(16f);
            nameView.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f
            );
            nameView.setLayoutParams(nameParams);

            TextView roleView = new TextView(this);
            roleView.setText(Roles.get(index));
            roleView.setTextColor(0xFFFFFFFF);
            roleView.setTextSize(14f);
            LinearLayout.LayoutParams roleParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f
            );
            roleView.setLayoutParams(roleParams);

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

            row.addView(nameView);
            row.addView(roleView);
            row.addView(deleteButton);
            profilesContainer.addView(row);
        }
    }
}