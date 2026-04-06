package com.example.getoutthere.organizer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.getoutthere.R;

import java.util.List;
import java.util.Map;

/**
 * RecyclerView adapter for displaying entrants in organizer fragments.
 * Each item displays the entrant's name and email.
 */
public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    // List of entrant data maps, each containing name, email, phone, status, deviceId
    private List<Map<String, String>> entrants;
    private  String eventId;

    /**
     * Constructs an EntrantAdapter with the given list of entrants.
     *
     * @param entrants List of entrant data maps.
     */
    public EntrantAdapter(List<Map<String, String>> entrants) {
        this.entrants = entrants;
    }
    public EntrantAdapter(List<Map<String, String>> entrants, String eventId) {
        this.entrants = entrants;
        this.eventId = eventId;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        Map<String, String> entrant = entrants.get(position);
        holder.tvEntrantName.setText(entrant.get("name"));
        holder.tvEntrantEmail.setText(entrant.get("email"));

        holder.itemView.setOnClickListener(v -> {
            String selectedName = entrant.get("name");
            String selectedDeviceId = entrant.get("deviceId");

            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Promote to Co-Organizer?")
                    .setMessage("Are you sure you want to promote " + selectedName + "?")
                    .setPositiveButton("Promote", (dialog, which) -> {
                        // Initialize DB
                        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

                        //remove them from the waiting list subcollection (removes them from entrant pool)
                        db.collection("events").document(eventId)
                                .collection("waitingList").document(selectedDeviceId).delete();

                        //decrement the waitlist count on the main Event document
                        db.collection("events").document(eventId)
                                .update("currentWaitlistCount", com.google.firebase.firestore.FieldValue.increment(-1));

                        //construct and send the notification (US 01.09.01)
                        java.util.HashMap<String, Object> notifData = new java.util.HashMap<>();
                        notifData.put("eventId", eventId);
                        notifData.put("message", "You have been invited to be a Co-Organizer for an event!");
                        notifData.put("type", "co_organizer_invite");
                        notifData.put("timestamp", com.google.firebase.Timestamp.now());

                        db.collection("profiles").document(selectedDeviceId)
                                .collection("notifications").add(notifData)
                                .addOnSuccessListener(docRef -> {
                                    android.widget.Toast.makeText(v.getContext(), "Promotion invite sent to " + selectedName, android.widget.Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    android.widget.Toast.makeText(v.getContext(), "Failed to send invite", android.widget.Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    /**
     * Updates the adapter's data and refreshes the list.
     *
     * @param newEntrants New list of entrant data maps.
     */
    public void updateData(List<Map<String, String>> newEntrants) {
        this.entrants = newEntrants;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for entrant items.
     */
    static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView tvEntrantName;
        TextView tvEntrantEmail;

        EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEntrantName = itemView.findViewById(R.id.tvEntrantName);
            tvEntrantEmail = itemView.findViewById(R.id.tvEntrantEmail);
        }
    }
}