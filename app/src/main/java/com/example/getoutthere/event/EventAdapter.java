package com.example.getoutthere.event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.getoutthere.R;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Provides a view for each event in EventListActivity.java, showing event name, draw date
 * and any images.
 */
public class EventAdapter extends ArrayAdapter<Event> {

    private Context context;
    private List<Event> events;

    public EventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.events = events;
    }

    /**
     *
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_event, parent, false);
        }

        Event event = events.get(position);

        TextView eventName = convertView.findViewById(R.id.eventName);
        ImageView eventImage = convertView.findViewById(R.id.eventImage);
        TextView noImageText = convertView.findViewById(R.id.noImageText);
        TextView eventDrawDate = convertView.findViewById(R.id.eventDrawDate);

        // Event name
        eventName.setText(event.getName());

        // Draw date
        if (event.getDrawDate() != null) {
            String formattedDate = new SimpleDateFormat("MM/dd/yyyy")
                    .format(event.getDrawDate().toDate());
            eventDrawDate.setText("Draw Date: " + formattedDate);
        } else {
            eventDrawDate.setText("Draw Date: N/A");
        }

        // Image handling
        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {

            Glide.with(context)
                    .load(event.getPosterUrl())
                    .into(eventImage);

            noImageText.setVisibility(View.GONE);

        } else {
            // If no image, keeps image space, but doesn't load anything
            eventImage.setImageDrawable(null);
            noImageText.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}