// ========================================
// MINIMAL FIX: Permission Bug Only
// CHANGE: Request all permissions TOGETHER in ONE call
// NO OTHER CHANGES
// ========================================

package com.example.sirralquran.views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.sirralquran.dialogs.FiqhSelectionDialog;
import com.example.sirralquran.dialogs.NotificationSettingsDialog;
import com.example.sirralquran.models.Prayer;
import com.example.sirralquran.utils.HijriDateHelper;
import com.example.sirralquran.utils.LocationHelper;
import com.example.sirralquran.utils.PrayerNotificationManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * UPDATED: Removed back button, added Fiqh selector button
 */
public class SalahTrackerActivity extends AppCompatActivity implements PrayerAdapter.OnPrayerClickListener {

    private static final String TAG = "SalahTracker";
    private static final int PERMISSIONS_REQUEST = 1001;

    // UI Components - REMOVED backButton, ADDED fiqhSelectorButton
    private LinearLayout fiqhSelectorButton;
    private TextView fiqhMethodText;
    private TextView dateText;
    private TextView hijriDateText;
    private TextView locationText;
    private RecyclerView prayerRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CircularProgressIndicator progressIndicator;
    private TextView completedCountText;
    private TextView qazaCountText;
    private BottomNavigationView bottomNavigationView;

    // Controllers and Adapters
    private PrayerController prayerController;
    private PrayerAdapter prayerAdapter;
    private PrayerNotificationManager notificationManager;
    private HijriDateHelper hijriDateHelper;
    private List<Prayer> prayerList;

