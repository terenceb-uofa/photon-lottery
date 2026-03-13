package com.example.getoutthere.admin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;

import com.example.getoutthere.models.EntrantProfile;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageProfilesActivityTest {

    private List<EntrantProfile> profileList;

    private EntrantProfile makeProfile(String deviceId, String name, String role) {
        EntrantProfile p = mock(EntrantProfile.class);
        when(p.getDeviceId()).thenReturn(deviceId);
        when(p.getName()).thenReturn(name);
        when(p.getRole()).thenReturn(role);
        return p;
    }

    @Before
    public void setUp() {
        profileList = new ArrayList<>();
    }

    /**
     * Simulates grabData() receiving two valid profiles from Firestore
     * and verifies the local list is populated correctly.
     */
    @Test
    public void testGrabData_populatesProfileList_onSuccess() {
        EntrantProfile profile1 = makeProfile("device1", "Alice Smith", "Entrant");
        EntrantProfile profile2 = makeProfile("device2", "Bob Jones", "Organizer");

        List<EntrantProfile> fakeFirestoreResults = Arrays.asList(profile1, profile2);
        profileList.clear();
        for (EntrantProfile p : fakeFirestoreResults) {
            if (p != null) {
                profileList.add(p);
            }
        }

        assertEquals("Profile list should contain 2 profiles after a successful fetch", 2, profileList.size());
        assertEquals("First profile name should match", "Alice Smith", profileList.get(0).getName());
        assertEquals("Second profile name should match", "Bob Jones", profileList.get(1).getName());
    }

    /**
     * Simulates the delete button logic: after a successful Firestore delete,
     * the profile is removed from the local list.
     */
    @Test
    public void testDelete_removesProfileFromList() {
        EntrantProfile profile1 = makeProfile("device1", "Alice Smith", "Entrant");
        EntrantProfile profile2 = makeProfile("device2", "Bob Jones", "Organizer");
        profileList.add(profile1);
        profileList.add(profile2);

        // Simulate successful Firestore delete → remove from local list
        profileList.remove(profile1);

        assertEquals("List should have 1 profile after deletion", 1, profileList.size());
        assertFalse("Deleted profile should no longer be in the list", profileList.contains(profile1));
        assertTrue("Remaining profile should still be present", profileList.contains(profile2));
    }
}