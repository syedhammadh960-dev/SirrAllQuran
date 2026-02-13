package com.example.sirralquran.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sirralquran.controllers.RamadanManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.sirralquran.R;
import com.example.sirralquran.adapters.DayLessonAdapter;
import com.example.sirralquran.models.DayLesson;
import com.example.sirralquran.models.RamadanDayContent;
import com.example.sirralquran.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ FIXED: Real-time unlock updates with auto-refresh
 *
 * NEW FEATURES:
 * 1. Auto-refreshes every 30 seconds to check unlock status
 * 2. Updates UI when days unlock (no need to restart app)
 * 3. Shows countdown timer updates in real-time
 */
public class ThirtyDayJourneyActivity extends AppCompatActivity implements DayLessonAdapter.OnLessonClickListener {

    private static final String TAG = "ThirtyDayJourney";

    // ✅ NEW: Auto-refresh interval (30 seconds)
    private static final long REFRESH_INTERVAL_MS = 30 * 1000; // 30 seconds

    private ImageView backButton;
    private TextView progressInfoText;
    private ProgressBar journeyProgressBar;
    private TextView progressPercentText;
    private RecyclerView dayLessonRecyclerView;
    private BottomNavigationView bottomNavigationView;
    private ProgressBar loadingProgress;

    private DayLessonAdapter lessonAdapter;
    private RamadanManager ramadanManager;
    private FirebaseHelper firebaseHelper;
    private List<DayLesson> lessonList;

    // Track loading state
    private int totalDaysToLoad = 0;
    private int daysLoaded = 0;
    private boolean isLoading = false;

