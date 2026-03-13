package com.example.getoutthere.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getoutthere.R;
import com.example.getoutthere.utils.LotteryUtils;
import com.example.getoutthere.utils.NotificationUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment displaying the waitlist for an event.
 * Allows the organizer to draw a lottery and notify waitlisted entrants.
 */
public class WaitlistFragment extends Fragment {

    // Rename parameter argument to match eventId
    private static final String ARG_EVENT_ID = "eventId";

    // Event ID parameter
    private String eventId;

    // Event capacity for lottery sampling
    private int eventCapacity = 0;

    // Firestore instance
    private FirebaseFirestore db;

    // RecyclerView adapter and data
    private EntrantAdapter adapter;
    private List<Map<String, String>> waitlistEntrants = new ArrayList<>();

    // UI elements
    private RecyclerView rvWaitlist;
    private Button btnNotifyWaitlist;
    private Button btnDrawLottery;

    public WaitlistFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param eventId The ID of the event to display the waitlist for.
     * @return A new instance of fragment WaitlistFragment.
     */
    public static WaitlistFragment newInstance(String eventId) {
        WaitlistFragment fragment = new WaitlistFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_waitlist, container, false);

        // Initialize UI elements
        rvWaitlist = view.findViewById(R.id.rvWaitlist);
        btnNotifyWaitlist = view.findViewById(R.id.btnNotifyWaitlist);
        btnDrawLottery = view.findViewById(R.id.btnDrawLottery);

        // Set up RecyclerView
        adapter = new EntrantAdapter(waitlistEntrants);
        rvWaitlist.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWaitlist.setAdapter(adapter);

        // Draw Lottery button
        btnDrawLottery.setOnClickListener(v -> drawLottery());

        // Notify Waitlist button
        btnNotifyWaitlist.setOnClickListener(v -> notifyWaitlistEntrants());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload event capacity and waitlist entrants every time tab is shown
        loadEventCapacity();
        loadWaitlistEntrants();
    }

    /**
     * Loads the event capacity from Firestore.
     */
    private void loadEventCapacity() {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long capacity = documentSnapshot.getLong("capacity");
                        if (capacity != null) {
                            eventCapacity = capacity.intValue();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load event capacity: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Loads all entrants with status "waiting" from Firestore.
     */
    private void loadWaitlistEntrants() {
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .whereEqualTo("status", "waiting")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    waitlistEntrants.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, String> entrant = new HashMap<>();
                        entrant.put("deviceId", doc.getId());
                        entrant.put("name", doc.getString("name"));
                        entrant.put("email", doc.getString("email"));
                        entrant.put("phone", doc.getString("phone"));
                        waitlistEntrants.add(entrant);
                    }
                    adapter.updateData(waitlistEntrants);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Randomly samples entrants from the waitlist and updates their status to "invited".
     */
    private void drawLottery() {
        if (waitlistEntrants.isEmpty()) {
            Toast.makeText(getContext(), "No entrants on waitlist", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventCapacity == 0) {
            Toast.makeText(getContext(), "Event capacity not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use LotteryUtils to randomly sample entrants based on event capacity
        List<Map<String, String>> selected = LotteryUtils.drawLottery(waitlistEntrants, eventCapacity);

        // Update status to "invited" in Firestore for each selected entrant
        for (Map<String, String> entrant : selected) {
            String deviceId = entrant.get("deviceId");
            db.collection("events")
                    .document(eventId)
                    .collection("waitingList")
                    .document(deviceId)
                    .update("status", "invited")
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        Toast.makeText(getContext(), selected.size() + " entrants selected!", Toast.LENGTH_SHORT).show();

        // Reload list to reflect changes
        loadWaitlistEntrants();
    }

    /**
     * Shows a dialog for the organizer to type a message,
     * then sends a notification to all entrants with status "waiting".
     */
    private void notifyWaitlistEntrants() {
        if (waitlistEntrants.isEmpty()) {
            Toast.makeText(getContext(), "No entrants to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show dialog for organizer to type message
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Send Notification to Waitlist");

        // Input field
        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Type your message...");
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send notification to each waiting entrant
            for (Map<String, String> entrant : waitlistEntrants) {
                String deviceId = entrant.get("deviceId");
                Map<String, Object> notification = NotificationUtils.buildNotification(message, eventId);

                db.collection("profiles")
                        .document(deviceId)
                        .collection("notifications")
                        .add(notification)
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            Toast.makeText(getContext(), "Notified " + waitlistEntrants.size() + " waitlist entrants!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}