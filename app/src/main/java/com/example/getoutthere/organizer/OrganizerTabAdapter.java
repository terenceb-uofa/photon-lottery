package com.example.getoutthere.organizer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Acts as the adapter for the organizer's event management tabs.
 * <p>
 * Manages a set of fragments within a ViewPager2, allowing the organizer to swipe
 * between different lists of entrants associated with a specific event. The tabs
 * include views for invited, enrolled, cancelled, and waitlisted entrants.
 * </p>
 *
 * Outstanding Issues:
 * - None right now
 */

/**
 * Adapter providing fragments for the organizer's tabbed event view.
 * @version 1.0
 */
public class OrganizerTabAdapter extends FragmentStateAdapter {

    private String eventId;

    /**
     * Initializes the tab adapter with the host activity and the specific event ID.
     *
     * @param fragmentActivity The host activity where the ViewPager2 is attached.
     * @param eventId The unique identifier of the event being managed.
     */
    public OrganizerTabAdapter(@NonNull FragmentActivity fragmentActivity, String eventId) {
        super(fragmentActivity);
        this.eventId = eventId;
    }

    /**
     * Creates and returns the fragment associated with the specified tab position.
     * Each position corresponds to a specific entrant status list (Invited, Enrolled,
     * Cancelled, Waitlist).
     *
     * @param position The zero-based index of the tab selected.
     * @return A new instance of the appropriate Fragment for the given position.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return InvitedFragment.newInstance(eventId);
            case 1: return EnrolledFragment.newInstance(eventId);
            case 2: return CancelledFragment.newInstance(eventId);
            case 3: return WaitlistFragment.newInstance(eventId);
            default: return WaitlistFragment.newInstance(eventId);
        }
    }

    /**
     * Gets the total number of tabs managed by this adapter.
     *
     * @return The total number of tabs (4).
     */
    @Override
    public int getItemCount() {
        return 4; // 4 tabs
    }
}