    // ✅ NEW: Handler for auto-refresh
    private Handler refreshHandler;
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thirty_day_journey);

        initializeViews();
        ramadanManager = new RamadanManager(this);
        firebaseHelper = FirebaseHelper.getInstance();

        // Initialize with empty list
        lessonList = new ArrayList<>();

        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();

        // ✅ NEW: Setup auto-refresh
        setupAutoRefresh();

        // Load data ONLY in onCreate (not in onResume)
        loadAllLessonsFromFirebase();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        progressInfoText = findViewById(R.id.progressInfoText);
        journeyProgressBar = findViewById(R.id.journeyProgressBar);
        progressPercentText = findViewById(R.id.progressPercentText);
        dayLessonRecyclerView = findViewById(R.id.dayLessonRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        loadingProgress = findViewById(R.id.loadingProgress);
    }

    /**
     * ✅ NEW: Setup auto-refresh timer
     * Checks unlock status every 30 seconds
     */
    private void setupAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());

        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Update unlock status for all lessons
                updateUnlockStatus();

                // Schedule next refresh
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
    }

    /**
     * ✅ NEW: Update unlock status in real-time
     * This checks if any locked days should now be unlocked
     */
    private void updateUnlockStatus() {
        if (lessonList == null || lessonList.isEmpty()) {
            return;
        }

        boolean hasChanges = false;

        for (int i = 0; i < lessonList.size(); i++) {
            DayLesson lesson = lessonList.get(i);
            int dayNumber = lesson.getDayNumber();

            // Get current unlock status
            boolean wasLocked = lesson.isLocked();
            boolean isNowUnlocked = ramadanManager.isDayUnlocked(dayNumber);

            // Check if status changed
            if (wasLocked && isNowUnlocked) {
                Log.d(TAG, "🔓 Day " + dayNumber + " just unlocked!");
                lesson.setLocked(false);
                hasChanges = true;

                // Show toast notification
                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "✅ Day " + dayNumber + " is now unlocked!",
                            Toast.LENGTH_SHORT).show();
                });
            }

            // Update completion status
            boolean isCompleted = ramadanManager.isDayCompleted(dayNumber);
            if (lesson.isCompleted() != isCompleted) {
                lesson.setCompleted(isCompleted);
                hasChanges = true;
            }
        }

        // Update UI if anything changed
        if (hasChanges) {
            runOnUiThread(() -> {
                if (lessonAdapter != null) {
                    lessonAdapter.notifyDataSetChanged();
                }
                updateProgressUI();
            });
        }
    }

    /**
     * Load ALL 30 days from Firebase
     */
    private void loadAllLessonsFromFirebase() {
        if (isLoading) {
            Log.d(TAG, "⚠️ Already loading, skipping duplicate request");
            return;
        }

        isLoading = true;
        showLoading(true);

        int maxAccessibleDay = ramadanManager.getMaxAccessibleDay();
        Log.d(TAG, "🚀 Loading 30-day content from Firebase. Max accessible: " + maxAccessibleDay);

        lessonList.clear();

        totalDaysToLoad = maxAccessibleDay;
        daysLoaded = 0;

        // Load content for each accessible day from Firebase
        for (int i = 1; i <= maxAccessibleDay; i++) {
            final int dayNumber = i;

            firebaseHelper.getDayContent(dayNumber, new FirebaseHelper.OnDataLoadListener<RamadanDayContent>() {
                @Override
                public void onSuccess(RamadanDayContent content) {
                    daysLoaded++;
                    Log.d(TAG, "✅ Loaded day " + dayNumber + " (" + daysLoaded + "/" + totalDaysToLoad + ")");

                    DayLesson lesson = convertToDayLesson(content, dayNumber);

                    synchronized (lessonList) {
                        // Remove duplicates
                        for (int j = lessonList.size() - 1; j >= 0; j--) {
                            if (lessonList.get(j).getDayNumber() == dayNumber) {
                                lessonList.remove(j);
                            }
                        }

                        // Insert at correct position
                        int insertIndex = 0;
                        for (int j = 0; j < lessonList.size(); j++) {
                            if (lessonList.get(j).getDayNumber() < dayNumber) {
                                insertIndex = j + 1;
                            }
                        }
                        lessonList.add(insertIndex, lesson);
                    }

                    if (daysLoaded >= totalDaysToLoad) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "✅ All " + totalDaysToLoad + " days loaded!");
                            finishLoading();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    daysLoaded++;
                    Log.e(TAG, "❌ Failed to load day " + dayNumber + ": " + error);

                    DayLesson fallback = getFallbackLesson(dayNumber);
                    synchronized (lessonList) {
                        lessonList.add(fallback);
                    }

                    if (daysLoaded >= totalDaysToLoad) {
                        runOnUiThread(() -> finishLoading());
                    }
                }
            });
        }

        // Add locked days
        for (int i = maxAccessibleDay + 1; i <= 30; i++) {
            DayLesson lockedLesson = getLockedLesson(i);
            lessonList.add(lockedLesson);
        }

        if (maxAccessibleDay == 0) {
            finishLoading();
            Toast.makeText(this, "No days unlocked yet", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Finish loading process
     */
    private void finishLoading() {
        updateRecyclerView();
        updateProgressUI();
        showLoading(false);
        isLoading = false;
    }

    /**
     * Convert Firebase RamadanDayContent to DayLesson model
     */
    private DayLesson convertToDayLesson(RamadanDayContent content, int dayNumber) {
        boolean isCompleted = ramadanManager.isDayCompleted(dayNumber);
        boolean isLocked = !ramadanManager.isDayUnlocked(dayNumber);

        return new DayLesson(
                dayNumber,
                content.getCoreTheme() != null ? content.getCoreTheme() : "Day " + dayNumber,
                content.getExplanation() != null ?
                        (content.getExplanation().length() > 100 ?
                                content.getExplanation().substring(0, 100) + "..." :
                                content.getExplanation()) :
                        "Loading...",
                content.getCoreTheme() != null ? content.getCoreTheme() : "Theme",
                content.getJuz(),
                content.getSurahRange() != null ? content.getSurahRange() : "Juz " + content.getJuz(),
                content.getExplanation() != null ? content.getExplanation() : "",
                content.getReflectionQuestion() != null ? "\"" + content.getReflectionQuestion() + "\" - Dr. Israr Ahmed" : "",
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
     * Get locked lesson for inaccessible days
     */
    private DayLesson getLockedLesson(int dayNumber) {
        return new DayLesson(
                dayNumber,
                "Day " + dayNumber,
                "Locked until Day " + (dayNumber - 1) + " is complete",
                "🔒 Locked",
                dayNumber,
                "Juz " + dayNumber,
                "",
                "",
                "",
                false,
                true
        );
    }

    private void updateProgressUI() {
        int completedCount = ramadanManager.getCompletedDaysCount();
        int totalAccessible = ramadanManager.getMaxAccessibleDay();
        int progress = ramadanManager.getProgressPercentage();

        progressInfoText.setText(completedCount + " of " + totalAccessible + " days completed");
        journeyProgressBar.setProgress(progress);
        progressPercentText.setText(progress + "%");
    }

    private void setupRecyclerView() {
        lessonAdapter = new DayLessonAdapter(lessonList, this);
        dayLessonRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dayLessonRecyclerView.setAdapter(lessonAdapter);
    }

    private void updateRecyclerView() {
        if (lessonAdapter != null) {
            lessonAdapter.updateData(lessonList);
        }
    }

    private void showLoading(boolean show) {
        if (loadingProgress != null) {
            loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (dayLessonRecyclerView != null) {
            dayLessonRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_thirty_day);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ThirtyDayJourneyActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_salah) {
                startActivity(new Intent(ThirtyDayJourneyActivity.this, SalahTrackerActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_thirty_day) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(ThirtyDayJourneyActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    @Override
    public void onLessonClick(DayLesson lesson, int position) {
        int dayNumber = lesson.getDayNumber();

        // Check if day is unlocked
        if (lesson.isLocked()) {
            String timeRemaining = ramadanManager.getFormattedTimeRemaining(dayNumber);
            Toast.makeText(this, timeRemaining, Toast.LENGTH_LONG).show();
            return;
        }

        // Open Day Content Activity
        Intent intent = new Intent(ThirtyDayJourneyActivity.this, DayContentActivity.class);
        intent.putExtra("DAY_NUMBER", dayNumber);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update unlock status without reloading
        if (!isLoading) {
            updateUnlockStatus();
            updateProgressUI();
        }

        // ✅ NEW: Start auto-refresh timer
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
            Log.d(TAG, "🔄 Auto-refresh started (every 30 seconds)");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // ✅ NEW: Stop auto-refresh timer when activity not visible
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
            Log.d(TAG, "⏸️ Auto-refresh paused");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // ✅ NEW: Clean up handler
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
            refreshHandler = null;
            refreshRunnable = null;
        }
    }
}