package com.example.getoutthere.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;

/**
 * Unit tests for NotificationUtils.
 */
public class NotificationUtilsTest {

    @Test
    public void testBuildNotification_containsMessage() {
        Map<String, Object> result = NotificationUtils.buildNotification("Hello!", "event123");
        assertEquals("Hello!", result.get("message"));
    }

    @Test
    public void testBuildNotification_containsEventId() {
        Map<String, Object> result = NotificationUtils.buildNotification("Hello!", "event123");
        assertEquals("event123", result.get("eventId"));
    }

    @Test
    public void testBuildNotification_readIsFalse() {
        Map<String, Object> result = NotificationUtils.buildNotification("Hello!", "event123");
        assertEquals(false, result.get("read"));
    }

    @Test
    public void testBuildNotification_containsTimestamp() {
        Map<String, Object> result = NotificationUtils.buildNotification("Hello!", "event123");
        assertNotNull(result.get("timestamp"));
    }

    @Test
    public void testBuildNotification_emptyMessage() {
        Map<String, Object> result = NotificationUtils.buildNotification("", "event123");
        assertEquals("", result.get("message"));
    }

    @Test
    public void testBuildNotification_emptyEventId() {
        Map<String, Object> result = NotificationUtils.buildNotification("Hello!", "");
        assertEquals("", result.get("eventId"));
    }
}