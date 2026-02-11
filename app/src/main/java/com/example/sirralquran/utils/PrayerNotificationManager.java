package com.example.sirralquran.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.sirralquran.R;
import com.example.sirralquran.models.Prayer;
import com.example.sirralquran.receivers.PrayerNotificationReceiver;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * COMPLETE Prayer Notification Manager
 * - Fixed: Unique request codes for each prayer
 * - Fixed: Proper time calculation
 * - Fixed: Detailed logging
 */
public class PrayerNotificationManager {
    private static final String TAG = "PrayerNotification";
    private static final String CHANNEL_ID = "prayer_notifications";
    private static final String CHANNEL_NAME = "Prayer Times";

    // CRITICAL: Unique request codes for each prayer
    private static final int REQUEST_CODE_FAJR = 100;
    private static final int REQUEST_CODE_DHUHR = 200;
    private static final int REQUEST_CODE_ASR = 300;
    private static final int REQUEST_CODE_MAGHRIB = 400;
    private static final int REQUEST_CODE_ISHA = 500;
    private static final int REQUEST_CODE_TEST = 9999;

    private final Context context;
    private final AlarmManager alarmManager;
    private final NotificationManager notificationManager;

    public PrayerNotificationManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();
        checkExactAlarmPermission();
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "✅ Exact alarm permission granted");
            } else {
                Log.e(TAG, "❌ CRITICAL: Exact alarm permission NOT granted!");
            }
        }
    }

    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    public void openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for prayer times");
            channel.enableVibration(true);
            channel.setSound(getNotificationSound(), null);

            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "✅ Notification channel created");
        }
    }

    private Uri getNotificationSound() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    /**
     * Get unique request code for each prayer
     */
    private int getRequestCodeForPrayer(String prayerName) {
        switch (prayerName) {
            case "Fajr": return REQUEST_CODE_FAJR;
            case "Dhuhr": return REQUEST_CODE_DHUHR;
            case "Asr": return REQUEST_CODE_ASR;
            case "Maghrib": return REQUEST_CODE_MAGHRIB;
            case "Isha": return REQUEST_CODE_ISHA;
            default: return 0;
        }
    }

    /**
     * Schedule all prayer notifications
     */
    public void scheduleAllPrayerNotifications(List<Prayer> prayers) {
        Log.d(TAG, "📅 ========================================");
        Log.d(TAG, "📅 SCHEDULING NOTIFICATIONS FOR " + prayers.size() + " PRAYERS");
        Log.d(TAG, "📅 Current time: " + new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH).format(new Date()));
        Log.d(TAG, "📅 ========================================");

        if (!canScheduleExactAlarms()) {
            Log.e(TAG, "❌ CANNOT SCHEDULE: No exact alarm permission!");
            return;
        }

        int scheduled = 0;
        int skipped = 0;
        int disabled = 0;

        for (Prayer prayer : prayers) {
            if (!prayer.hasNotification() || prayer.isCompleted()) {
                if (prayer.isCompleted()) {
                    Log.d(TAG, "⏭️ " + prayer.getName() + " - Already completed");
                } else {
                    Log.d(TAG, "⏭️ " + prayer.getName() + " - Notification disabled");
                }
                disabled++;

                // Cancel any existing alarm for this prayer
                cancelPrayerNotification(getRequestCodeForPrayer(prayer.getName()));
                continue;
            }

            boolean success = schedulePrayerNotification(prayer);
            if (success) {
                scheduled++;
            } else {
                skipped++;
            }
        }

        Log.d(TAG, "📊 Summary: Scheduled=" + scheduled + ", Skipped=" + skipped + ", Disabled=" + disabled);
        Log.d(TAG, "📅 ========================================");
    }

    /**
     * Schedule notification for a single prayer
     */
    public boolean schedulePrayerNotification(Prayer prayer) {
        try {
            int requestCode = getRequestCodeForPrayer(prayer.getName());

            Log.d(TAG, "");
            Log.d(TAG, "🔔 Processing: " + prayer.getName() + " (" + prayer.getNameArabic() + ")");
            Log.d(TAG, "   Request code: " + requestCode);
            Log.d(TAG, "   Prayer time: " + prayer.getTime());
            Log.d(TAG, "   Notification offset: " + prayer.getNotificationOffset() + " minutes");

            Calendar prayerTime = parsePrayerTime(prayer.getTime());

            if (prayerTime == null) {
                Log.e(TAG, "   ❌ FAILED: Could not parse time: " + prayer.getTime());
                return false;
            }

            Calendar originalTime = (Calendar) prayerTime.clone();
            prayerTime.add(Calendar.MINUTE, -prayer.getNotificationOffset());

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH);
            Log.d(TAG, "   Original prayer time: " + sdf.format(originalTime.getTime()));
            Log.d(TAG, "   Notification time: " + sdf.format(prayerTime.getTime()));

            long currentTime = System.currentTimeMillis();
            long notificationTime = prayerTime.getTimeInMillis();
            long timeDifference = notificationTime - currentTime;

            if (timeDifference < 0) {
                Log.d(TAG, "   ⏭️ SKIPPED: Time passed " + Math.abs(timeDifference / 1000) + "s ago");
                return false;
            }

            Log.d(TAG, "   ⏰ Will fire in: " + (timeDifference / 1000) + " seconds (" + (timeDifference / 60000) + " minutes)");

            Intent intent = new Intent(context, PrayerNotificationReceiver.class);
            intent.putExtra("prayer_name", prayer.getName());
            intent.putExtra("prayer_name_arabic", prayer.getNameArabic());
            intent.putExtra("prayer_time", prayer.getTime());
            intent.putExtra("notification_offset", prayer.getNotificationOffset());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,  // Unique for each prayer
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.e(TAG, "   ❌ FAILED: No exact alarm permission!");
                    return false;
                }
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        pendingIntent
                );
                Log.d(TAG, "   ✅ SCHEDULED (Android 12+)");
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        pendingIntent
                );
                Log.d(TAG, "   ✅ SCHEDULED (Android 6+)");
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        pendingIntent
                );
                Log.d(TAG, "   ✅ SCHEDULED (Android <6)");
            }

            Log.d(TAG, "   📌 Alarm set for: " + sdf.format(prayerTime.getTime()));

            return true;

        } catch (Exception e) {
            Log.e(TAG, "   ❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cancel notification for a prayer by request code
     */
    public void cancelPrayerNotification(int requestCode) {
        Intent intent = new Intent(context, PrayerNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "❌ Cancelled notification (code: " + requestCode + ")");
    }

    /**
     * Cancel all prayer notifications
     */
    public void cancelAllNotifications() {
        Log.d(TAG, "❌ Cancelling all notifications...");
        cancelPrayerNotification(REQUEST_CODE_FAJR);
        cancelPrayerNotification(REQUEST_CODE_DHUHR);
        cancelPrayerNotification(REQUEST_CODE_ASR);
        cancelPrayerNotification(REQUEST_CODE_MAGHRIB);
        cancelPrayerNotification(REQUEST_CODE_ISHA);
        Log.d(TAG, "✅ All notifications cancelled");
    }

    /**
     * Parse prayer time string to Calendar
     */
    private Calendar parsePrayerTime(String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            Date time = sdf.parse(timeStr);

            if (time == null) {
                return null;
            }

            Calendar calendar = Calendar.getInstance();
            Calendar prayerCal = Calendar.getInstance();
            prayerCal.setTime(time);

            calendar.set(Calendar.HOUR_OF_DAY, prayerCal.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, prayerCal.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing time: " + timeStr);
            return null;
        }
    }

    /**
     * Schedule test notification in 10 seconds
     */
    public void scheduleTestNotification() {
        Log.d(TAG, "🧪 Scheduling test notification in 10 seconds...");

        if (!canScheduleExactAlarms()) {
            Log.e(TAG, "❌ Cannot schedule test: No permission!");
            return;
        }

        long triggerTime = System.currentTimeMillis() + 10000;

        Intent intent = new Intent(context, PrayerNotificationReceiver.class);
        intent.putExtra("prayer_name", "Test");
        intent.putExtra("prayer_name_arabic", "اختبار");
        intent.putExtra("prayer_time", "Now");
        intent.putExtra("notification_offset", 0);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_TEST,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }

        Log.d(TAG, "✅ Test scheduled for " +
                new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH).format(new Date(triggerTime)));
    }
}