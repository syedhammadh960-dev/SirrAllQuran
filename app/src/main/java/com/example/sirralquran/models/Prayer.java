package com.example.sirralquran.models;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * COMPLETE Prayer model with crash fixes
 */
public class Prayer {
    private static final String TAG = "Prayer";

    private String name;
    private String nameArabic;
    private String time;
    private boolean isCompleted;
    private boolean isQaza;
    private boolean hasNotification;
    private int notificationOffset;
    private String offeredTime;

    public Prayer() {
        this.isCompleted = false;
        this.isQaza = false;
        this.hasNotification = true;
        this.notificationOffset = 15;
        this.offeredTime = null;
    }

    public Prayer(String name, String nameArabic, String time) {
        this.name = name;
        this.nameArabic = nameArabic;
        this.time = time;
        this.isCompleted = false;
        this.isQaza = false;
        this.hasNotification = true;
        this.notificationOffset = 15;
        this.offeredTime = null;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameArabic() {
        return nameArabic;
    }

    public void setNameArabic(String nameArabic) {
        this.nameArabic = nameArabic;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
        // Clear qaza when marking as completed
        if (completed && isQaza) {
            isQaza = false;
        }
    }

    public boolean isQaza() {
        return isQaza;
    }

    public void setQaza(boolean qaza) {
        isQaza = qaza;
        // Clear completion when marking as qaza
        if (qaza && isCompleted) {
            isCompleted = false;
            offeredTime = null;
        }
    }

    public boolean hasNotification() {
        return hasNotification;
    }

    public void setHasNotification(boolean hasNotification) {
        this.hasNotification = hasNotification;
    }

    public int getNotificationOffset() {
        return notificationOffset;
    }

    public void setNotificationOffset(int notificationOffset) {
        this.notificationOffset = notificationOffset;
    }

    public String getOfferedTime() {
        return offeredTime;
    }

    public void setOfferedTime(String offeredTime) {
        this.offeredTime = offeredTime;
    }

    /**
     * Get notification offset as readable string
     */
    public String getNotificationOffsetText() {
        if (notificationOffset == 0) return "At prayer time";
        if (notificationOffset < 60) return notificationOffset + " min before";
        return (notificationOffset / 60) + " hour before";
    }

    /**
     * FIXED: Check if prayer time has arrived
     * Handles 12-hour format properly
     */
    public boolean hasPrayerTimeArrived() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            Date prayerTimeDate = sdf.parse(this.time);

            if (prayerTimeDate == null) {
                Log.e(TAG, "Failed to parse prayer time: " + this.time);
                return false;
            }

            Calendar prayerCal = Calendar.getInstance();
            prayerCal.setTime(prayerTimeDate);

            Calendar currentCal = Calendar.getInstance();

            // Compare hours and minutes
            int prayerMinutes = prayerCal.get(Calendar.HOUR_OF_DAY) * 60 +
                    prayerCal.get(Calendar.MINUTE);
            int currentMinutes = currentCal.get(Calendar.HOUR_OF_DAY) * 60 +
                    currentCal.get(Calendar.MINUTE);

            boolean arrived = currentMinutes >= prayerMinutes;

            Log.d(TAG, "Prayer: " + name + " | Time: " + time +
                    " | Current: " + currentMinutes + "min | Prayer: " + prayerMinutes +
                    "min | Arrived: " + arrived);

            return arrived;

        } catch (Exception e) {
            Log.e(TAG, "Error checking prayer time: " + e.getMessage());
            return false;
        }
    }
}