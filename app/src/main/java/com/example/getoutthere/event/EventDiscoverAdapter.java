package com.example.getoutthere.event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.getoutthere.R;
import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventDiscoverAdapter extends ArrayAdapter<Event> {

    public EventDiscoverAdapter(@NonNull Context context, @NonNull List<Event> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_discover_event_card, parent, false);
        }

        Event event = getItem(position);

        ImageView imgPoster = view.findViewById(R.id.imgPoster);
        TextView textDay = view.findViewById(R.id.textDay);
        TextView textMonth = view.findViewById(R.id.textMonth);
        TextView textFee = view.findViewById(R.id.textFee);
        TextView textName = view.findViewById(R.id.textName);
        TextView textAddress = view.findViewById(R.id.textAddress);

        if (event != null) {
            // Name
            textName.setText(event.getName() != null ? event.getName() : "Untitled Event");

            // Address
            textAddress.setText(event.getAddress() != null ? event.getAddress() : "Location TBA");

            // Fee
            if (event.getSignupFee() <= 0) {
                textFee.setText("Free");
            } else {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
                textFee.setText(currencyFormat.format(event.getSignupFee()));
            }

            // Date
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

            // Poster
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