package com.example.sirralquran.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.sirralquran.models.DayLesson;
import com.example.sirralquran.models.RamadanDayContent;
import com.example.sirralquran.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * UPDATED LessonController with Firebase Integration
 * Now fetches real data from Firebase instead of hardcoded content
 */
public class LessonController {

    private static final String TAG = "LessonController";
    private Context context;
    private SharedPreferences sharedPreferences;
    private FirebaseHelper firebaseHelper;
    private RamadanManager ramadanManager;

    private static final String PREFS_NAME = "LessonPrefs";
    private static final String KEY_COMPLETED_DAYS = "completed_days_";

    public LessonController(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.firebaseHelper = FirebaseHelper.getInstance();
        this.ramadanManager = new RamadanManager(context);
    }

    /**
     * Get all 30 lessons with locked/unlocked status
     * Now uses RamadanManager for unlock logic
     */
    public List<DayLesson> getAllLessons() {
        List<DayLesson> lessons = new ArrayList<>();
        int maxAccessibleDay = ramadanManager.getMaxAccessibleDay();

        // Generate all 30 days
        for (int i = 1; i <= 30; i++) {
            boolean isCompleted = ramadanManager.isDayCompleted(i);
            boolean isLocked = !ramadanManager.isDayUnlocked(i);

            // Create basic lesson structure (will be populated from Firebase when opened)
            DayLesson lesson = new DayLesson(
                    i,
                    "Day " + i,
                    "Loading...",
                    "Day " + i + " Theme",
                    i,
                    "Loading...",
                    "Content will load from Firebase",
                    "Dr. Israr Ahmed",
                    "",
                    isCompleted,
                    isLocked
            );

            lessons.add(lesson);
        }

        return lessons;
    }

    /**
     * Get a specific lesson by day number from Firebase
     */
    public void getLessonByDay(int dayNumber, OnLessonLoadListener listener) {
        if (dayNumber < 1 || dayNumber > 30) {
            listener.onError("Invalid day number");
            return;
        }

        // Check if day is unlocked
        if (!ramadanManager.isDayUnlocked(dayNumber)) {
            listener.onError("Day " + dayNumber + " is locked. " +
                    ramadanManager.getFormattedTimeRemaining(dayNumber));
            return;
        }

        // Fetch from Firebase
        firebaseHelper.getDayContent(dayNumber, new FirebaseHelper.OnDataLoadListener<RamadanDayContent>() {
            @Override
            public void onSuccess(RamadanDayContent content) {
                // Convert RamadanDayContent to DayLesson
                DayLesson lesson = convertToDayLesson(content, dayNumber);
                listener.onSuccess(lesson);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load lesson: " + error);
                // Return fallback lesson
                listener.onSuccess(getFallbackLesson(dayNumber));
            }
        });
    }

    /**
     * Convert Firebase RamadanDayContent to DayLesson model
     */
    private DayLesson convertToDayLesson(RamadanDayContent content, int dayNumber) {
        boolean isCompleted = ramadanManager.isDayCompleted(dayNumber);
        boolean isLocked = !ramadanManager.isDayUnlocked(dayNumber);

        // Extract key takeaways as single string
        StringBuilder takeawaysText = new StringBuilder();
        if (content.getKeyTakeaways() != null) {
            for (String takeaway : content.getKeyTakeaways()) {
                takeawaysText.append("• ").append(takeaway).append("\n");
            }
        }

        return new DayLesson(
                dayNumber,
                content.getSurahRange() != null ? content.getSurahRange() : "Day " + dayNumber,
                content.getCoreTheme() != null ? content.getCoreTheme() : "",
                content.getCoreTheme() != null ? content.getCoreTheme() : "Day " + dayNumber,
                content.getJuz(),
                content.getSurahRange() + " (Juz " + content.getJuz() + ")",
                content.getExplanation() != null ? content.getExplanation() : "",
                "\"" + content.getReflectionQuestion() + "\" - Dr. Israr Ahmed",
                content.getVideoUrl() != null ? content.getVideoUrl() : "",
                isCompleted,
                isLocked
        );
    }

    /**
     * Get fallback lesson if Firebase fails
     */
    private DayLesson getFallbackLesson(int dayNumber) {
        boolean isCompleted = ramadanManager.isDayCompleted(dayNumber);
        boolean isLocked = !ramadanManager.isDayUnlocked(dayNumber);

        return new DayLesson(
                dayNumber,
                "Day " + dayNumber,
                "Content loading...",
                "Day " + dayNumber + " Theme",
                dayNumber,
                "Juz " + dayNumber,
                "Content is being loaded from Firebase. Please check your internet connection.",
                "Dr. Israr Ahmed",
                "",
                isCompleted,
                isLocked
        );
    }

    /**
     * Mark lesson as completed (uses RamadanManager)
     */
    public void markLessonCompleted(int dayNumber) {
        ramadanManager.markDayCompleted(dayNumber);
        Log.d(TAG, "Lesson " + dayNumber + " marked as completed");
    }

    /**
     * Check if lesson is completed
     */
    public boolean isLessonCompleted(int dayNumber) {
        return ramadanManager.isDayCompleted(dayNumber);
    }

    /**
     * Get current unlocked day
     */
    public int getCurrentDay() {
        return ramadanManager.getCurrentRamadanDay();
    }

    /**
     * Get count of completed lessons
     */
    public int getCompletedLessonsCount() {
        return ramadanManager.getCompletedDaysCount();
    }

    /**
     * Get progress percentage
     */
    public int getProgressPercentage() {
        return ramadanManager.getProgressPercentage();
    }

    /**
     * Check if user can access a specific day
     */
    public boolean canAccessDay(int dayNumber) {
        return ramadanManager.isDayUnlocked(dayNumber);
    }

    /**
     * Get time remaining for locked day
     */
    public String getTimeRemaining(int dayNumber) {
        return ramadanManager.getFormattedTimeRemaining(dayNumber);
    }

    /**
     * Callback interface for lesson loading
     */
    public interface OnLessonLoadListener {
        void onSuccess(DayLesson lesson);
        void onError(String error);
    }
}