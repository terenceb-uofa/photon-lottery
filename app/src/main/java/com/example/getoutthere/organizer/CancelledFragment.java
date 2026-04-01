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

public class CancelledFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private String eventId;

    private FirebaseFirestore db;
    private EntrantAdapter adapter;
    private List<Map<String, String>> cancelledEntrants = new ArrayList<>();
    private RecyclerView rvCancelled;

    public CancelledFragment() {}

    public static CancelledFragment newInstance(String eventId) {
        CancelledFragment fragment = new CancelledFragment();
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
        View view = inflater.inflate(R.layout.fragment_cancelled, container, false);

        rvCancelled = view.findViewById(R.id.rvCancelled);
        rvCancelled.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EntrantAdapter(cancelledEntrants);
        rvCancelled.setAdapter(adapter);

        fetchCancelledEntrants();

        return view;
    }

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
                        for (QueryDocumentSnapshot doc : value) {
                            Map<String, String> entrant = new HashMap<>();
                            entrant.put("deviceId", doc.getId());
                            entrant.put("name", doc.getString("name"));
                            entrant.put("email", doc.getString("email"));
                            entrant.put("phone", doc.getString("phone"));
                            entrant.put("status", doc.getString("status"));
                            cancelledEntrants.add(entrant);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}