package com.example.sirralquran.controllers;

import android.content.Context;
import com.example.sirralquran.models.User;

public class UserController {
    private Context context;
    private AuthController authController;

    public UserController(Context context) {
        this.context = context;
        this.authController = new AuthController(context);
    }

    public User getCurrentUser() {
        String userId = authController.getCurrentUserId();
        String userName = authController.getCurrentUserName();
        String userEmail = authController.getCurrentUserEmail();

        if (userId != null) {
            User user = new User(userId, userName, userEmail);
            user.setCompletedDays(2); // Sample data
            user.setProgressPercentage(7); // Sample data
            return user;
        }

        return null;
    }

    public void updateUserProgress(int completedDays) {
        // Update user progress in backend/SharedPreferences
        int progressPercentage = (completedDays * 100) / 30;
        // Save to SharedPreferences or Firebase
    }

    public int getCompletedDays() {
        // Get from backend/SharedPreferences
        return 2; // Sample data
    }

    public int getProgressPercentage() {
        // Calculate based on completed days
        return (getCompletedDays() * 100) / 30;
    }
}
