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
import com.example.getoutthere.utils.NotificationUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment displaying invited entrants for an event.
 * Allows the organizer to notify all selected entrants.
 */
public class InvitedFragment extends Fragment {

    // Renamed parameter argument to match eventId
    private static final String ARG_EVENT_ID = "eventId";

    // Event ID parameter
    private String eventId;

    // Firestore instance
    private FirebaseFirestore db;

    // RecyclerView adapter and data
    private EntrantAdapter adapter;
    private List<Map<String, String>> invitedEntrants = new ArrayList<>();

    // UI elements
    private RecyclerView rvInvited;
    private Button btnNotifyInvited;

    public InvitedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param eventId The ID of the event to display invited entrants for.
     * @return A new instance of fragment InvitedFragment.
     */
    public static InvitedFragment newInstance(String eventId) {
        InvitedFragment fragment = new InvitedFragment();
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
        View view = inflater.inflate(R.layout.fragment_invited, container, false);

        // Initialize UI elements
        rvInvited = view.findViewById(R.id.rvInvited);
        btnNotifyInvited = view.findViewById(R.id.btnNotifyInvited);

        // Set up RecyclerView
        adapter = new EntrantAdapter(invitedEntrants);
        rvInvited.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInvited.setAdapter(adapter);

        // Notify Invited button
        btnNotifyInvited.setOnClickListener(v -> notifyInvitedEntrants());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload invited entrants every time tab is shown
        loadInvitedEntrants();
    }

    /**
     * Loads all entrants with status "Invited" from Firestore.
     */
    private void loadInvitedEntrants() {
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .whereEqualTo("status", "Invited")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    invitedEntrants.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, String> entrant = new HashMap<>();
                        entrant.put("deviceId", doc.getId());
                        entrant.put("name", doc.getString("name"));
                        entrant.put("email", doc.getString("email"));
                        entrant.put("phone", doc.getString("phone"));
                        invitedEntrants.add(entrant);
                    }
                    adapter.updateData(invitedEntrants);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load invited entrants: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Shows a dialog for the organizer to type a message,
     * then sends a notification to all entrants with status "Invited".
     * US 02.07.02 — Organizer sends notifications to all selected entrants.
     */
    private void notifyInvitedEntrants() {
        if (invitedEntrants.isEmpty()) {
            Toast.makeText(getContext(), "No invited entrants to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show dialog for organizer to type message
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Send Notification to Invited Entrants");

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

            // Send notification to each invited entrant
            for (Map<String, String> entrant : invitedEntrants) {
                String deviceId = entrant.get("deviceId");
                Map<String, Object> notification = NotificationUtils.buildNotification(message, eventId);

                db.collection("profiles")
                        .document(deviceId)
                        .collection("notifications")
                        .add(notification)
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            Toast.makeText(getContext(), "Notified " + invitedEntrants.size() + " invited entrants!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}