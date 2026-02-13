package com.example.sirralquran.views;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sirralquran.R;
import com.example.sirralquran.utils.PrayerNotificationManager;
import com.google.android.material.card.MaterialCardView;

/**
 * Test screen for notification timing and tone
 */
public class NotificationTestActivity extends AppCompatActivity {

    private static final String TAG = "NotificationTest";

    private RadioGroup timingRadioGroup;
    private RadioButton timing10sec;
    private RadioButton timing30sec;
    private RadioButton timing1min;
    private RadioButton timing2min;
    private RadioButton timing5min;

    private Button testNotificationButton;
    private Button checkPermissionsButton;
    private TextView statusText;
    private MaterialCardView permissionCard;

    private PrayerNotificationManager notificationManager;
    private int selectedSeconds = 10; // Default 10 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_test);

        initializeViews();
        notificationManager = new PrayerNotificationManager(this);

        setupListeners();
        checkPermissionStatus();
    }

    private void initializeViews() {
        timingRadioGroup = findViewById(R.id.timingRadioGroup);
        timing10sec = findViewById(R.id.timing10sec);
        timing30sec = findViewById(R.id.timing30sec);
        timing1min = findViewById(R.id.timing1min);
        timing2min = findViewById(R.id.timing2min);
        timing5min = findViewById(R.id.timing5min);

        testNotificationButton = findViewById(R.id.testNotificationButton);
        checkPermissionsButton = findViewById(R.id.checkPermissionsButton);
        statusText = findViewById(R.id.statusText);
        permissionCard = findViewById(R.id.permissionCard);
    }

    private void setupListeners() {
        // Timing selection
        timingRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.timing10sec) {
                selectedSeconds = 10;
            } else if (checkedId == R.id.timing30sec) {
                selectedSeconds = 30;
            } else if (checkedId == R.id.timing1min) {
                selectedSeconds = 60;
            } else if (checkedId == R.id.timing2min) {
                selectedSeconds = 120;
            } else if (checkedId == R.id.timing5min) {
                selectedSeconds = 300;
            }
        });

        // Test notification button
        testNotificationButton.setOnClickListener(v -> {
            if (notificationManager.canScheduleExactAlarms()) {
                scheduleTestNotification(selectedSeconds);
            } else {
                Toast.makeText(this,
                        "❌ Please enable 'Alarms & reminders' first",
                        Toast.LENGTH_LONG).show();
                notificationManager.openExactAlarmSettings();
            }
        });

        // Check permissions button
        checkPermissionsButton.setOnClickListener(v -> {
            checkPermissionStatus();
        });
    }

    private void checkPermissionStatus() {
        boolean canSchedule = notificationManager.canScheduleExactAlarms();

        if (canSchedule) {
            statusText.setText("✅ Permission Granted\nYou can test notifications");
            statusText.setTextColor(Color.parseColor("#4CAF50")); // Green
            permissionCard.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Light green
            testNotificationButton.setEnabled(true);
        } else {
            statusText.setText("❌ Permission Required\nEnable 'Alarms & reminders' to test");
            statusText.setTextColor(Color.parseColor("#F44336")); // Red
            permissionCard.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Light red
            testNotificationButton.setEnabled(false);
        }
    }

    private void scheduleTestNotification(int seconds) {
        // Schedule custom test notification
        notificationManager.scheduleCustomTestNotification(
                seconds,
                "🧪 Test Notification",
                "This is a test notification to check timing and sound"
        );

        String timeText;
        if (seconds < 60) {
            timeText = seconds + " seconds";
        } else {
            int minutes = seconds / 60;
            timeText = minutes + " minute" + (minutes > 1 ? "s" : "");
        }

        Toast.makeText(this,
                "🧪 Test notification scheduled in " + timeText,
                Toast.LENGTH_LONG).show();

        // Show countdown
        statusText.setText("⏰ Notification will fire in " + timeText + "\n\nWait for it...");
        statusText.setTextColor(Color.parseColor("#FF9800")); // Orange
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissionStatus();
    }
}