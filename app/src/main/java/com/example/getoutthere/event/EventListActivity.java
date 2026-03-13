package com.example.getoutthere.event;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.getoutthere.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// Shows list of currently available events user can enroll in.
// Clicking an individual event displays its data.

public class EventListActivity extends AppCompatActivity {

    private ListView listView;
    private List<Event> events = new ArrayList<>();
    private List<String> eventNames = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listOfEvents);

        // Fetch events from Firestore
        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            events.clear();
            eventNames.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Event event = doc.toObject(Event.class);
                event.setId(doc.getId());
                events.add(event);
                eventNames.add(event.getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_history_row, eventNames);
            listView.setAdapter(adapter);
        });

        // On click, go to EventDetailsActivity
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event event = events.get(position);
            Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
            intent.putExtra("eventId", event.getId());
            startActivity(intent);
        });
    }
}