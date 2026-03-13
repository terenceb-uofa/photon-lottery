package com.example.getoutthere.organizer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OrganizerTabAdapter extends FragmentStateAdapter {

    private String eventId;

    public OrganizerTabAdapter(@NonNull FragmentActivity fragmentActivity, String eventId) {
        super(fragmentActivity);
        this.eventId = eventId;
    }

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

    @Override
    public int getItemCount() {
        return 4; // 4 tabs
    }
}
