package com.example.sirralquran.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.sirralquran.utils.FirebaseHelper;

/**
 * Manages Ramadan-specific logic:
 * - Detects if it's Ramadan
 * - Calculates current Ramadan day
 * - Handles day unlocking logic
 * - Manages 24-hour day completion requirement
 */
public class RamadanManager {

    private static final String TAG = "RamadanManager";
    private static final String PREFS_NAME = "RamadanPrefs";
    private static final String KEY_IS_RAMADAN = "is_ramadan";
    private static final String KEY_CURRENT_RAMADAN_DAY = "current_ramadan_day";
    private static final String KEY_LAST_DAY_COMPLETION_TIME = "last_completion_time_";

    private Context context;
    private SharedPreferences prefs;
    private FirebaseHelper firebaseHelper;

    public RamadanManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.firebaseHelper = FirebaseHelper.getInstance();
    }

    /**
     * Check Ramadan status from Firebase
     */
    public void checkRamadanStatus(OnRamadanStatusListener listener) {
        firebaseHelper.isRamadanActive(new FirebaseHelper.OnDataLoadListener<Boolean>() {
            @Override
            public void onSuccess(Boolean isActive) {
                prefs.edit().putBoolean(KEY_IS_RAMADAN, isActive).apply();

                if (isActive) {
                    // Fetch current Ramadan day from Firebase
                    firebaseHelper.getCurrentRamadanDay(new FirebaseHelper.OnDataLoadListener<Integer>() {
                        @Override
                        public void onSuccess(Integer day) {
                            prefs.edit().putInt(KEY_CURRENT_RAMADAN_DAY, day).apply();
                            listener.onStatusChecked(true, day);
                        }

                        @Override
                        public void onError(String error) {
                            // Use local day if Firebase fails
                            int localDay = prefs.getInt(KEY_CURRENT_RAMADAN_DAY, 1);
                            listener.onStatusChecked(true, localDay);
                        }
                    });
                } else {
                    listener.onStatusChecked(false, 0);
                }
            }

            @Override
            public void onError(String error) {
                // Fallback to local storage
                boolean isRamadan = prefs.getBoolean(KEY_IS_RAMADAN, false);
                int day = prefs.getInt(KEY_CURRENT_RAMADAN_DAY, 0);
                listener.onStatusChecked(isRamadan, day);
            }
        });
    }

    /**
     * Check if Ramadan (from local storage)
     */
    public boolean isRamadan() {
        return prefs.getBoolean(KEY_IS_RAMADAN, false);
    }

    /**
     * Get current Ramadan day (1-30)
     */
    public int getCurrentRamadanDay() {
        return prefs.getInt(KEY_CURRENT_RAMADAN_DAY, 1);
    }

    /**
     * Check if a specific day is unlocked
     * Logic: Day is unlocked if:
     * 1. It's day 1 (always unlocked)
     * 2. Previous day is completed AND 24 hours have passed
     * 3. Day number <= current Ramadan day (can't access future days)
     */
    public boolean isDayUnlocked(int dayNumber) {
        // Can't access days beyond current Ramadan day
        int currentRamadanDay = getCurrentRamadanDay();
        if (dayNumber > currentRamadanDay) {
            return false;
        }

        if (dayNumber == 1) {
            return true; // Day 1 always unlocked
        }

        int previousDay = dayNumber - 1;

        // Check if previous day is completed
        boolean previousDayCompleted = isDayCompleted(previousDay);
        if (!previousDayCompleted) {
            return false;
        }

        // Check if 24 hours have passed since completion
        long completionTime = prefs.getLong(KEY_LAST_DAY_COMPLETION_TIME + previousDay, 0);
        if (completionTime == 0) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - completionTime;
        long twentyFourHours = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

        // FOR TESTING: Change to 1 minute
        // long twentyFourHours = 1 * 60 * 1000; // 1 minute for testing

        return timeDiff >= twentyFourHours;
    }

    /**
     * Mark day as completed
     */
    public void markDayCompleted(int dayNumber) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("day_completed_" + dayNumber, true);
        editor.putLong(KEY_LAST_DAY_COMPLETION_TIME + dayNumber, System.currentTimeMillis());
        editor.apply();

        Log.d(TAG, "Day " + dayNumber + " marked as completed at " + System.currentTimeMillis());
    }

    /**
     * Check if day is completed
     */
    public boolean isDayCompleted(int dayNumber) {
        return prefs.getBoolean("day_completed_" + dayNumber, false);
    }

    /**
     * Get time remaining until next day unlocks (in milliseconds)
     */
    public long getTimeUntilNextDayUnlocks(int dayNumber) {
        if (dayNumber <= 1) {
            return 0; // Day 1 always available
        }

        long completionTime = prefs.getLong(KEY_LAST_DAY_COMPLETION_TIME + (dayNumber - 1), 0);
        if (completionTime == 0) {
            return -1; // Previous day not completed yet
        }

        long twentyFourHours = 24 * 60 * 60 * 1000;
        // FOR TESTING: long twentyFourHours = 1 * 60 * 1000; // 1 minute

        long unlockTime = completionTime + twentyFourHours;
        long currentTime = System.currentTimeMillis();

        return Math.max(0, unlockTime - currentTime);
    }

    /**
     * Get formatted time remaining string
     */
    public String getFormattedTimeRemaining(int dayNumber) {
        long millis = getTimeUntilNextDayUnlocks(dayNumber);

        if (millis == -1) {
            return "Complete Day " + (dayNumber - 1) + " first";
        }

        if (millis == 0) {
            return "Available now";
        }

        long hours = millis / (60 * 60 * 1000);
        long minutes = (millis % (60 * 60 * 1000)) / (60 * 1000);

        return "Unlocks in " + hours + "h " + minutes + "m";
    }

    /**
     * Get maximum accessible day based on current Ramadan day
     */
    public int getMaxAccessibleDay() {
        return getCurrentRamadanDay();
    }

    /**
     * Get completed days count
     */
    public int getCompletedDaysCount() {
        int count = 0;
        int maxDay = getMaxAccessibleDay();
        for (int i = 1; i <= maxDay; i++) {
            if (isDayCompleted(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get progress percentage
     */
    public int getProgressPercentage() {
        int maxDay = getMaxAccessibleDay();
        if (maxDay == 0) return 0;

        int completed = getCompletedDaysCount();
        return (completed * 100) / maxDay;
    }

    /**
     * Reset all progress (for testing)
     */
    public void resetAllProgress() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Interface for Ramadan status callback
     */
    public interface OnRamadanStatusListener {
        void onStatusChecked(boolean isRamadan, int currentDay);
    }
}