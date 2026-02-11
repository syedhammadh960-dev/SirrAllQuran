package com.example.sirralquran.models;

public class User {
    private String userId;
    private String fullName;
    private String email;
    private String profileImageUrl;
    private int completedDays;
    private int progressPercentage;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String userId, String fullName, String email) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.completedDays = 0;
        this.progressPercentage = 0;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public int getCompletedDays() {
        return completedDays;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setCompletedDays(int completedDays) {
        this.completedDays = completedDays;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
}