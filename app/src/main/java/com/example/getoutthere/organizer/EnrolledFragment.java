package com.example.getoutthere.organizer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getoutthere.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment displaying the enrolled entrants for an event.
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

    public static EnrolledFragment newInstance(String eventId) {
        EnrolledFragment fragment = new EnrolledFragment();
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
        View view = inflater.inflate(R.layout.fragment_enrolled, container, false);

        // Initialize RecyclerView
        rvEnrolled = view.findViewById(R.id.rvEnrolled);
        rvEnrolled.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Adapter
        adapter = new EntrantAdapter(enrolledEntrants);
        rvEnrolled.setAdapter(adapter);

        // Fetch the enrolled entrants from Firebase
        fetchEnrolledEntrants();

        return view;
    }

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
                        for (QueryDocumentSnapshot doc : value) {
                            Map<String, String> entrant = new HashMap<>();
                            entrant.put("deviceId", doc.getId());
                            entrant.put("name", doc.getString("name"));
                            entrant.put("email", doc.getString("email"));
                            entrant.put("phone", doc.getString("phone"));
                            entrant.put("status", doc.getString("status"));
                            enrolledEntrants.add(entrant);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}