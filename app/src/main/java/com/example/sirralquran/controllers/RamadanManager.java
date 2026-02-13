package com.example.sirralquran.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.sirralquran.utils.FirebaseHelper;
import java.util.Calendar;

/**
 * ✅ FIXED: Smart unlock logic with catch-up mode
 *
 * UNLOCK LOGIC:
 * 1. Day 1: Always unlocked
 * 2. User BEHIND Ramadan (e.g., Ramadan Day 5, user completed Day 1):
 *    → Next day unlocks IMMEDIATELY after completion (catch-up mode)
 * 3. User CAUGHT UP with Ramadan (e.g., Ramadan Day 5, user completed Day 4):
 *    → Next day unlocks at 5:00 AM (normal mode)
 *
 * EXAMPLE SCENARIOS:
 * - Ramadan Day 5, User completes Day 1 → Day 2 unlocks IMMEDIATELY ✅
 * - Ramadan Day 5, User completes Day 2 → Day 3 unlocks IMMEDIATELY ✅
 * - Ramadan Day 5, User completes Day 3 → Day 4 unlocks IMMEDIATELY ✅
 * - Ramadan Day 5, User completes Day 4 → Day 5 unlocks at 5:00 AM next morning ⏰
 * - Ramadan Day 5, User completes Day 5 → Day 6 unlocks at 5:00 AM next morning ⏰
 */
public class RamadanManager {

    private static final String TAG = "RamadanManager";
    private static final String PREFS_NAME = "RamadanPrefs";
    private static final String KEY_IS_RAMADAN = "is_ramadan";
    private static final String KEY_CURRENT_RAMADAN_DAY = "current_ramadan_day";
    private static final String KEY_DAY_COMPLETED = "day_completed_";
    private static final String KEY_DAY_COMPLETION_TIME = "day_completion_time_";

