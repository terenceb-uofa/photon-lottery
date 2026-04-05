package com.example.getoutthere.organizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.widget.EditText;

import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.getoutthere.R;
import com.google.firebase.Timestamp;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Date;

// This test is AI generated with Anthropics Claude Code, Prompt: "Generate an android test for the following code", Date: Mar 13, 2026


/**
 * Instrumented test to verify the logic and input validation
 * of the Organizer Create Event screen.
 *
 * These tests check that the screen loads correctly, buttons work,
 * and invalid input shows the proper error messages.
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerCreateEventActivityTest {

    /**
     * Launches OrganizerCreateEventActivity before each test.
     */
    @Rule
    public ActivityScenarioRule<OrganizerCreateEventActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerCreateEventActivity.class);

    /**
     * Verifies that the screen title is displayed correctly.
     */
    @Test
    public void screenTitle_isCreateEvent() {
        onView(withId(R.id.screenTitle))
                .check(matches(withText("Create Event")));
    }

    /**
     * Verifies that the upload poster button is enabled when the screen opens.
     */
    @Test
    public void uploadPosterButton_isEnabled() {
        onView(withId(R.id.uploadPosterButton))
                .check(matches(isEnabled()));
    }

    /**
     * Verifies that the create event button is enabled when the screen opens.
     */
    @Test
    public void createEventButton_isEnabled() {
        onView(withId(R.id.createEventButton))
                .perform(scrollTo())
                .check(matches(isEnabled()));
    }

    /**
     * Verifies that pressing the back button closes the activity.
     */
    @Test
    public void backButton_finishesActivity() {
        onView(withId(R.id.backButton)).perform(click());
        assertEquals(Lifecycle.State.DESTROYED, activityRule.getScenario().getState());
    }

    /**
     * Verifies that an error is shown when the event name is empty.
     */
    @Test
    public void createEvent_emptyName_showsError() {
        clickCreateEventButton();

        onView(withId(R.id.nameInput))
                .check(matches(hasErrorText("Event name is required")));
    }

    /**
     * Verifies that an error is shown when the description is empty.
     */
    @Test
    public void createEvent_emptyDescription_showsError() {
        setText(R.id.nameInput, "Sample Event");

        clickCreateEventButton();

        onView(withId(R.id.descriptionInput))
                .check(matches(hasErrorText("Description is required")));
    }

    /**
     * Verifies that an error is shown when the address is empty.
     */
    @Test
    public void createEvent_emptyAddress_showsError() {
        setText(R.id.nameInput, "Sample Event");
        setText(R.id.descriptionInput, "Test description");

        clickCreateEventButton();

        onView(withId(R.id.addressInput))
                .check(matches(hasErrorText("Address is required")));
    }

    /**
     * Verifies that an error is shown when the start date is empty.
     */
    @Test
    public void createEvent_emptyStartDate_showsError() {
        setText(R.id.nameInput, "Sample Event");
        setText(R.id.descriptionInput, "Test description");
        setText(R.id.addressInput, "123 Main Street");

        clickCreateEventButton();

        onView(withId(R.id.startDateInput))
                .check(matches(hasErrorText("Start date is required")));
    }

    /**
     * Verifies that an error is shown when the end date is empty.
     */
    @Test
    public void createEvent_emptyEndDate_showsError() {
        setText(R.id.nameInput, "Sample Event");
        setText(R.id.descriptionInput, "Test description");
        setText(R.id.addressInput, "123 Main Street");
        setDateField(R.id.startDateInput, "2026-03-20 10:00", "startDateTimestamp");

        clickCreateEventButton();

        onView(withId(R.id.endDateInput))
                .check(matches(hasErrorText("End date is required")));
    }

    /**
     * Verifies that an error is shown when the draw date is empty.
     */
    @Test
    public void createEvent_emptyDrawDate_showsError() {
        setText(R.id.nameInput, "Sample Event");
        setText(R.id.descriptionInput, "Test description");
        setText(R.id.addressInput, "123 Main Street");
        setDateField(R.id.startDateInput, "2026-03-20 10:00", "startDateTimestamp");
        setDateField(R.id.endDateInput, "2026-03-20 12:00", "endDateTimestamp");

        clickCreateEventButton();

        onView(withId(R.id.drawDateInput))
                .check(matches(hasErrorText("Draw date is required")));
    }

    /**
     * Verifies that an error is shown when the registration start is empty.
     */
    @Test
    public void createEvent_emptyRegistrationStart_showsError() {
        setText(R.id.nameInput, "Sample Event");
        setText(R.id.descriptionInput, "Test description");
        setText(R.id.addressInput, "123 Main Street");
        setDateField(R.id.startDateInput, "2026-03-20 10:00", "startDateTimestamp");
        setDateField(R.id.endDateInput, "2026-03-20 12:00", "endDateTimestamp");
        setDateField(R.id.drawDateInput, "2026-03-18 09:00", "drawDateTimestamp");

        clickCreateEventButton();

        onView(withId(R.id.registrationStartInput))
                .check(matches(hasErrorText("Registration start is required")));
    }

    /**
     * Verifies that an error is shown when the registration end is empty.
     */
    @Test
    public void createEvent_emptyRegistrationEnd_showsError() {
        setText(R.id.nameInput, "Sample Event");
        setText(R.id.descriptionInput, "Test description");
        setText(R.id.addressInput, "123 Main Street");
        setDateField(R.id.startDateInput, "2026-03-20 10:00", "startDateTimestamp");
        setDateField(R.id.endDateInput, "2026-03-20 12:00", "endDateTimestamp");
        setDateField(R.id.drawDateInput, "2026-03-18 09:00", "drawDateTimestamp");
        setDateField(R.id.registrationStartInput, "2026-03-15 08:00", "registrationStartTimestamp");

        clickCreateEventButton();

        onView(withId(R.id.registrationEndInput))
                .check(matches(hasErrorText("Registration end is required")));
    }


    /**
     * Verifies that an error is shown when the fee is empty.
     */
    @Test
    public void createEvent_emptyFee_showsError() {
        fillFieldsUpToCapacity();

        clickCreateEventButton();

        onView(withId(R.id.feeInput))
                .perform(scrollTo())
                .check(matches(hasErrorText("Fee is required")));
    }

    /**
     * Verifies that an error is shown when capacity is zero.
     */
    @Test
    public void createEvent_zeroCapacity_showsError() {
        fillFieldsUpToFee();
        setText(R.id.capacityInput, "0");
        setText(R.id.feeInput, "10");

        clickCreateEventButton();

        onView(withId(R.id.capacityInput))
                .check(matches(hasErrorText("Capacity must be at least 1")));
    }

    /**
     * Verifies that an error is shown when capacity is negative.
     */
    @Test
    public void createEvent_negativeCapacity_showsError() {
        fillFieldsUpToFee();
        setText(R.id.capacityInput, "-5");
        setText(R.id.feeInput, "10");

        clickCreateEventButton();

        onView(withId(R.id.capacityInput))
                .check(matches(hasErrorText("Capacity must be at least 1")));
    }

    /**
     * Verifies that an error is shown when capacity is not a whole number.
     */
    @Test
    public void createEvent_nonNumericCapacity_showsError() {
        fillFieldsUpToFee();
        setText(R.id.capacityInput, "abc");
        setText(R.id.feeInput, "10");

        clickCreateEventButton();

        onView(withId(R.id.capacityInput))
                .check(matches(hasErrorText("Enter a valid whole number")));
    }

    /**
     * Verifies that an error is shown when the fee is negative.
     */
    @Test
    public void createEvent_negativeFee_showsError() {
        fillFieldsUpToFee();
        setText(R.id.capacityInput, "25");
        setText(R.id.feeInput, "-5");

        clickCreateEventButton();

        onView(withId(R.id.feeInput))
                .perform(scrollTo())
                .check(matches(hasErrorText("Fee cannot be negative")));
    }

    /**
     * Verifies that an error is shown when the fee is not numeric.
     */
    @Test
    public void createEvent_nonNumericFee_showsError() {
        fillFieldsUpToFee();
        setText(R.id.capacityInput, "25");
        setText(R.id.feeInput, "abc");

        clickCreateEventButton();

        onView(withId(R.id.feeInput))
                .perform(scrollTo())
                .check(matches(hasErrorText("Enter a valid fee")));
    }

    /**
     * Verifies that an error is shown when the waitlist limit is zero.
     */
    @Test
    public void createEvent_zeroWaitlistLimit_showsError() {
        fillAllRequiredFields();
        setText(R.id.waitlistLimitInput, "0");

        clickCreateEventButton();

        onView(withId(R.id.waitlistLimitInput))
                .perform(scrollTo())
                .check(matches(hasErrorText("Waitlist limit must be at least 1")));
    }

    /**
     * Verifies that an error is shown when the waitlist limit is negative.
     */
    @Test
    public void createEvent_negativeWaitlistLimit_showsError() {
        fillAllRequiredFields();
        setText(R.id.waitlistLimitInput, "-2");

        clickCreateEventButton();

        onView(withId(R.id.waitlistLimitInput))
                .perform(scrollTo())
                .check(matches(hasErrorText("Waitlist limit must be at least 1")));
    }

    /**
     * Verifies that an error is shown when the waitlist limit is not a whole number.
     */
    @Test
    public void createEvent_nonNumericWaitlistLimit_showsError() {
        fillAllRequiredFields();
        setText(R.id.waitlistLimitInput, "abc");

        clickCreateEventButton();

        onView(withId(R.id.waitlistLimitInput))
                .perform(scrollTo())
                .check(matches(hasErrorText("Enter a valid whole number")));
    }

    /**
     * Clicks the Create Event button after scrolling to it.
     */
    private void clickCreateEventButton() {
        onView(withId(R.id.createEventButton)).perform(scrollTo(), click());
    }

    /**
     * Fills in all required fields up to the capacity field.
     */
    private void fillFieldsUpToCapacity() {
        setText(R.id.nameInput, "Sample Event");
        setText(R.id.descriptionInput, "Test description");
        setText(R.id.addressInput, "123 Main Street");

        setDateField(R.id.startDateInput, "2026-03-20 10:00", "startDateTimestamp");
        setDateField(R.id.endDateInput, "2026-03-20 12:00", "endDateTimestamp");
        setDateField(R.id.drawDateInput, "2026-03-18 09:00", "drawDateTimestamp");
        setDateField(R.id.registrationStartInput, "2026-03-15 08:00", "registrationStartTimestamp");
        setDateField(R.id.registrationEndInput, "2026-03-19 23:59", "registrationEndTimestamp");

        setText(R.id.capacityInput, "50");
    }

    /**
     * Fills in all required fields up to the fee field.
     */
    private void fillFieldsUpToFee() {
        fillFieldsUpToCapacity();
    }

    /**
     * Fills in all required fields with valid values.
     */
    private void fillAllRequiredFields() {
        fillFieldsUpToCapacity();
        setText(R.id.feeInput, "10.0");
    }

    /**
     * Sets text directly into the specified EditText field.
     *
     * @param viewId the ID of the EditText to update
     * @param text the text to enter into the field
     */
    private void setText(int viewId, String text) {
        activityRule.getScenario().onActivity(activity -> {
            EditText editText = activity.findViewById(viewId);
            editText.setText(text);
        });
    }

    /**
     * Sets the displayed date text and updates the matching
     * Timestamp field inside the activity.
     *
     * @param viewId the ID of the EditText to update
     * @param text the date and time string to display
     * @param timestampFieldName the name of the Timestamp field to update
     */
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