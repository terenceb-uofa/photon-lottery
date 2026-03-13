package com.example.getoutthere.organizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.widget.EditText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;
import com.google.firebase.Timestamp;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Instrumented test to verify the logic and data saving of OrganizerCreateEvent screen
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerCreateEventActivityTest {

    @Rule
    public ActivityScenarioRule<OrganizerCreateEventActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerCreateEventActivity.class);

    @Test
    public void screenTitle_isCreateEvent() {
        onView(withId(R.id.screenTitle))
                .check(matches(withText("Create Event")));
    }

    @Test
    public void createEvent_emptyName_showsError() {
        onView(withId(R.id.createEventButton)).perform(click());

        onView(withId(R.id.nameInput))
                .check(matches(hasErrorText("Event name is required")));
    }

    @Test
    public void createEvent_invalidCapacity_showsError() {
        fillFieldsUpToFee();
        setText(R.id.capacityInput, "0");
        setText(R.id.feeInput, "10");

        onView(withId(R.id.createEventButton)).perform(click());

        onView(withId(R.id.capacityInput))
                .check(matches(hasErrorText("Capacity must be at least 1")));
    }

    @Test
    public void createEvent_negativeFee_showsError() {
        fillFieldsUpToFee();
        setText(R.id.capacityInput, "25");
        setText(R.id.feeInput, "-5");

        onView(withId(R.id.createEventButton)).perform(click());

        onView(withId(R.id.feeInput))
                .check(matches(hasErrorText("Fee cannot be negative")));
    }

    private void fillFieldsUpToFee() {
        setText(R.id.nameInput, "Sample Event");
        setText(R.id.descriptionInput, "Test description");
        setText(R.id.addressInput, "123 Main Street");

        setDateField(R.id.startDateInput, "2026-03-20 10:00", "startDateTimestamp");
        setDateField(R.id.endDateInput, "2026-03-20 12:00", "endDateTimestamp");
        setDateField(R.id.drawDateInput, "2026-03-18 09:00", "drawDateTimestamp");
        setDateField(R.id.registrationStartInput, "2026-03-15 08:00", "registrationStartTimestamp");
        setDateField(R.id.registrationEndInput, "2026-03-19 23:59", "registrationEndTimestamp");
    }

    private void setText(int viewId, String text) {
        activityRule.getScenario().onActivity(activity -> {
            EditText editText = activity.findViewById(viewId);
            editText.setText(text);
        });
    }

    private void setDateField(int viewId, String text, String timestampFieldName) {
        activityRule.getScenario().onActivity(activity -> {
            EditText editText = activity.findViewById(viewId);
            editText.setText(text);

            try {
                Field field = OrganizerCreateEventActivity.class.getDeclaredField(timestampFieldName);
                field.setAccessible(true);
                field.set(activity, new Timestamp(new Date()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}