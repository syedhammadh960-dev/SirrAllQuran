package com.example.sirralquran.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sirralquran.R;
import com.example.sirralquran.controllers.LessonController;
import com.example.sirralquran.models.DayLesson;
import com.google.android.material.button.MaterialButton;

/**
 * ✅ FIXED: DayContentActivity with real-time next day unlock
 *
 * NEW FEATURES:
 * 1. Auto-refresh every 30 seconds
 * 2. "Next Day" button unlocks automatically when conditions met
 * 3. Shows toast when next day unlocks
 */
public class DayContentActivity extends AppCompatActivity {

    private static final String TAG = "DayContent";

    // ✅ NEW: Auto-refresh interval
    private static final long REFRESH_INTERVAL_MS = 30 * 1000; // 30 seconds

    private ImageView backButton;
    private ImageView infoButton;
    private LinearLayout completeLayout;
    private CheckBox completeCheckbox;
    private TextView dayAndSurahText;
    private TextView lessonTitleText;
    private TextView themeDescriptionText;
    private TextView scholarQuoteText;
    private TextView tafseerText;
    private Button playButton;
    private MaterialButton previousDayButton;
    private Button nextDayButton;
    private ProgressBar loadingProgress;
    private ScrollView contentLayout;

    private LessonController lessonController;
    private int currentDayNumber;
    private DayLesson currentLesson;

