package com.example.sirralquran.views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.example.sirralquran.R;
import com.example.sirralquran.adapters.PrayerAdapter;
import com.example.sirralquran.controllers.PrayerController;
import com.example.sirralquran.dialogs.NotificationSettingsDialog;
import com.example.sirralquran.models.Prayer;
import com.example.sirralquran.utils.LocationHelper;
import com.example.sirralquran.utils.PrayerNotificationManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * COMPLETE Salah Tracker with all fixes
 */
public class SalahTrackerActivity extends AppCompatActivity implements PrayerAdapter.OnPrayerClickListener {

    private static final String TAG = "SalahTracker";
    private static final int PERMISSIONS_REQUEST = 1001;

    private ImageView backButton;
    private TextView dateText;
    private TextView hijriDateText;
    private TextView locationText;
    private RecyclerView prayerRecyclerView;
    private CircularProgressIndicator progressIndicator;
    private TextView completedCountText;
    private TextView qazaCountText;
    private TextView tipText;
    private BottomNavigationView bottomNavigationView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private PrayerAdapter prayerAdapter;
    private PrayerController prayerController;
    private PrayerNotificationManager notificationManager;
    private List<Prayer> prayerList;

    private boolean permissionsChecked = false;
    private boolean initialLoadDone = false;
    private boolean isUpdatingPrayer = false;  // Prevent concurrent updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salah_tracker);

        initializeViews();
        prayerController = new PrayerController(this);
        notificationManager = new PrayerNotificationManager(this);

        loadPrayerData();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();
        setupSwipeRefresh();

        if (!permissionsChecked) {
            checkAndRequestPermissions();
            permissionsChecked = true;
        }

