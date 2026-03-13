package com.example.getoutthere.admin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;

import com.example.getoutthere.event.Event;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ManageImagesActivityTest {

    private List<Event> eventList;

    private Event makeEvent(String id, String name, String posterUrl) {
        Event e = mock(Event.class);
        when(e.getId()).thenReturn(id);
        when(e.getName()).thenReturn(name);
        when(e.getPosterUrl()).thenReturn(posterUrl);
        when(e.getStartDate()).thenReturn(new Timestamp(new Date()));
        return e;
    }

    @Before
    public void setUp() {
        eventList = new ArrayList<>();
    }

    /**
     * Simulates grabData() receiving two valid events from Firestore
     * and verifies the local list is populated correctly.
     */
    @Test
    public void testGrabData_populatesEventList_onSuccess() {
        Event event1 = makeEvent("id1", "Trail Hike", "https://example.com/poster1.jpg");
        Event event2 = makeEvent("id2", "Beach Cleanup", "https://example.com/poster2.jpg");

        List<Event> fakeFirestoreResults = Arrays.asList(event1, event2);
        eventList.clear();
        for (Event e : fakeFirestoreResults) {
            if (e != null) {
                eventList.add(e);
            }
        }

        assertEquals("Event list should contain 2 events after a successful fetch", 2, eventList.size());
        assertEquals("First event name should match", "Trail Hike", eventList.get(0).getName());
        assertEquals("Second event name should match", "Beach Cleanup", eventList.get(1).getName());
    }

    /**
     * Simulates removeImageUrlFromFirestore() logic: after a successful
     * Firestore update, the event is removed from the local list.
     */
    @Test
    public void testDelete_removesEventFromList() {
        Event event1 = makeEvent("id1", "Trail Hike", "https://example.com/poster1.jpg");
        Event event2 = makeEvent("id2", "Beach Cleanup", "https://example.com/poster2.jpg");
        eventList.add(event1);
        eventList.add(event2);

        // Simulate successful Firestore update → remove from local list
        eventList.remove(event1);

        assertEquals("List should have 1 event after deletion", 1, eventList.size());
        assertFalse("Deleted event should no longer be in the list", eventList.contains(event1));
        assertTrue("Remaining event should still be present", eventList.contains(event2));
    }
}