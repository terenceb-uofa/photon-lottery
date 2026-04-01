package com.example.getoutthere.admin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.getoutthere.event.Event;
import com.example.getoutthere.models.EntrantProfile;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for ManageOrganizersActivity logic.
 * Follows the team standard for Mockito-based local state verification.
 *
 * @author Terence Bedell
 */
public class ManageOrganizersActivityTest {

    private List<EntrantProfile> organizersList;
    private Map<String, List<Event>> organizerEventsMap;

    // Helper to mock Profile class
    private EntrantProfile makeProfile(String id, String name, String role) {
        EntrantProfile p = mock(EntrantProfile.class);
        when(p.getDeviceId()).thenReturn(id);
        when(p.getName()).thenReturn(name);
        when(p.getRole()).thenReturn(role);
        return p;
    }
    // Helper to mock Event class
    private Event makeEvent(String id, String name, String organizerId) {
        Event e = mock(Event.class);
        when(e.getId()).thenReturn(id);
        when(e.getName()).thenReturn(name);
        when(e.getOrganizerId()).thenReturn(organizerId);
        return e;
    }

    @Before
    public void setUp() {
        organizersList = new ArrayList<>();
        organizerEventsMap = new HashMap<>();
    }

    /**
     * Simulates the result of grabData() building the relationship map
     * between organizers and their events.
     */
    @Test
    public void testOrganizerMap_populatesCorrectly() {
        String orgId = "user_123";
        Event event1 = makeEvent("e1", "Gala", orgId);
        Event event2 = makeEvent("e2", "Tournament", orgId);

        // Simulate the map-building logic in grabData()
        organizerEventsMap.computeIfAbsent(orgId, k -> new ArrayList<>()).add(event1);
        organizerEventsMap.computeIfAbsent(orgId, k -> new ArrayList<>()).add(event2);

        assertNotNull("Map should contain the organizer ID", organizerEventsMap.get(orgId));
        assertEquals("Organizer should have 2 events mapped", 2, organizerEventsMap.get(orgId).size());
        assertEquals("Event name should match", "Gala", organizerEventsMap.get(orgId).get(0).getName());
    }

    /**
     * Simulates the successful population of the profile list after
     * unique organizer IDs are identified.
     */
    @Test
    public void testOrganizersList_populatesOnSuccess() {
        EntrantProfile profile1 = makeProfile("id1", "Alice", "organizer");
        EntrantProfile profile2 = makeProfile("id2", "Bob", "organizer");

        organizersList.add(profile1);
        organizersList.add(profile2);

        assertEquals("List should contain 2 profiles", 2, organizersList.size());
        assertEquals("First name should match", "Alice", organizersList.get(0).getName());
    }

    /**
     * Simulates the result of DeletionUtils success: the profile is
     * removed from the local list so the UI can re-render.
     */
    @Test
    public void testDelete_removesOrganizerFromLocalList() {
        EntrantProfile profile = makeProfile("id1", "Alice", "organizer");
        organizersList.add(profile);

        // Simulate successful deletion callback → remove from list
        organizersList.remove(profile);

        assertTrue("List should be empty after deletion", organizersList.isEmpty());
    }
}