    // ✅ NEW: Handler for auto-refresh
    private Handler refreshHandler;
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_content);

        currentDayNumber = getIntent().getIntExtra("DAY_NUMBER", 1);

        initializeViews();
        lessonController = new LessonController(this);

        // ✅ NEW: Setup auto-refresh
        setupAutoRefresh();

        loadLessonData();
        setupClickListeners();
        setupBackPressHandler();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        infoButton = findViewById(R.id.infoButton);
        completeLayout = findViewById(R.id.completeLayout);
        completeCheckbox = findViewById(R.id.completeCheckbox);
        dayAndSurahText = findViewById(R.id.dayAndSurahText);
        lessonTitleText = findViewById(R.id.lessonTitleText);
        themeDescriptionText = findViewById(R.id.themeDescriptionText);
        scholarQuoteText = findViewById(R.id.scholarQuoteText);
        tafseerText = findViewById(R.id.tafseerText);
        playButton = findViewById(R.id.playButton);
        previousDayButton = findViewById(R.id.previousDayButton);
        nextDayButton = findViewById(R.id.nextDayButton);
        loadingProgress = findViewById(R.id.loadingProgress);
        contentLayout = findViewById(R.id.contentLayout);
    }

    /**
     * ✅ NEW: Setup auto-refresh for next day unlock
     */
    private void setupAutoRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());

        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Update next day button status
                updateNextDayButtonStatus();

                // Schedule next refresh
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
    }

    /**
     * ✅ NEW: Update next day button unlock status in real-time
     */
    private void updateNextDayButtonStatus() {
        if (currentDayNumber >= 30) {
            return; // Day 30, no next day
        }

        int nextDay = currentDayNumber + 1;

        // Get current unlock status
        boolean wasLocked = !nextDayButton.isEnabled();
        boolean isNowUnlocked = lessonController.canAccessDay(nextDay);

        // Check if status changed from locked to unlocked
        if (wasLocked && isNowUnlocked) {
            android.util.Log.d(TAG, "🔓 Day " + nextDay + " just unlocked!");

            // Update button
            nextDayButton.setEnabled(true);
            nextDayButton.setAlpha(1.0f);
            nextDayButton.setText("Next Day");

            // Show toast
            Toast.makeText(this,
                    "✅ Day " + nextDay + " is now unlocked!",
                    Toast.LENGTH_SHORT).show();
        } else if (!wasLocked && !isNowUnlocked) {
            // Locked again (shouldn't happen, but just in case)
            nextDayButton.setEnabled(false);
            nextDayButton.setAlpha(0.5f);
            nextDayButton.setText("🔒 Locked");
        }
    }

    private void loadLessonData() {
        showLoading(true);

        lessonController.getLessonByDay(currentDayNumber, new LessonController.OnLessonLoadListener() {
            @Override
            public void onSuccess(DayLesson lesson) {
                currentLesson = lesson;
                displayLesson(lesson);
                showLoading(false);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DayContentActivity.this, error, Toast.LENGTH_LONG).show();
                showLoading(false);

                if (error.contains("locked")) {
                    new android.os.Handler().postDelayed(() -> finish(), 2000);
                }
            }
        });

        updateNavigationButtons();
    }

    private void displayLesson(DayLesson lesson) {
        dayAndSurahText.setText("Day " + currentDayNumber + " • " + lesson.getSurahInfo());
        lessonTitleText.setText(lesson.getTitle());
        themeDescriptionText.setText(lesson.getTheme());
        scholarQuoteText.setText(lesson.getScholarQuote());
        tafseerText.setText(lesson.getTafseer());
        completeCheckbox.setChecked(lesson.isCompleted());
    }

    private void showLoading(boolean show) {
        if (loadingProgress != null) {
            loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (contentLayout != null) {
            contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DayContentActivity.this, "Lesson info", Toast.LENGTH_SHORT).show();
            }
        });

        completeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleCompletionToggle(isChecked);
            }
        });

        completeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeCheckbox.setChecked(!completeCheckbox.isChecked());
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideoPlayer();
            }
        });

        previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDayNumber > 1) {
                    currentDayNumber--;
                    loadLessonData();
                }
            }
        });

        nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDayNumber < 30) {
                    // Check if next day is unlocked
                    if (!lessonController.canAccessDay(currentDayNumber + 1)) {
                        String timeRemaining = lessonController.getTimeRemaining(currentDayNumber + 1);
                        Toast.makeText(DayContentActivity.this,
                                timeRemaining,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    currentDayNumber++;
                    loadLessonData();
                }
            }
        });
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void handleCompletionToggle(boolean isChecked) {
        if (isChecked) {
            lessonController.markLessonCompleted(currentDayNumber);
            Toast.makeText(this, "Day " + currentDayNumber + " marked as complete! 🎉",
                    Toast.LENGTH_SHORT).show();

            // ✅ IMMEDIATELY update next day button (for catch-up mode)
            updateNavigationButtons();

            // ✅ Check if next day unlocked immediately (catch-up mode)
            if (currentDayNumber < 30) {
                new Handler().postDelayed(() -> {
                    boolean nextDayUnlocked = lessonController.canAccessDay(currentDayNumber + 1);
                    if (nextDayUnlocked) {
                        Toast.makeText(this,
                                "✅ Day " + (currentDayNumber + 1) + " is now unlocked!",
                                Toast.LENGTH_SHORT).show();
                    }
                }, 500); // Small delay to ensure data is saved
            }
        }
    }

    private void openVideoPlayer() {
        if (currentLesson != null && currentLesson.getVideoUrl() != null
                && !currentLesson.getVideoUrl().isEmpty()) {
            Intent intent = new Intent(DayContentActivity.this, VideoPlayerActivity.class);
            intent.putExtra("DAY_NUMBER", currentDayNumber);
            intent.putExtra("LESSON_TITLE", currentLesson.getTitle());
            intent.putExtra("LESSON_THEME", currentLesson.getTheme());
            intent.putExtra("VIDEO_URL", currentLesson.getVideoUrl());
            intent.putExtra("SURAH_INFO", currentLesson.getSurahInfo());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Video not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNavigationButtons() {
        // Previous button
        if (currentDayNumber == 1) {
            previousDayButton.setEnabled(false);
            previousDayButton.setAlpha(0.5f);
        } else {
            previousDayButton.setEnabled(true);
            previousDayButton.setAlpha(1.0f);
        }

        // Next button
        if (currentDayNumber == 30) {
            nextDayButton.setEnabled(false);
            nextDayButton.setAlpha(0.5f);
        } else {
            // ✅ Check if next day is unlocked
            boolean nextDayUnlocked = lessonController.canAccessDay(currentDayNumber + 1);
            nextDayButton.setEnabled(nextDayUnlocked);
            nextDayButton.setAlpha(nextDayUnlocked ? 1.0f : 0.5f);

            if (!nextDayUnlocked) {
                nextDayButton.setText("🔒 Locked");
            } else {
                nextDayButton.setText("Next Day");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLessonData();

        // ✅ NEW: Start auto-refresh
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
            android.util.Log.d(TAG, "🔄 Auto-refresh started");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // ✅ NEW: Stop auto-refresh
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
            android.util.Log.d(TAG, "⏸️ Auto-refresh paused");
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