package com.example.getoutthere.event;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class EventTest {

    @Test
    public void testEventGettersAndSetters() {
        // 1. Arrange
        Event event = new Event();

        // 2. Act
        event.setId("test_id_123");
        event.setName("Annual Hackathon");
        event.setCapacity(100);

        // 3. Assert
        assertEquals("test_id_123", event.getId());
        assertEquals("Annual Hackathon", event.getName());
        assertEquals(100, event.getCapacity());
    }
}