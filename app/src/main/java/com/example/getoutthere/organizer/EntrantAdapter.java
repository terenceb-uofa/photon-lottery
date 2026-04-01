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

    /**
     * Constructs an EntrantAdapter with the given list of entrants.
     *
     * @param entrants List of entrant data maps.
     */
    public EntrantAdapter(List<Map<String, String>> entrants) {
        this.entrants = entrants;
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