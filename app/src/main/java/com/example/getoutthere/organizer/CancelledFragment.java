package com.example.getoutthere.organizer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getoutthere.R;
import com.example.getoutthere.utils.NotificationUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment displaying the list of entrants who have cancelled or declined an event invitation.
 *
 * <p>Shows all entrants with status "Cancelled". Uses a real-time snapshot
 * listener so the list updates automatically when an entrant declines an invite.
 * Implements US 02.06.02.
 *
 * <p>Profile data (name, email, phone) is always fetched from the
 * {@code profiles/{deviceId}} collection, because the waitingList
 * sub-collection stores null for these fields.
 */
public class CancelledFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private String eventId;

    private FirebaseFirestore db;
    private EntrantAdapter adapter;
    private List<Map<String, String>> cancelledEntrants = new ArrayList<>();
    private RecyclerView rvCancelled;



    public CancelledFragment() {}

    /**
     * Creates a new instance of CancelledFragment with the given event ID.
     * @param eventId the Firestore document ID of the event
     * @return a new CancelledFragment instance
     */
    public static CancelledFragment newInstance(String eventId) {
        CancelledFragment fragment = new CancelledFragment();
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
     * Inflates the fragment layout, initializes UI elements, and begins fetching cancelled entrants.
     * @param inflater the layout inflater
     * @param container the parent view group
     * @param savedInstanceState the saved instance state bundle
     * @return the inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cancelled, container, false);

        rvCancelled = view.findViewById(R.id.rvCancelled);
        rvCancelled.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EntrantAdapter(cancelledEntrants, eventId);
        rvCancelled.setAdapter(adapter);

        fetchCancelledEntrants();

        Button btnNotifyCancelled = view.findViewById(R.id.btnNotifyCancelled);
        btnNotifyCancelled.setOnClickListener(v -> notifyCancelledEntrants());

        return view;
    }

    /**
     * Fetches all entrants with status "Cancelled" from Firestore using a real-time snapshot listener.
     * Profile data is loaded via loadProfileInfo() rather than directly from the waitingList document.
     * Implements US 02.06.02.
     */
    private void fetchCancelledEntrants() {
        if (eventId == null) return;

        // Listens for anyone with the status "Cancelled"
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .whereEqualTo("status", "Cancelled")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("CancelledFragment", "Listen failed.", error);
                        return;
                    }

                    cancelledEntrants.clear();
                    if (value != null) {
                        int index = 0;
                        for (QueryDocumentSnapshot doc : value) {
                            String deviceId = doc.getId();
                            Map<String, String> entrant = new HashMap<>();
                            entrant.put("deviceId", deviceId);
                            entrant.put("name", "Loading...");
                            entrant.put("email", "");
                            entrant.put("phone", "");
                            cancelledEntrants.add(entrant);
                            loadProfileInfo(deviceId, index);
                            index++;
                        }
                        adapter.updateData(new ArrayList<>(cancelledEntrants));
                    }
                });
    }

    /**
     * Shows a dialog for the organizer to type a message,
     * then sends a notification to all entrants with status "Cancelled".
     * US 02.07.03 — Organizer sends a notification to all cancelled entrants.
     */
    private void notifyCancelledEntrants() {
        if (cancelledEntrants.isEmpty()) {
            Toast.makeText(getContext(), "No cancelled entrants to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Send Notification to Cancelled Entrants");

        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Type your message...");
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            for (Map<String, String> entrant : cancelledEntrants) {
                String deviceId = entrant.get("deviceId");
                if (deviceId == null) continue;
                Map<String, Object> notification = NotificationUtils.buildNotification(message, eventId);
                db.collection("profiles")
                        .document(deviceId)
                        .collection("notifications")
                        .add(notification)
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Failed to notify " + deviceId, Toast.LENGTH_SHORT).show());
            }
            Toast.makeText(getContext(), "Notified " + cancelledEntrants.size() + " cancelled entrants!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Fetches profile data for a single cancelled entrant from the profiles collection.
     * Applies two stale-callback safety checks in both success and failure listeners.
     * @param deviceId the device ID of the entrant
     * @param index the index in cancelledEntrants for this entrant
     */
    private void loadProfileInfo(String deviceId, int index) {
        db.collection("profiles")
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    // Safety check 1: bounds
                    if (index < 0 || index >= cancelledEntrants.size()) return;
                    // Safety check 2: identity
                    if (!cancelledEntrants.get(index).get("deviceId").equals(deviceId)) return;

                    Map<String, String> entrant = cancelledEntrants.get(index);
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");
                        entrant.put("name", name != null && !name.isEmpty() ? name : "Unknown user");
                        entrant.put("email", email != null ? email : "");
                        entrant.put("phone", phone != null ? phone : "");
                    } else {
                        entrant.put("name", "Unknown user");
                        entrant.put("email", "");
                        entrant.put("phone", "");
                    }
                    adapter.updateData(new ArrayList<>(cancelledEntrants));
                })
                .addOnFailureListener(e -> {
                    // Safety check 1: bounds
                    if (index < 0 || index >= cancelledEntrants.size()) return;
                    // Safety check 2: identity
                    if (!cancelledEntrants.get(index).get("deviceId").equals(deviceId)) return;

                    Map<String, String> entrant = cancelledEntrants.get(index);
                    entrant.put("name", "Unknown user");
                    entrant.put("email", "");
                    entrant.put("phone", "");
                    adapter.updateData(new ArrayList<>(cancelledEntrants));
                });
    }
}