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

    /**
     * Initializes the fragment, retrieves the event ID argument, and sets up Firestore.
     * @param savedInstanceState the saved instance state bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Inflates the fragment layout, initializes UI elements, and sets up the RecyclerView.
     * @param inflater the layout inflater
     * @param container the parent view group
     * @param savedInstanceState the saved instance state bundle
     * @return the inflated view
     */
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

    /**
     * Reloads invited entrants each time the tab becomes visible.
     */
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
                        entrant.put("name", "Loading...");
                        entrant.put("email", "");
                        entrant.put("phone", "");
                        invitedEntrants.add(entrant);
                        int index = invitedEntrants.size() - 1;
                        loadProfileInfo(doc.getId(), index);
                    }
                    adapter.updateData(new ArrayList<>(invitedEntrants));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load invited entrants: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Fetches the entrant's profile info from Firestore using their device ID.
     * If a name is found, it replaces the temporary loading text in the RecyclerView.
     *
     * @param deviceId the device ID of the entrant whose profile is being loaded
     * @param index the position in the list where the entrant's info should be updated
     */
    private void loadProfileInfo(String deviceId, int index) {
        db.collection("profiles")
                .document(deviceId)
                .get()
                .addOnSuccessListener(profileDoc -> {
                    if (index < 0 || index >= invitedEntrants.size()) return;
                    if (!invitedEntrants.get(index).get("deviceId").equals(deviceId)) return;

                    Map<String, String> entrant = invitedEntrants.get(index);

                    String name = "Unknown user";
                    String email = "";
                    String phone = "";

                    if (profileDoc.exists()) {
                        String fetchedName = profileDoc.getString("name");
                        String fetchedEmail = profileDoc.getString("email");
                        String fetchedPhone = profileDoc.getString("phone");

                        if (fetchedName != null && !fetchedName.isEmpty()) name = fetchedName;
                        if (fetchedEmail != null) email = fetchedEmail;
                        if (fetchedPhone != null) phone = fetchedPhone;
                    }

                    entrant.put("name", name);
                    entrant.put("email", email);
                    entrant.put("phone", phone);

                    adapter.updateData(new ArrayList<>(invitedEntrants));
                })
                .addOnFailureListener(e -> {
                    if (index < 0 || index >= invitedEntrants.size()) return;
                    if (!invitedEntrants.get(index).get("deviceId").equals(deviceId)) return;
                    invitedEntrants.get(index).put("name", "Unknown user");
                    invitedEntrants.get(index).put("email", "");
                    invitedEntrants.get(index).put("phone", "");
                    adapter.updateData(new ArrayList<>(invitedEntrants));
                });
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