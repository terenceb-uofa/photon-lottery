package com.example.getoutthere.models;

/**
 * Represents a user profile within the application.
 * <p>
 * This model class stores basic user information such as their device ID,
 * name, contact information, and their account role (user or admin).
 * The application relies on the device ID for passwordless authentication.
 */
public class EntrantProfile {
    private String deviceId; // No password login
    private String name;
    private String email;
    private String phoneNumber;
    private String accountType;
    private boolean notificationsEnabled;

    /**
     * Default constructor required for Firebase Firestore
     */
    public EntrantProfile() {}

    /**
     * Constructs a new EntrantProfile with all required user details.
     *
     * @param deviceId             The unique Android device ID used for authentication.
     * @param name                 The display name of the user.
     * @param email                The email address of the user.
     * @param phoneNumber          The contact phone number of the user.
     * @param accountType          The role of the user (e.g., "user").
     * @param notificationsEnabled Whether user opted in to receiving notifications.
     */
    public EntrantProfile(String deviceId, String name, String email, String phoneNumber, String accountType, boolean notificationsEnabled) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.accountType = accountType;
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * Gets the unique device ID associated with this profile.
     * @return The user's device ID.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the unique device ID for this profile.
     * @param deviceId The unique Android device ID.
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the user's display name.
     * @return The user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's display name.
     * @param name The new name for the user.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's email address.
     * @return The user's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * @param email The new email for the user.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's phone number.
     * @return The user's phone number.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the user's phone number.
     * @param phoneNumber The new phone number.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the account role/type for the user.
     * @return The account type (e.g., "user").
     */
    public String getRole() {
        return accountType;
    }

    /**
     * Sets the account role/type for the user.
     * @param accountType The new role for the user.
     */
    public void setRole(String accountType) {
        this.accountType = accountType;
    }

    /**
     * Gets whether notifications are enabled for this user.
     * @return true if enabled, false otherwise.
     */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Sets whether notifications are enabled for this user.
     * @param notificationsEnabled the new preference.
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}