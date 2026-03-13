package com.example.getoutthere.event;

import static org.junit.Assert.*;

import com.google.firebase.Timestamp;
import org.junit.Before;
import org.junit.Test;

// Tests that the Event model is set up properly

public class EventTest {
    private Event event;

    @Before
    public void setUp() {
        event = new Event();
    }

    /**
     * Test - Initialize default values with expected values
     */
    @Test
    public void testDefaultValues() {
        assertEquals(0, event.getCapacity());
        assertEquals(0, event.getCurrentWaitlistCount());
        assertNull(event.getName());
        assertNull(event.getAddress());
        assertNull(event.getDescription());
    }

    /**
     * Test - Setters and getters work correctly
     */
    @Test
    public void testSetAndGetFields() {
        event.setName("Test Event");
        event.setCapacity(10);
        event.setCurrentWaitlistCount(3);
        event.setAddress("123 Main St, AB 1T1 2Y2");
        event.setDescription("Description of a test event");

        assertEquals("Test Event", event.getName());
        assertEquals(10, event.getCapacity());
        assertEquals(3, event.getCurrentWaitlistCount());
        assertEquals("123 Main St, AB 1T1 2Y2", event.getAddress());
        assertEquals("Description of a test event", event.getDescription());
    }

    /**
     * Test - Number of available spots calculated correctly.
     * Users "join" and "leave", and checks if capacity and spots available updated.
     */
    @Test
    public void testSpotsAvailableCalculation() {
        event.setCapacity(10);
        event.setCurrentWaitlistCount(4); // Initially 4 people inside

        int spotsAvailable = event.getCapacity() - event.getCurrentWaitlistCount();
        assertEquals(6, spotsAvailable);

        // 1 user joins - Should have 5 spots available
        event.setCurrentWaitlistCount(event.getCurrentWaitlistCount() + 1);
        spotsAvailable = event.getCapacity() - event.getCurrentWaitlistCount();
        assertEquals(5, spotsAvailable);

        // 2 users leave - Should have 7 spots available
        event.setCurrentWaitlistCount(event.getCurrentWaitlistCount() - 2);
        spotsAvailable = event.getCapacity() - event.getCurrentWaitlistCount();
        assertEquals(7, spotsAvailable);
    }

    /**
     * Test - Ensures "date" fields are stored and retrieved as timestamp objects.
     */
    @Test
    public void testTimestamps() {
        Timestamp now = Timestamp.now();
        event.setStartDate(now);
        event.setEndDate(now);
        event.setDrawDate(now);

        assertEquals(now, event.getStartDate());
        assertEquals(now, event.getEndDate());
        assertEquals(now, event.getDrawDate());
    }
}