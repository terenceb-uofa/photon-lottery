package com.example.getoutthere.event;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.getoutthere.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

/**
 * Acts as a bottom sheet dialogue for filtering events.
 * <p>
 * Provides a user interface for participants to filter the events list by event type,
 * minimum capacity, and a specific date range. It communicates the selected filters
 * back to the parent activity or fragment using a custom listener interface.
 * </p>
 *
 * Outstanding Issues:
 * - No issues
 */

/**
 * Represents the fragment used to configure and apply event search filters.
 * @version 1.0
 */
// The following code is from Anthropic, Claude, "Add filter fragment with eventType, minCapacity, and date range to EventListActivity", 2026-04-02
public class EventFilterFragment extends BottomSheetDialogFragment {

    /**
     * Interface definition for a callback to be invoked when filters are applied.
     */
    public interface FilterListener {
        /**
         * Called when the user applies the configured filters.
         *
         * @param eventType The selected type of event to filter by, or "All" if no specific type is selected.
         * @param minCapacity The minimum capacity limit for the event.
         * @param minStartDate The earliest acceptable start date in milliseconds.
         * @param maxStartDate The latest acceptable start date in milliseconds.
         */
        void onFiltersApplied(String eventType, int minCapacity, long minStartDate, long maxStartDate);
    }

    private FilterListener filterListener;

    private AutoCompleteTextView eventTypeInput;
    private EditText minCapacityInput;
    private EditText minStartDateInput;
    private EditText maxStartDateInput;

    private long minStartDateMillis = 0;
    private long maxStartDateMillis = Long.MAX_VALUE;

    /**
     * Sets the listener that will be notified when the user applies or clears the filters.
     *
     * @param listener The FilterListener implementation to receive filter updates.
     */
    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_filter, container, false);
    }

    /**
     * Called immediately after onCreateView has returned. Sets up the input fields,
     * dropdown adapters, date pickers, and click listeners for the apply and clear buttons.
     *
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventTypeInput = view.findViewById(R.id.filterEventTypeInput);
        minCapacityInput = view.findViewById(R.id.filterMinCapacityInput);
        minStartDateInput = view.findViewById(R.id.filterMinStartDateInput);
        maxStartDateInput = view.findViewById(R.id.filterMaxStartDateInput);
        Button applyButton = view.findViewById(R.id.filterApplyButton);
        Button clearButton = view.findViewById(R.id.filterClearButton);

        // Set up event type dropdown
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.event_types,
                android.R.layout.simple_dropdown_item_1line
        );
        eventTypeInput.setAdapter(adapter);

        // Date pickers
        minStartDateInput.setKeyListener(null);
        maxStartDateInput.setKeyListener(null);

        minStartDateInput.setOnClickListener(v -> showDatePicker(true));
        maxStartDateInput.setOnClickListener(v -> showDatePicker(false));

        applyButton.setOnClickListener(v -> {
            String eventType = eventTypeInput.getText().toString().trim();
            if (eventType.isEmpty()) eventType = "All";

            int minCapacity = 0;
            String minCapacityText = minCapacityInput.getText().toString().trim();
            if (!minCapacityText.isEmpty()) {
                try { minCapacity = Integer.parseInt(minCapacityText); } catch (NumberFormatException ignored) {}
            }

            if (filterListener != null) {
                filterListener.onFiltersApplied(eventType, minCapacity, minStartDateMillis, maxStartDateMillis);
            }
            dismiss();
        });

        clearButton.setOnClickListener(v -> {
            eventTypeInput.setText("");
            minCapacityInput.setText("");
            minStartDateInput.setText("");
            maxStartDateInput.setText("");
            minStartDateMillis = 0;
            maxStartDateMillis = Long.MAX_VALUE;
            if (filterListener != null) {
                filterListener.onFiltersApplied("All", 0, 0, Long.MAX_VALUE);
            }
            dismiss();
        });
    }

    /**
     * Opens a DatePickerDialog and stores the selected date as milliseconds,
     * displaying the formatted date string in the appropriate input field.
     *
     * @param isMin true if picking the minimum start date, false for the maximum start date.
     */
    private void showDatePicker(boolean isMin) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day, isMin ? 0 : 23, isMin ? 0 : 59, isMin ? 0 : 59);
            long millis = selected.getTimeInMillis();
            String formatted = String.format("%04d-%02d-%02d", year, month + 1, day);
            if (isMin) {
                minStartDateMillis = millis;
                minStartDateInput.setText(formatted);
            } else {
                maxStartDateMillis = millis;
                maxStartDateInput.setText(formatted);
            }
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}