        checkExactAlarmPermission();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        dateText = findViewById(R.id.dateText);
        hijriDateText = findViewById(R.id.hijriDateText);
        locationText = findViewById(R.id.locationText);
        prayerRecyclerView = findViewById(R.id.prayerRecyclerView);
        progressIndicator = findViewById(R.id.progressIndicator);
        completedCountText = findViewById(R.id.completedCountText);
        qazaCountText = findViewById(R.id.qazaCountText);
        tipText = findViewById(R.id.tipText);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!notificationManager.canScheduleExactAlarms()) {
                Log.e(TAG, "❌ No exact alarm permission");
                showExactAlarmPermissionDialog();
            }
        }
    }

    private void showExactAlarmPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Allow Exact Alarms")
                .setMessage("Prayer notifications require permission to schedule exact alarms.\n\nPlease enable 'Alarms & reminders'.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    notificationManager.openExactAlarmSettings();
                })
                .setNegativeButton("Later", null)
                .show();
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!permissionsNeeded.isEmpty()) {
            requestPermissions(permissionsNeeded.toArray(new String[0]), PERMISSIONS_REQUEST);
        } else {
            if (!initialLoadDone) {
                checkLocationAndFetchSmart(false);
                initialLoadDone = true;
            }
        }
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_orange_dark,
                R.color.accent_gold
        );

        swipeRefreshLayout.setOnRefreshListener(this::refreshPrayerTimes);
    }

    private void loadPrayerData() {
        prayerList = prayerController.getTodaysPrayers();
        updateProgressUI();
        updateDateInfo();
    }

    private void refreshPrayerTimes() {
        LocationHelper locationHelper = prayerController.getLocationHelper();

        if (!locationHelper.hasLocationPermission()) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!locationHelper.isGPSEnabled()) {
            swipeRefreshLayout.setRefreshing(false);
            showGPSEnableDialog();
            return;
        }

        fetchPrayerTimes(true);
    }

    private void checkLocationAndFetchSmart(boolean isManualRefresh) {
        LocationHelper locationHelper = prayerController.getLocationHelper();

        if (!locationHelper.hasLocationPermission() || !locationHelper.isGPSEnabled()) {
            return;
        }

        fetchPrayerTimes(isManualRefresh);
    }

    private void showGPSEnableDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Enable GPS")
                .setMessage("GPS required for accurate prayer times.")
                .setPositiveButton("Enable", (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchPrayerTimes(boolean forceRefresh) {
        prayerController.fetchPrayerTimesFromAPI(new PrayerController.OnPrayerTimesLoadedListener() {
            @Override
            public void onSuccess(List<Prayer> prayers) {
                runOnUiThread(() -> {
                    prayerList.clear();
                    prayerList.addAll(prayers);

                    prayerAdapter.notifyDataSetChanged();
                    updateProgressUI();
                    updateDateInfo();

                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    scheduleNotifications();

                    Toast.makeText(SalahTrackerActivity.this,
                            "Prayer times updated ✓",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(SalahTrackerActivity.this,
                            "Could not update",
                            Toast.LENGTH_SHORT).show();
                });
            }
        }, forceRefresh);
    }

    private void scheduleNotifications() {
        if (!notificationManager.canScheduleExactAlarms()) {
            Toast.makeText(this,
                    "⚠️ Enable 'Alarms & reminders' for notifications",
                    Toast.LENGTH_LONG).show();
            return;
        }

        notificationManager.cancelAllNotifications();
        notificationManager.scheduleAllPrayerNotifications(prayerList);
    }

    private void updateDateInfo() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.ENGLISH);
        dateText.setText(dateFormat.format(new Date()));
        hijriDateText.setText("15 Ramadan 1446");
        locationText.setText(prayerController.getLocationHelper().getCachedCity());
    }

    private void setupRecyclerView() {
        prayerAdapter = new PrayerAdapter(prayerList, this);
        prayerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        prayerRecyclerView.setAdapter(prayerAdapter);
        prayerRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> showOptionsMenu());
    }

    private void showOptionsMenu() {
        PopupMenu popup = new PopupMenu(this, backButton);
        popup.getMenu().add(Menu.NONE, 1, Menu.NONE, "Refresh Prayer Times");
        popup.getMenu().add(Menu.NONE, 2, Menu.NONE, "Test Notification (10s)");
        popup.getMenu().add(Menu.NONE, 3, Menu.NONE, "Check Alarm Permission");
        popup.getMenu().add(Menu.NONE, 4, Menu.NONE, "Back");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    swipeRefreshLayout.setRefreshing(true);
                    refreshPrayerTimes();
                    return true;
                case 2:
                    testNotification();
                    return true;
                case 3:
                    checkExactAlarmPermission();
                    return true;
                case 4:
                    finish();
                    return true;
            }
            return false;
        });

        popup.show();
    }

    private void testNotification() {
        if (!notificationManager.canScheduleExactAlarms()) {
            Toast.makeText(this,
                    "❌ Enable 'Alarms & reminders' first",
                    Toast.LENGTH_LONG).show();
            checkExactAlarmPermission();
            return;
        }

        notificationManager.scheduleTestNotification();
        Toast.makeText(this, "🧪 Test notification in 10 seconds", Toast.LENGTH_LONG).show();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_salah);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_salah) {
                return true;
            } else if (itemId == R.id.nav_thirty_day) {
                startActivity(new Intent(this, ThirtyDayJourneyActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void updateProgressUI() {
        int completed = prayerController.getCompletedPrayersCount();
        int qaza = prayerController.getQazaPrayersCount();
        int total = prayerList.size();
        int progress = total > 0 ? (completed * 100) / total : 0;

        progressIndicator.setProgress(progress);
        completedCountText.setText(String.valueOf(completed));
        qazaCountText.setText(String.valueOf(qaza));
    }

    @Override
    public void onPrayerChecked(Prayer prayer, boolean isChecked) {
        if (isUpdatingPrayer) return;  // Prevent concurrent updates

        isUpdatingPrayer = true;

        try {
            prayer.setCompleted(isChecked);
            if (isChecked && prayer.isQaza()) {
                prayer.setQaza(false);
            }

            prayerController.updatePrayerStatus(prayer);
            updateProgressUI();

            // Reschedule notifications (will cancel if completed)
            scheduleNotifications();

            prayerAdapter.notifyDataSetChanged();

            Log.d(TAG, "✅ Prayer updated: " + prayer.getName() +
                    " | Completed: " + prayer.isCompleted() +
                    " | Offered: " + prayer.getOfferedTime());

        } finally {
            isUpdatingPrayer = false;
        }
    }

    @Override
    public void onPrayerLongClick(Prayer prayer) {
        prayer.setQaza(!prayer.isQaza());
        if (prayer.isQaza()) {
            prayer.setCompleted(false);
        }

        prayerController.updatePrayerStatus(prayer);
        updateProgressUI();
        prayerAdapter.notifyDataSetChanged();

        Toast.makeText(this,
                prayer.isQaza() ? "Marked as Qaza" : "Qaza removed",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationIconClick(Prayer prayer, int position) {
        if (prayer.isCompleted()) {
            Toast.makeText(this, "Prayer already completed", Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationSettingsDialog dialog = new NotificationSettingsDialog(
                this,
                prayer,
                updatedPrayer -> {
                    prayerController.updatePrayerStatus(updatedPrayer);
                    prayerAdapter.notifyItemChanged(position);

                    scheduleNotifications();

                    Toast.makeText(this,
                            "Notification settings updated ✓",
                            Toast.LENGTH_SHORT).show();
                }
        );
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST) {
            boolean locationGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        locationGranted = true;
                    }
                }
            }

            if (locationGranted && !initialLoadDone) {
                Toast.makeText(this, "Checking prayer times...", Toast.LENGTH_SHORT).show();
                checkLocationAndFetchSmart(false);
                initialLoadDone = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPrayerData();
        prayerAdapter.notifyDataSetChanged();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (notificationManager.canScheduleExactAlarms()) {
                scheduleNotifications();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        prayerController.getLocationHelper().stopLocationUpdates();
    }
}