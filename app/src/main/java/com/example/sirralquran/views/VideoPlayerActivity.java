package com.example.sirralquran.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.example.sirralquran.R;

public class VideoPlayerActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView videoTitleText;
    private ImageView playIconLarge;
    private TextView videoDurationText;
    private TextView lessonTitleText;
    private TextView lessonDescriptionText;
    private TextView viewCountText;
    private TextView durationInfoText;
    private TextView themeText;
    private TextView surahText;
    private MaterialButton previousDayButton;
    private Button nextDayButton;

    private int currentDayNumber;
    private String lessonTitle;
    private String lessonTheme;
    private String videoUrl;
    private String surahInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Get data from intent
        currentDayNumber = getIntent().getIntExtra("DAY_NUMBER", 1);
        lessonTitle = getIntent().getStringExtra("LESSON_TITLE");
        lessonTheme = getIntent().getStringExtra("LESSON_THEME");
        videoUrl = getIntent().getStringExtra("VIDEO_URL");
        surahInfo = getIntent().getStringExtra("SURAH_INFO");

        initializeViews();
        loadVideoData();
        setupClickListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        videoTitleText = findViewById(R.id.videoTitleText);
        playIconLarge = findViewById(R.id.playIconLarge);
        videoDurationText = findViewById(R.id.videoDurationText);
        lessonTitleText = findViewById(R.id.lessonTitleText);
        lessonDescriptionText = findViewById(R.id.lessonDescriptionText);
        viewCountText = findViewById(R.id.viewCountText);
        durationInfoText = findViewById(R.id.durationInfoText);
        themeText = findViewById(R.id.themeText);
        surahText = findViewById(R.id.surahText);
        previousDayButton = findViewById(R.id.previousDayButton);
        nextDayButton = findViewById(R.id.nextDayButton);
    }

    private void loadVideoData() {
        // Set video title in header
        videoTitleText.setText("Day " + currentDayNumber + " - " + getLessonShortTitle());

        // Set lesson info
        lessonTitleText.setText(lessonTitle);
        lessonDescriptionText.setText("Exploring the evidences of creation and humanity's noble beginning");

        // Set video stats
        viewCountText.setText("1.2K views");
        durationInfoText.setText("15 min");
        videoDurationText.setText("15:30");

        // Set about section
        themeText.setText(lessonTheme);
        surahText.setText(surahInfo);

        // TODO: Initialize actual video player here
        // For now, we're showing a placeholder
        setupVideoPlayerPlaceholder();
    }

    private String getLessonShortTitle() {
        // Extract Surah name from full title
        if (lessonTitle != null && lessonTitle.contains("(")) {
            int startIndex = lessonTitle.indexOf("(");
            return lessonTitle.substring(0, startIndex).trim();
        }
        return lessonTitle;
    }

    private void setupVideoPlayerPlaceholder() {
        // Handle play button click on placeholder
        playIconLarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Start video playback
                // For now, just hide the play icon
                playIconLarge.setVisibility(View.GONE);

                // You can integrate YouTube player, ExoPlayer, or WebView here
                // Example:
                // playVideoWithExoPlayer(videoUrl);
                // or
                // playYouTubeVideo(videoId);
            }
        });
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Previous Day button
        previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDayNumber > 1) {
                    navigateToDayContent(currentDayNumber - 1);
                }
            }
        });

        // Next Day button
        nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDayNumber < 30) {
                    navigateToDayContent(currentDayNumber + 1);
                }
            }
        });

        // Update button states
        updateNavigationButtons();
    }

    private void navigateToDayContent(int dayNumber) {
        // Go back to DayContentActivity with new day number
        Intent intent = new Intent(VideoPlayerActivity.this, DayContentActivity.class);
        intent.putExtra("DAY_NUMBER", dayNumber);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void updateNavigationButtons() {
        // Disable Previous button on Day 1
        if (currentDayNumber == 1) {
            previousDayButton.setEnabled(false);
            previousDayButton.setAlpha(0.5f);
        } else {
            previousDayButton.setEnabled(true);
            previousDayButton.setAlpha(1.0f);
        }

        // Disable Next button on Day 30
        if (currentDayNumber == 30) {
            nextDayButton.setEnabled(false);
            nextDayButton.setAlpha(0.5f);
        } else {
            nextDayButton.setEnabled(true);
            nextDayButton.setAlpha(1.0f);
        }
    }

    // TODO: Implement actual video player integration
    /*
    private void playVideoWithExoPlayer(String videoUrl) {
        // ExoPlayer implementation
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).build();
        PlayerView playerView = findViewById(R.id.playerView);
        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }
    */

    /*
    private void playYouTubeVideo(String videoId) {
        // YouTube Player implementation
        // Add YouTubePlayerView to layout and initialize
    }
    */

    @Override
    protected void onPause() {
        super.onPause();
        // TODO: Pause video playback when activity is paused
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: Release video player resources
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}