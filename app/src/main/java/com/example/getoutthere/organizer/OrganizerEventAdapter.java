package com.example.getoutthere.organizer;

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
import java.util.List;
import java.util.Locale;

/**
 * Acts as the adapter for displaying events in the organizer's event management feed.
 * <p>
 * Binds {@link Event} data to a custom card layout tailored for organizers. Handles formatting and
 * displaying comprehensive event details such as the title, address, sign-up fee, event date,
 * dynamic status (e.g., OPEN, CLOSED, DRAWN, ENDED), current waitlist count, registration timelines,
 * and loading the event poster using the Glide library.
 * </p>
 *
 * Outstanding Issues:
 * - None
 */

/**
 * Represents the adapter that maps Event objects to their corresponding UI components for organizers.
 * This class handles the rendering of individual event cards with extended management details.
 * @version 1.0
 */
public class OrganizerEventAdapter extends ArrayAdapter<Event> {

    /**
     * Initializes the OrganizerEventAdapter.
     *
     * @param context The current context, used to inflate the layout and access resources.
     * @param events The list of {@link Event} objects to be represented in the UI.
     */
    public OrganizerEventAdapter(@NonNull Context context, @NonNull List<Event> events) {
        super(context, 0, events);
    }

    /**
     * Gets a View that displays the data at the specified position in the data set.
     * Populates the custom layout fields with the specific event's data, handling null checks,
     * formatting dates and currency, evaluating current event status based on system time,
     * and executing image loading via Glide.
     *
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return A View corresponding to the event data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_organizer_event_card, parent, false);
        }

        Event event = getItem(position);

        ImageView imgPoster = view.findViewById(R.id.imgPoster);
        TextView textName = view.findViewById(R.id.textName);
        TextView textAddress = view.findViewById(R.id.textAddress);
        TextView textDay = view.findViewById(R.id.textDay);
        TextView textMonth = view.findViewById(R.id.textMonth);
        TextView textStatus = view.findViewById(R.id.textStatus);
        TextView textWaitlistCount = view.findViewById(R.id.textWaitlistCount);
        TextView textFee = view.findViewById(R.id.textFee);
        TextView textRegistrationStart = view.findViewById(R.id.textRegistrationStart);
        TextView textRegistrationEnd = view.findViewById(R.id.textRegistrationEnd);
        TextView textDrawDate = view.findViewById(R.id.textDrawDate);

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

            // Event Date
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

            // Status text
            java.util.Date now = new java.util.Date();

            Timestamp eventStart = event.getStartDate();
            Timestamp drawDate = event.getDrawDate();
            Timestamp registrationEnd = event.getRegistrationEnd();

            String status = event.getStatus();

            if (status != null && !status.trim().isEmpty()) {
                String normalizedStatus = status.trim().toUpperCase(Locale.getDefault());
                textStatus.setText(normalizedStatus);

                switch (normalizedStatus) {
                    case "ENDED":
                        textStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.error));
                        break;
                    case "DRAWN":
                        textStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.status));
                        break;
                    case "CLOSED":
                        textStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                        break;
                    case "OPEN":
                    default:
                        textStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.success));
                        break;
                }
            } else if (eventStart != null && eventStart.toDate().before(now)) {
                textStatus.setText("ENDED");
                textStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.error));
            } else if (drawDate != null && drawDate.toDate().before(now)) {
                textStatus.setText("DRAWN");
                textStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.status));
            } else if (registrationEnd != null && registrationEnd.toDate().before(now)) {
                textStatus.setText("CLOSED");
                textStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            } else {
                textStatus.setText("OPEN");
                textStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.success));
            }

            // Waitlist count
            textWaitlistCount.setText("Waitlist: " + event.getCurrentWaitlistCount());

            // Registration Dates
            SimpleDateFormat registrationFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

            Timestamp registrationStart = event.getRegistrationStart();
            if (registrationStart != null) {
                textRegistrationStart.setText("Registration opens: " + registrationFormat.format(registrationStart.toDate()));
            } else {
                textRegistrationStart.setText("Registration opens: TBD");
            }

            if (registrationEnd != null) {
                textRegistrationEnd.setText("Registration closes: " + registrationFormat.format(registrationEnd.toDate()));
            } else {
                textRegistrationEnd.setText("Registration closes: TBD");
            }

            // Draw Date
            if (drawDate != null) {
                textDrawDate.setText("Draw date: " + registrationFormat.format(drawDate.toDate()));
            } else {
                textDrawDate.setText("Draw date: TBD");
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