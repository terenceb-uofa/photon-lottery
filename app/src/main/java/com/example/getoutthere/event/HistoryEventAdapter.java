package com.example.getoutthere.entrant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.getoutthere.R;
import com.example.getoutthere.event.Event;
import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


// The following code is from Anthropic, Claude, "Take the given OrganizerEventListActivity, OrganizerEventAdapter, EventListActivity, EventDiscoverAdapter, and rewrite EventHistory and create HistoryEventAdapter it to work for the given history ui xml files", 2026-04-06


public class HistoryEventAdapter extends ArrayAdapter<Event> {

    private final Map<String, String> eventStatuses;

    public HistoryEventAdapter(@NonNull Context context,
                               @NonNull List<Event> events,
                               @NonNull Map<String, String> eventStatuses) {
        super(context, 0, events);
        this.eventStatuses = eventStatuses;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_history_event_card, parent, false);
        }

        Event event = getItem(position);

        ImageView imgPoster = view.findViewById(R.id.imgPoster);
        TextView textName = view.findViewById(R.id.textName);
        TextView textAddress = view.findViewById(R.id.textAddress);
        TextView textFee = view.findViewById(R.id.textFee);
        TextView textDay = view.findViewById(R.id.textDay);
        TextView textMonth = view.findViewById(R.id.textMonth);
        TextView textHistoryStatus = view.findViewById(R.id.textHistoryStatus);

        if (event != null) {
            textName.setText(event.getName() != null ? event.getName() : "Untitled Event");
            textAddress.setText(event.getAddress() != null ? event.getAddress() : "Location TBA");

            if (event.getSignupFee() <= 0) {
                textFee.setText("Free");
            } else {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
                textFee.setText(currencyFormat.format(event.getSignupFee()));
            }

            Timestamp startTimestamp = event.getStartDate();
            if (startTimestamp != null) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
                textDay.setText(dayFormat.format(startTimestamp.toDate()));
                textMonth.setText(monthFormat.format(startTimestamp.toDate()).toUpperCase(Locale.getDefault()));
            } else {
                textDay.setText("--");
                textMonth.setText("---");
            }

            String status = eventStatuses.get(event.getId());
            if (status == null || status.trim().isEmpty()) {
                status = "JOINED";
            }

            String displayStatus = status.toUpperCase(Locale.getDefault());
            textHistoryStatus.setText(displayStatus);

            switch (displayStatus) {
                case "SELECTED":
                case "INVITED":
                    textHistoryStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.success));
                    break;
                case "CANCELLED":
                case "REMOVED":
                    textHistoryStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.error));
                    break;
                default:
                    textHistoryStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    break;
            }

            String posterUrl = event.getPosterUrl();
            if (posterUrl != null && !posterUrl.isEmpty()) {
                Glide.with(getContext())
                        .load(posterUrl)
                        .placeholder(R.drawable.sample_event_poster)
                        .error(R.drawable.sample_event_poster)
                        .centerCrop()
                        .into(imgPoster);
            } else {
                imgPoster.setImageResource(R.drawable.sample_event_poster);
            }
        }

        return view;
    }
}