package com.example.sirralquran.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.sirralquran.R;
import com.example.sirralquran.views.SalahTrackerActivity;

/**
 * BroadcastReceiver to show prayer notifications
 */
public class PrayerNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "PrayerReceiver";
    private static final String CHANNEL_ID = "prayer_notifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        String prayerName = intent.getStringExtra("prayer_name");
        String prayerNameArabic = intent.getStringExtra("prayer_name_arabic");
        String prayerTime = intent.getStringExtra("prayer_time");
        int offset = intent.getIntExtra("notification_offset", 0);

        Log.d(TAG, "📢 Notification triggered for: " + prayerName);

        // Check permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "⚠️ Notification permission not granted");
                return;
            }
        }

        // Create notification channel
        createNotificationChannel(context);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getNotificationTitle(prayerName, offset))
                .setContentText(prayerNameArabic + " - " + prayerTime)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getNotificationMessage(prayerName, offset)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(getNotificationSound(context))
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setContentIntent(getPendingIntent(context));

        // Show notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(prayerName.hashCode(), builder.build());

        Log.d(TAG, "✅ Notification shown for " + prayerName);
    }

    /**
     * Create notification channel (Android 8.0+)
     */
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Prayer Times",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for prayer times");
            channel.enableVibration(true);
            channel.setSound(getNotificationSound(context), null);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Get notification title based on offset
     */
    private String getNotificationTitle(String prayerName, int offset) {
        if (offset == 0) {
            return "🕌 " + prayerName + " Time Now!";
        } else if (offset < 60) {
            return "🕌 " + prayerName + " in " + offset + " minutes";
        } else {
            return "🕌 " + prayerName + " in " + (offset / 60) + " hour";
        }
    }

    /**
     * Get notification message
     */
    private String getNotificationMessage(String prayerName, int offset) {
        if (offset == 0) {
            return "It's time for " + prayerName + " prayer. Don't miss it!";
        } else {
            return prayerName + " prayer is coming soon. Prepare for salah.";
        }
    }

    /**
     * Get notification sound (uses default)
     */
    private Uri getNotificationSound(Context context) {
        // Use default notification sound
        //return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // TODO: To use custom adhan sound in future:
        // 1. Get adhan.mp3 audio file
        // 2. Create folder: app/src/main/res/raw/
        // 3. Place adhan.mp3 in res/raw/
        // 4. Uncomment this line:
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.adhan);
    }

    /**
     * Get pending intent to open app
     */
    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, SalahTrackerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}