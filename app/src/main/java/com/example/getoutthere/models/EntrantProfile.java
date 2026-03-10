package com.example.getoutthere.models;

public class EntrantProfile {
    private String deviceId; // No password login
    private String name;
    private String email;
    private String phoneNumber;
    public EntrantProfile() {}

    public EntrantProfile(String deviceId, String name, String email, String phoneNumber) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
    // Add getters and setters here
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}