    // 5:00 AM unlock time
    private static final int UNLOCK_HOUR = 5;
    private static final int UNLOCK_MINUTE = 0;

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
                    firebaseHelper.getCurrentRamadanDay(new FirebaseHelper.OnDataLoadListener<Integer>() {
                        @Override
                        public void onSuccess(Integer day) {
                            prefs.edit().putInt(KEY_CURRENT_RAMADAN_DAY, day).apply();
                            listener.onStatusChecked(true, day);
                        }

                        @Override
                        public void onError(String error) {
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
                boolean isRamadan = prefs.getBoolean(KEY_IS_RAMADAN, false);
                int day = prefs.getInt(KEY_CURRENT_RAMADAN_DAY, 0);
                listener.onStatusChecked(isRamadan, day);
            }
        });
    }

    public boolean isRamadan() {
        return prefs.getBoolean(KEY_IS_RAMADAN, false);
    }

    public int getCurrentRamadanDay() {
        return prefs.getInt(KEY_CURRENT_RAMADAN_DAY, 1);
    }

    /**
     * ✅ FIXED: Smart unlock logic with catch-up mode
     *
     * LOGIC:
     * 1. Day 1: Always unlocked
     * 2. Check if previous day completed
     * 3. Check if user is BEHIND current Ramadan day:
     *    - If YES → Unlock immediately (catch-up mode)
     *    - If NO → Wait until 5:00 AM (normal mode)
     */
    public boolean isDayUnlocked(int dayNumber) {
        // Can't access days beyond current Ramadan day
        int currentRamadanDay = getCurrentRamadanDay();
        if (dayNumber > currentRamadanDay) {
            Log.d(TAG, "🔒 Day " + dayNumber + " locked: Beyond Ramadan day " + currentRamadanDay);
            return false;
        }

        // Day 1 always unlocked
        if (dayNumber == 1) {
            Log.d(TAG, "✅ Day 1 always unlocked");
            return true;
        }

        int previousDay = dayNumber - 1;

        // ✅ CONDITION 1: Previous day must be completed
        boolean previousDayCompleted = isDayCompleted(previousDay);
        if (!previousDayCompleted) {
            Log.d(TAG, "🔒 Day " + dayNumber + " locked: Day " + previousDay + " not completed");
            return false;
        }

        // ✅ NEW: Check if user is BEHIND current Ramadan day
        if (dayNumber < currentRamadanDay) {
            // CATCH-UP MODE: Unlock immediately
            Log.d(TAG, "⚡ Day " + dayNumber + " UNLOCKED (Catch-up mode: Ramadan Day " +
                    currentRamadanDay + " > User Day " + dayNumber + ")");
            return true;
        }

        // ✅ NORMAL MODE: User is caught up, use 5:00 AM logic
        long completionTime = prefs.getLong(KEY_DAY_COMPLETION_TIME + previousDay, 0);
        if (completionTime == 0) {
            Log.d(TAG, "🔒 Day " + dayNumber + " locked: No completion time for day " + previousDay);
            return false;
        }

        long nextUnlockTime = getNext5AM(completionTime);
        long currentTime = System.currentTimeMillis();
        boolean isUnlocked = currentTime >= nextUnlockTime;

        if (isUnlocked) {
            Log.d(TAG, "✅ Day " + dayNumber + " UNLOCKED at 5:00 AM (Normal mode)");
        } else {
            long remainingMs = nextUnlockTime - currentTime;
            Log.d(TAG, "🔒 Day " + dayNumber + " locked: Unlocks in " +
                    formatDuration(remainingMs) + " at " + formatTime(nextUnlockTime));
        }

        return isUnlocked;
    }

    /**
     * Calculate next 5:00 AM after given timestamp
     */
    private long getNext5AM(long fromTime) {
        Calendar completionCal = Calendar.getInstance();
        completionCal.setTimeInMillis(fromTime);

        Calendar next5AM = Calendar.getInstance();
        next5AM.setTimeInMillis(fromTime);
        next5AM.set(Calendar.HOUR_OF_DAY, UNLOCK_HOUR);
        next5AM.set(Calendar.MINUTE, UNLOCK_MINUTE);
        next5AM.set(Calendar.SECOND, 0);
        next5AM.set(Calendar.MILLISECOND, 0);

        // If completion time is after today's 5 AM, move to tomorrow's 5 AM
        if (completionCal.after(next5AM)) {
            next5AM.add(Calendar.DAY_OF_MONTH, 1);
        }

        return next5AM.getTimeInMillis();
    }

    /**
     * Mark day as completed
     */
    public void markDayCompleted(int dayNumber) {
        long currentTime = System.currentTimeMillis();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_DAY_COMPLETED + dayNumber, true);
        editor.putLong(KEY_DAY_COMPLETION_TIME + dayNumber, currentTime);
        editor.apply();

        int currentRamadanDay = getCurrentRamadanDay();
        int nextDay = dayNumber + 1;

        // Check unlock mode for next day
        if (nextDay < currentRamadanDay) {
            // Catch-up mode
            Log.d(TAG, "✅ Day " + dayNumber + " completed at " + formatTime(currentTime));
            Log.d(TAG, "⚡ Day " + nextDay + " unlocks IMMEDIATELY (catch-up mode)");
        } else {
            // Normal mode
            long nextUnlockTime = getNext5AM(currentTime);
            Log.d(TAG, "✅ Day " + dayNumber + " completed at " + formatTime(currentTime));
            Log.d(TAG, "🔓 Day " + nextDay + " unlocks at " + formatTime(nextUnlockTime) + " (5:00 AM)");
        }
    }

    public boolean isDayCompleted(int dayNumber) {
        return prefs.getBoolean(KEY_DAY_COMPLETED + dayNumber, false);
    }

    /**
     * ✅ FIXED: Get time remaining with catch-up mode support
     */
    public long getTimeUntilNextDayUnlocks(int dayNumber) {
        if (dayNumber <= 1) {
            return 0;
        }

        int previousDay = dayNumber - 1;
        int currentRamadanDay = getCurrentRamadanDay();

        // Check if previous day is completed
        if (!isDayCompleted(previousDay)) {
            return -1;
        }

        // ✅ CATCH-UP MODE: If behind Ramadan, unlock immediately
        if (dayNumber < currentRamadanDay) {
            return 0; // Already unlocked
        }

        // ✅ NORMAL MODE: Use 5:00 AM logic
        long completionTime = prefs.getLong(KEY_DAY_COMPLETION_TIME + previousDay, 0);
        if (completionTime == 0) {
            return -1;
        }

        long nextUnlockTime = getNext5AM(completionTime);
        long currentTime = System.currentTimeMillis();

        return Math.max(0, nextUnlockTime - currentTime);
    }

    /**
     * ✅ FIXED: Get formatted time remaining with catch-up mode
     */
    public String getFormattedTimeRemaining(int dayNumber) {
        if (dayNumber <= 1) {
            return "✅ Available now";
        }

        int previousDay = dayNumber - 1;
        int currentRamadanDay = getCurrentRamadanDay();

        // Check if previous day is completed
        if (!isDayCompleted(previousDay)) {
            return "🔒 Complete Day " + previousDay + " first";
        }

        // ✅ CATCH-UP MODE
        if (dayNumber < currentRamadanDay) {
            return "✅ Available now (Catch-up mode)";
        }

        // ✅ NORMAL MODE
        long millis = getTimeUntilNextDayUnlocks(dayNumber);

        if (millis == -1) {
            return "🔒 Complete Day " + previousDay + " first";
        }

        if (millis == 0) {
            return "✅ Available now";
        }

        long hours = millis / (60 * 60 * 1000);
        long minutes = (millis % (60 * 60 * 1000)) / (60 * 1000);

        long completionTime = prefs.getLong(KEY_DAY_COMPLETION_TIME + previousDay, 0);
        long unlockTime = getNext5AM(completionTime);
        String unlockTimeStr = formatTime(unlockTime);

        if (hours > 0) {
            return "🔒 Unlocks in " + hours + "h " + minutes + "m at " + unlockTimeStr;
        } else {
            return "🔒 Unlocks in " + minutes + " minutes at " + unlockTimeStr;
        }
    }

    public int getMaxAccessibleDay() {
        return getCurrentRamadanDay();
    }

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

    public int getProgressPercentage() {
        int maxDay = getMaxAccessibleDay();
        if (maxDay == 0) return 0;

        int completed = getCompletedDaysCount();
        return (completed * 100) / maxDay;
    }

    public void resetAllProgress() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        Log.d(TAG, "🔄 All progress reset");
    }

    private String formatTime(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        String ampm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);

        return String.format("%d:%02d %s", displayHour, minute, ampm);
    }

    private String formatDuration(long millis) {
        long hours = millis / (60 * 60 * 1000);
        long minutes = (millis % (60 * 60 * 1000)) / (60 * 1000);

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + " minutes";
        }
    }

    public interface OnRamadanStatusListener {
        void onStatusChecked(boolean isRamadan, int currentDay);
    }
}