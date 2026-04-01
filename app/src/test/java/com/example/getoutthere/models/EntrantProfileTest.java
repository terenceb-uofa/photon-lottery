package com.example.getoutthere.models;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Local unit tests for the EntrantProfile model class.
 */
public class EntrantProfileTest {

    @Test
    public void testEmptyConstructor() {
        // Create a profile using the empty constructor
        EntrantProfile profile = new EntrantProfile();

        // Verify all fields initialize as null
        assertNull(profile.getDeviceId());
        assertNull(profile.getName());
        assertNull(profile.getEmail());
        assertNull(profile.getPhoneNumber());
        assertNull(profile.getRole());
    }

    @Test
    public void testParameterizedConstructorAndGetters() {
        // Create a profile with specific data
        EntrantProfile profile = new EntrantProfile("device123", "Jane Doe", "jane@email.com", "1234567890", "user");

        // Verify all getters return the exact data passed into the constructor
        assertEquals("device123", profile.getDeviceId());
        assertEquals("Jane Doe", profile.getName());
        assertEquals("jane@email.com", profile.getEmail());
        assertEquals("1234567890", profile.getPhoneNumber());
        assertEquals("user", profile.getRole());
    }

    @Test
    public void testSetters() {
        // Create an empty profile
        EntrantProfile profile = new EntrantProfile();

        // Update all fields using setters
        profile.setDeviceId("device999");
        profile.setName("John Smith");
        profile.setEmail("john@email.com");
        profile.setPhoneNumber("0987654321");
        profile.setRole("admin");

        // Verify all getters reflect the newly updated data
        assertEquals("device999", profile.getDeviceId());
        assertEquals("John Smith", profile.getName());
        assertEquals("john@email.com", profile.getEmail());
        assertEquals("0987654321", profile.getPhoneNumber());
        assertEquals("admin", profile.getRole());
    }
}