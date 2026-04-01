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
     * Fetches all entrants from the selected event's waiting list collection in Firestore.
     * It stores each entrant's device ID and loads their profile name so it can
     * be displayed in the ListView.
     * If the waiting list is empty it displays "No one is on the waiting list."
     */
    private void loadWaitlistEntrants() {
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    waitlistEntrants.clear();

                    if (querySnapshot.isEmpty()) {
                        adapter.updateData(waitlistEntrants);
                        Toast.makeText(getContext(), "No entrants on waitlist", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String deviceId = doc.getId();

                        String status = doc.getString("status");
                        if (status == null || !status.equals("Waitlist")) {
                            continue;
                        }

                        Map<String, String> entrant = new HashMap<>();
                        entrant.put("deviceId", deviceId);
                        entrant.put("name", "Loading...");
                        entrant.put("email", "");
                        entrant.put("phone", "");

                        waitlistEntrants.add(entrant);
                        int index = waitlistEntrants.size() - 1;

                        loadProfileInfo(deviceId, index);
                    }

                    adapter.updateData(new ArrayList<>(waitlistEntrants));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    /**
     *
     * Fetches the entrant's profile name from Firestore using their device ID.
     * If a name is found, it replaces the temporary loading text in the ListView.
     * If the profile cannot be loaded, the entrant is shown as an unknown user.
     *
     * @param deviceId the device ID of the entrant whose profile is being loaded
     * @param index the position in the list where the entrant's name should be updated
     */
    private void loadProfileInfo(String deviceId, int index) {
        db.collection("profiles")
                .document(deviceId)
                .get()
                .addOnSuccessListener(profileDoc -> {
                    if (index < 0 || index >= waitlistEntrants.size()) return;
                    if (!waitlistEntrants.get(index).get("deviceId").equals(deviceId)) return;

                    Map<String, String> entrant = waitlistEntrants.get(index);

                    String name = "Unknown user";
                    String email = "";
                    String phone = "";

                    if (profileDoc.exists()) {
                        String fetchedName = profileDoc.getString("name");
                        String fetchedEmail = profileDoc.getString("email");
                        String fetchedPhone = profileDoc.getString("phone");

                        if (fetchedName != null && !fetchedName.isEmpty()) {
                            name = fetchedName;
                        }
                        if (fetchedEmail != null) {
                            email = fetchedEmail;
                        }
                        if (fetchedPhone != null) {
                            phone = fetchedPhone;
                        }
                    }

                    entrant.put("name", name);
                    entrant.put("email", email);
                    entrant.put("phone", phone);

                    adapter.updateData(new ArrayList<>(waitlistEntrants));
                })
                .addOnFailureListener(e -> {
                    if (index < 0 || index >= waitlistEntrants.size()) return;
                    if (!waitlistEntrants.get(index).get("deviceId").equals(deviceId)) return;

                    Map<String, String> entrant = waitlistEntrants.get(index);
                    entrant.put("name", "Unknown user");
                    adapter.updateData(new ArrayList<>(waitlistEntrants));
                });
    }

    /**
     * Calculates remaining capacity by checking existing invites/enrollments - randomly looks for replacements from waitlist
     * Sends notifications to participants if selected/not selected
     */
    private void drawLottery() {
        if (waitlistEntrants.isEmpty()) {       // no entrants on waitlist
            Toast.makeText(getContext(), "No entrants on waitlist", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventCapacity == 0) {       // if cant load capacity
            Toast.makeText(getContext(), "Event capacity not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId).get().addOnSuccessListener(eventSnapshot -> {
            String eventName = eventSnapshot.getString("name");
            if (eventName == null) eventName = "the event";
            final String finalEventName = eventName;

            db.collection("events") // See to see how many spots are taken
                    .document(eventId) // look for event id
                    .collection("waitingList") // get all entrants in waitlist
                    .whereIn("status", java.util.Arrays.asList("Invited", "Enrolled")) //see if person is invited/enrolled
                    .get() // get all entrants
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        int takenSpots = queryDocumentSnapshots.size();
                        int availableSpots = eventCapacity - takenSpots; // see whose spots are taken

                        if (availableSpots <= 0) {
                            Toast.makeText(getContext(), "All invites have been sent or event is full.", Toast.LENGTH_LONG).show(); // all spots are taken
                            return;
                        }

                        List<Map<String, String>> selected = LotteryUtils.drawLottery(waitlistEntrants, availableSpots); //  LotteryUtils sampled entrants based ONLY on available spots

                        if (selected.isEmpty()) {
                            Toast.makeText(getContext(), "No eligible entrants to draw.", Toast.LENGTH_SHORT).show(); // no eligible entrants
                            return;
                        }

                        // Create a set of winner IDs
                        java.util.Set<String> winnerIds = new java.util.HashSet<>();
                        for (Map<String, String> winner : selected) {
                            winnerIds.add(winner.get("deviceId"));
                        }

                        // Go through all entrants on the waitlist
                        for (Map<String, String> entrant : waitlistEntrants) {
                            String deviceId = entrant.get("deviceId");
                            boolean won = winnerIds.contains(deviceId);

                            // Update status to "Invited" or "Not Selected"
                            String newStatus = won ? "Invited" : "Not Selected";
                            db.collection("events")
                                    .document(eventId)
                                    .collection("waitingList")
                                    .document(deviceId)
                                    .update("status", newStatus)
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show());


                            // Send the specific "lottery_invite" notification to the winner
                            HashMap<String, Object> notificationData = new HashMap<>();
                            notificationData.put("eventId", eventId);

                            if (won) {
                                notificationData.put("message", "Congratulations! You were selected for " + finalEventName + ". Please accept or decline.");
                                notificationData.put("type", "lottery_invite");
                            } else {
                                notificationData.put("message", "Sorry, you were not selected for " + finalEventName + ".");
                                notificationData.put("type", "lottery_loss");
                            }

                            // Send notification to entrant profiles
                            db.collection("profiles")
                                    .document(deviceId)
                                    .collection("notifications")
                                    .add(notificationData);
                        }

                        Toast.makeText(getContext(), selected.size() + " replacement entrant(s) selected!", Toast.LENGTH_SHORT).show(); // successfully updated status
                        loadWaitlistEntrants(); //update list

                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to verify capacity: " + e.getMessage(), Toast.LENGTH_SHORT).show() //verify fails
                    );
        });

    }

    /**
     * Shows a dialog for the organizer to type a message,
     * then sends a notification to all entrants with status "Waitlist".
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