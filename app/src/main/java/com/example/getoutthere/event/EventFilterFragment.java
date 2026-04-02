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

// The following code is from Anthropic, Claude, "Add filter fragment with eventType, minCapacity, and date range to EventListActivity", 2026-04-02
public class EventFilterFragment extends BottomSheetDialogFragment {

    public interface FilterListener {
        void onFiltersApplied(String eventType, int minCapacity, long minStartDate, long maxStartDate);
    }

    private FilterListener filterListener;

    private AutoCompleteTextView eventTypeInput;
    private EditText minCapacityInput;
    private EditText minStartDateInput;
    private EditText maxStartDateInput;

    private long minStartDateMillis = 0;
    private long maxStartDateMillis = Long.MAX_VALUE;

    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_filter, container, false);
    }

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
     * displaying it in the appropriate input field.
     *
     * @param isMin true if picking the minimum start date, false for maximum
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