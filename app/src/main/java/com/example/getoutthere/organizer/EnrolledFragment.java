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

import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileWriter;

/**
 * Fragment displaying the list of entrants who have enrolled for an event.
 *
 * <p>Shows all entrants with status "Enrolled". Uses a real-time snapshot
 * listener so the list updates automatically when an entrant accepts an invite.
 * Provides CSV export functionality (US 02.06.03, US 02.06.05).
 *
 * <p>Profile data (name, email, phone) is always fetched from the
 * {@code profiles/{deviceId}} collection, because the waitingList
 * sub-collection stores null for these fields.
 */
public class EnrolledFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private String eventId;

    // Firestore instance
    private FirebaseFirestore db;

    // RecyclerView adapter and data
    private EntrantAdapter adapter;
    private List<Map<String, String>> enrolledEntrants = new ArrayList<>();

    // UI element
    private RecyclerView rvEnrolled;

    public EnrolledFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of EnrolledFragment with the given event ID.
     * @param eventId the Firestore document ID of the event
     * @return a new EnrolledFragment instance
     */
    public static EnrolledFragment newInstance(String eventId) {
        EnrolledFragment fragment = new EnrolledFragment();
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
     * Inflates the fragment layout, initializes UI elements, and begins fetching enrolled entrants.
     * @param inflater the layout inflater
     * @param container the parent view group
     * @param savedInstanceState the saved instance state bundle
     * @return the inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_enrolled, container, false);

        // Initialize RecyclerView
        rvEnrolled = view.findViewById(R.id.rvEnrolled);
        rvEnrolled.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Adapter
        adapter = new EntrantAdapter(enrolledEntrants, eventId);
        rvEnrolled.setAdapter(adapter);

        // Fetch the enrolled entrants from Firebase
        fetchEnrolledEntrants();

        Button btnExportCsv = view.findViewById(R.id.btnExportCsv);
        btnExportCsv.setOnClickListener(v -> previewAndExportCSV());

        Button btnNotifyEnrolled = view.findViewById(R.id.btnNotifyEnrolled);
        btnNotifyEnrolled.setOnClickListener(v -> notifyEnrolledEntrants());

        return view;
    }

    /**
     * Fetches all entrants with status "Enrolled" from Firestore using a real-time snapshot listener.
     * Profile data is loaded via loadProfileInfo() rather than directly from the waitingList document.
     * Implements US 02.06.03.
     */
    private void fetchEnrolledEntrants() {
        if (eventId == null) return;

        // Add a snapshot listener so it updates in real-time
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .whereEqualTo("status", "Enrolled")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("EnrolledFragment", "Listen failed.", error);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error fetching enrolled list", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    enrolledEntrants.clear();
                    if (value != null) {
                        int index = 0;
                        for (QueryDocumentSnapshot doc : value) {
                            String deviceId = doc.getId();
                            Map<String, String> entrant = new HashMap<>();
                            entrant.put("deviceId", deviceId);
                            entrant.put("name", "Loading...");
                            entrant.put("email", "");
                            entrant.put("phone", "");
                            enrolledEntrants.add(entrant);
                            loadProfileInfo(deviceId, index);
                            index++;
                        }
                        adapter.updateData(new ArrayList<>(enrolledEntrants));
                    }
                });
    }

    /**
     * Shows a dialog for the organizer to type a message,
     * then sends a notification to all entrants with status "Enrolled".
     * US 02.07.02 — Organizer sends notifications to all selected entrants.
     */
    private void notifyEnrolledEntrants() {
        if (enrolledEntrants.isEmpty()) {
            Toast.makeText(getContext(), "No enrolled entrants to notify", Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Send Notification to Enrolled Entrants");

        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Type your message...");
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            for (Map<String, String> entrant : enrolledEntrants) {
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
            Toast.makeText(getContext(), "Notified " + enrolledEntrants.size() + " enrolled entrants!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Fetches profile data for a single enrolled entrant from the profiles collection.
     *
     * <p>Profile fields (name, email, phone) are read from
     * {@code profiles/{deviceId}} rather than from the waitingList document,
     * because the teammate's join code writes null for these fields.
     *
     * <p>Two stale-callback safety checks are applied in both success and
     * failure listeners:
     * <ol>
     *   <li>Bounds check: {@code index < 0 || index >= enrolledEntrants.size()}</li>
     *   <li>Identity check: the deviceId at the index must still match</li>
     * </ol>
     *
     * @param deviceId the device ID of the entrant
     * @param index    the index in {@link #enrolledEntrants} for this entrant
     */
    private void loadProfileInfo(String deviceId, int index) {
        db.collection("profiles")
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    // Safety check 1: bounds
                    if (index < 0 || index >= enrolledEntrants.size()) return;
                    // Safety check 2: identity
                    if (!enrolledEntrants.get(index).get("deviceId").equals(deviceId)) return;

                    Map<String, String> entrant = enrolledEntrants.get(index);
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
                    adapter.updateData(new ArrayList<>(enrolledEntrants));
                })
                .addOnFailureListener(e -> {
                    // Safety check 1: bounds
                    if (index < 0 || index >= enrolledEntrants.size()) return;
                    // Safety check 2: identity
                    if (!enrolledEntrants.get(index).get("deviceId").equals(deviceId)) return;

                    Map<String, String> entrant = enrolledEntrants.get(index);
                    entrant.put("name", "Unknown user");
                    entrant.put("email", "");
                    entrant.put("phone", "");
                    adapter.updateData(new ArrayList<>(enrolledEntrants));
                });
    }



    /**
     * Builds a CSV string of enrolled entrants and shows a preview dialog before sharing.
     * Implements US 02.06.05.
     */
    private void previewAndExportCSV() {
        if (enrolledEntrants == null || enrolledEntrants.isEmpty()) {
            Toast.makeText(getContext(), "No enrolled entrants to export", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build the CSV string
        StringBuilder csvData = new StringBuilder();
        csvData.append("Name,Email\n");

        for (Map<String, String> entrant : enrolledEntrants) {
            String name = entrant.get("name") != null ? entrant.get("name").replace(",", "") : "Unknown";
            String email = entrant.get("email") != null ? entrant.get("email").replace(",", "") : "No Email";

            csvData.append(name).append(",").append(email).append("\n");
        }

        // Create the Preview Dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("CSV Preview");

        // Use a ScrollView so large lists don't run off the screen
        android.widget.ScrollView scrollView = new android.widget.ScrollView(getContext());
        android.widget.TextView tvPreview = new android.widget.TextView(getContext());

        tvPreview.setText(csvData.toString());
        tvPreview.setPadding(48, 32, 48, 32);
        tvPreview.setTextSize(14f);
        // Use a monospace font so the CSV columns line up neatly
        tvPreview.setTypeface(android.graphics.Typeface.MONOSPACE);

        scrollView.addView(tvPreview);
        builder.setView(scrollView);

        // Add the Share/Export button
        builder.setPositiveButton("Share File", (dialog, which) -> {
            shareCSVFile(csvData.toString()); // Call the share method if they approve
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    /**
     * Writes CSV content to a temporary file and triggers the Android share sheet.
     * @param csvContent the CSV string to write and share
     */
    private void shareCSVFile(String csvContent) {
        try {
            // Save to a temporary file in the app's cache
            File file = new File(requireContext().getCacheDir(), "enrolled_entrants.csv");
            FileWriter writer = new FileWriter(file);
            writer.write(csvContent);
            writer.close();

            // Generate a secure URI using the FileProvider
            Uri path = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", file);

            // Trigger the Android Share Menu
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Enrolled Entrants List");
            intent.putExtra(Intent.EXTRA_STREAM, path);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Share CSV File"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error sharing CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}