    // Handler for safe UI updates
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private boolean initialLoadDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salah_tracker);

        initializeViews();
        initializeControllers();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFiqhSelector(); // NEW
        setupBottomNavigation();
        loadInitialData();
    }

    private void initializeViews() {
        // REMOVED: backButton
        // NEW: Fiqh selector button
        fiqhSelectorButton = findViewById(R.id.fiqhSelectorButton);
        fiqhMethodText = findViewById(R.id.fiqhMethodText);

        dateText = findViewById(R.id.dateText);
        hijriDateText = findViewById(R.id.hijriDateText);
        locationText = findViewById(R.id.locationText);
        prayerRecyclerView = findViewById(R.id.prayerRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressIndicator = findViewById(R.id.progressIndicator);
        completedCountText = findViewById(R.id.completedCountText);
        qazaCountText = findViewById(R.id.qazaCountText);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Update current Fiqh method display
        updateFiqhMethodDisplay();
    }

    private void initializeControllers() {
        prayerController = new PrayerController(this);
        notificationManager = new PrayerNotificationManager(this);
        hijriDateHelper = new HijriDateHelper(this);
        prayerList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        prayerAdapter = new PrayerAdapter(prayerList, this);
        prayerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        prayerRecyclerView.setAdapter(prayerAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshPrayerTimes);
        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.primary_green, null)
        );
    }

    /**
     * NEW: Setup Fiqh selector button
     */
    private void setupFiqhSelector() {
        fiqhSelectorButton.setOnClickListener(v -> showFiqhSelectionDialog());
    }

    /**
     * NEW: Update Fiqh method text display
     */
    private void updateFiqhMethodDisplay() {
        String methodName = FiqhSelectionDialog.getSavedMethodName(this);

        // Show short name
        String shortName = getShortMethodName(methodName);
        fiqhMethodText.setText(shortName);
    }

    /**
     * NEW: Get short name for display
     */
    private String getShortMethodName(String fullName) {
        if (fullName.contains("Karachi")) return "Hanafi";
        if (fullName.contains("Shia")) return "Shia";
        if (fullName.contains("ISNA")) return "ISNA";
        if (fullName.contains("World League")) return "MWL";
        if (fullName.contains("Umm")) return "Makkah";
        if (fullName.contains("Egyptian")) return "Egypt";
        return "Hanafi"; // Default
    }

    /**
     * NEW: Show Fiqh selection dialog
     */
    private void showFiqhSelectionDialog() {
        FiqhSelectionDialog dialog = new FiqhSelectionDialog(this, (method, methodName) -> {
            Log.d(TAG, "📊 Fiqh changed to: " + methodName + " (method=" + method + ")");

            // Update display
            updateFiqhMethodDisplay();

            Toast.makeText(this,
                    "📖 Using: " + methodName,
                    Toast.LENGTH_SHORT).show();

            // Force refresh prayer times with new method
            swipeRefreshLayout.setRefreshing(true);
            refreshPrayerTimes();
        });

        dialog.show();
    }

    // ========================================
    // ✅ PERMISSION FIX: Request all permissions TOGETHER in ONE call
    // ========================================
    private void loadInitialData() {
        loadHijriDate();
        updateDateInfo();

        LocationHelper locationHelper = prayerController.getLocationHelper();

        // ✅ FIX: Check if ANY permission is missing
        boolean needsPermissions = !locationHelper.hasLocationPermission();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                needsPermissions = true;
            }
        }

        if (needsPermissions) {
            // ✅ FIX: Request ALL permissions in ONE call
            requestAllPermissions();
        } else {
            loadPrayerData();

            if (!initialLoadDone) {
                if (locationHelper.isGPSEnabled()) {
                    checkLocationAndFetchSmart(false);
                    initialLoadDone = true;
                } else {
                    showGPSEnableDialog();
                }
            }
        }

        updateDateInfo();
    }

    /**
     * ✅ FIX: Request ALL permissions in ONE call
     */
    private void requestAllPermissions() {
        List<String> permissions = new ArrayList<>();

        // Always request location
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        // Add notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        Log.d(TAG, "📢 Requesting " + permissions.size() + " permissions in ONE call");
        requestPermissions(permissions.toArray(new String[0]), PERMISSIONS_REQUEST);
    }

    private void loadHijriDate() {
        hijriDateText.setText(hijriDateHelper.getCachedHijriDate());

        hijriDateHelper.fetchHijriDate(new HijriDateHelper.HijriDateCallback() {
            @Override
            public void onSuccess(String hijriDate) {
                runOnUiThread(() -> hijriDateText.setText(hijriDate));
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Hijri date error: " + error);
            }
        });
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
                .setMessage("GPS is required for accurate prayer times. Enable it now?")
                .setPositiveButton("Settings", (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchPrayerTimes(boolean forceRefresh) {
        prayerController.fetchPrayerTimesFromAPI(
                new PrayerController.OnPrayerTimesLoadedListener() {
                    @Override
                    public void onSuccess(List<Prayer> prayers) {
                        runOnUiThread(() -> {
                            prayerList.clear();
                            prayerList.addAll(prayers);

                            uiHandler.post(() -> {
                                prayerAdapter.notifyDataSetChanged();
                            });

                            swipeRefreshLayout.setRefreshing(false);
                            scheduleNotifications();

                            String message = forceRefresh ? "Prayer times updated ✓" : "Prayer times loaded ✓";
                            Toast.makeText(SalahTrackerActivity.this, message, Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(SalahTrackerActivity.this,
                                    "Failed: " + error, Toast.LENGTH_SHORT).show();

                            loadPrayerData();
                        });
                    }
                },
                forceRefresh
        );
    }

    private void loadPrayerData() {
        List<Prayer> prayers = prayerController.getTodaysPrayers();
        prayerList.clear();
        prayerList.addAll(prayers);

        uiHandler.post(() -> {
            prayerAdapter.notifyDataSetChanged();
        });

        updateProgress();
        updateLocationDisplay();
    }

    private void updateDateInfo() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.ENGLISH);
        dateText.setText(sdf.format(new Date()));
    }

    private void updateLocationDisplay() {
        LocationHelper locationHelper = prayerController.getLocationHelper();
        String city = locationHelper.getCachedCity();
        locationText.setText(city);
    }

    private void updateProgress() {
        int completed = prayerController.getCompletedPrayersCount();
        int qaza = prayerController.getQazaPrayersCount();
        int total = 5;

        completedCountText.setText(String.valueOf(completed));
        qazaCountText.setText(String.valueOf(qaza));

        int progress = (int) ((completed / (float) total) * 100);
        progressIndicator.setProgress(progress);
    }

    private void scheduleNotifications() {
        // ✅ CHECK: Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "⚠️ Cannot schedule: POST_NOTIFICATIONS not granted");
                return;
            }
        }

        // ✅ CHECK: Exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!notificationManager.canScheduleExactAlarms()) {
                Log.w(TAG, "⚠️ Cannot schedule: No exact alarm permission");
                return;
            }
        }

        notificationManager.scheduleAllPrayerNotifications(prayerList);
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!notificationManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("Enable 'Alarms & reminders' for prayer notifications")
                        .setPositiveButton("Settings", (dialog, which) -> {
                            notificationManager.openExactAlarmSettings();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "✓ Permission granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "✓ No permission needed (Android < 12)", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPrayerChecked(Prayer prayer, boolean isChecked) {
        Log.d(TAG, "Prayer checked: " + prayer.getName() + " = " + isChecked);
        prayer.setCompleted(isChecked);

        // ✅ Record offered time
        if (isChecked) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
            prayer.setOfferedTime(sdf.format(new Date()));
        } else {
            prayer.setOfferedTime(null);
        }

        prayerController.updatePrayerStatus(prayer);

        uiHandler.post(() -> {
            prayerAdapter.notifyDataSetChanged();
        });

        updateProgress();
        scheduleNotifications();

        String message = isChecked ? prayer.getName() + " marked as offered ✓" :
                prayer.getName() + " unmarked";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPrayerLongClick(Prayer prayer) {
        if (!prayer.hasPrayerTimeArrived()) {
            Toast.makeText(this, "Prayer time hasn't arrived yet", Toast.LENGTH_SHORT).show();
            return;
        }

        prayer.setQaza(!prayer.isQaza());
        prayerController.updatePrayerStatus(prayer);

        uiHandler.post(() -> {
            prayerAdapter.notifyDataSetChanged();
        });

        updateProgress();

        String message = prayer.isQaza() ? prayer.getName() + " marked as QAZA" :
                prayer.getName() + " unmarked from QAZA";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationIconClick(Prayer prayer, int position) {
        NotificationSettingsDialog dialog = new NotificationSettingsDialog(
                this,
                prayer,
                updatedPrayer -> {
                    prayerController.updatePrayerStatus(updatedPrayer);

                    uiHandler.post(() -> {
                        prayerAdapter.notifyItemChanged(position);
                    });

                    scheduleNotifications();

                    Toast.makeText(this,
                            "Notification settings updated ✓",
                            Toast.LENGTH_SHORT).show();
                }
        );
        dialog.show();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_salah);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_salah) {
                return true;
            } else if (itemId == R.id.nav_thirty_day) {
                Intent intent = new Intent(this, ThirtyDayJourneyActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    /**
     * ✅ FIX: Handle all permissions in ONE result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST) {
            boolean locationGranted = false;
            boolean notificationGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                // Check location
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        locationGranted = true;
                        Log.d(TAG, "✅ Location permission granted");
                    }
                }

                // Check notification (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (permissions[i].equals(Manifest.permission.POST_NOTIFICATIONS)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            notificationGranted = true;
                            Log.d(TAG, "✅ Notification permission granted");
                        } else {
                            Log.w(TAG, "❌ Notification permission denied");
                            Toast.makeText(this,
                                    "⚠️ Notifications disabled",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            if (locationGranted && !initialLoadDone) {
                loadPrayerData();
                Toast.makeText(this, "Checking prayer times...", Toast.LENGTH_SHORT).show();
                checkLocationAndFetchSmart(false);
                initialLoadDone = true;
            }

            // Check exact alarm permission after all permissions granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!notificationManager.canScheduleExactAlarms()) {
                    checkExactAlarmPermission();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPrayerData();

        uiHandler.post(() -> {
            prayerAdapter.notifyDataSetChanged();
        });

        // Update Fiqh display in case it changed
        updateFiqhMethodDisplay();

        // Reschedule notifications if permissions available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (notificationManager.canScheduleExactAlarms()) {
                scheduleNotifications();
            }
        } else {
            scheduleNotifications();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        prayerController.getLocationHelper().stopLocationUpdates();
        uiHandler.removeCallbacksAndMessages(null);
    }
}