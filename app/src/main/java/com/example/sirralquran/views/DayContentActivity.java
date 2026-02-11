package com.example.sirralquran.views;

import android.content.Intent;
import android.os.Bundle;
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
 * FIXED DayContentActivity - Removed LinearLayout casting error
 */
public class DayContentActivity extends AppCompatActivity {

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
    private ScrollView contentLayout; // FIXED: Changed from LinearLayout to ScrollView

    private LessonController lessonController;
    private int currentDayNumber;
    private DayLesson currentLesson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_content);

        currentDayNumber = getIntent().getIntExtra("DAY_NUMBER", 1);

        initializeViews();
        lessonController = new LessonController(this);
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

        // FIXED: Changed to ScrollView (not LinearLayout)
        loadingProgress = findViewById(R.id.loadingProgress);
        contentLayout = findViewById(R.id.contentLayout);
    }

    /**
     * Load lesson data from Firebase
     */
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

                // If day is locked, show time remaining and go back
                if (error.contains("locked")) {
                    new android.os.Handler().postDelayed(() -> finish(), 2000);
                }
            }
        });

        updateNavigationButtons();
    }

    /**
     * Display lesson content in UI
     */
    private void displayLesson(DayLesson lesson) {
        // Set header info
        dayAndSurahText.setText("Day " + currentDayNumber + " • " + lesson.getSurahInfo());
        lessonTitleText.setText(lesson.getTitle());

        // Set theme
        themeDescriptionText.setText(lesson.getTheme());

        // Set scholar quote
        scholarQuoteText.setText(lesson.getScholarQuote());

        // Set tafseer/explanation
        tafseerText.setText(lesson.getTafseer());

        // Set completion checkbox state
        completeCheckbox.setChecked(lesson.isCompleted());
    }

    /**
     * Show/hide loading indicator
     */
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
            updateNavigationButtons();
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
            // Check if next day is unlocked
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
    }
}