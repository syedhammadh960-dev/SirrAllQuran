package com.example.sirralquran.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.sirralquran.R;

public class TutorialOverlayHelper {

    private static final String PREFS_NAME = "TutorialPrefs";
    private static final String KEY_HOME_TUTORIAL_SHOWN = "home_tutorial_shown";
    private static final String KEY_SALAH_TUTORIAL_SHOWN = "salah_tutorial_shown";
    private static final String KEY_DAY_CONTENT_TUTORIAL_SHOWN = "day_content_tutorial_shown";

    private Context context;
    private SharedPreferences prefs;
    private ViewGroup rootView;
    private FrameLayout overlayLayout;
    private int currentStep = 0;
    private TutorialStep[] tutorialSteps;
    private int screenHeight;
    private int screenWidth;

    public TutorialOverlayHelper(Activity activity) {
        this.context = activity;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.rootView = activity.findViewById(android.R.id.content);

        // Get screen dimensions
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
    }

    public static class TutorialStep {
        public String title;
        public String message;
        public int avatarImage;
        public View targetView;
        public int pointerPosition; // 0=left, 1=right, 2=top, 3=bottom

        public TutorialStep(String title, String message, int avatarImage, View targetView, int pointerPosition) {
            this.title = title;
            this.message = message;
            this.avatarImage = avatarImage;
            this.targetView = targetView;
            this.pointerPosition = pointerPosition;
        }
    }

    public boolean shouldShowHomeTutorial() {
        return !prefs.getBoolean(KEY_HOME_TUTORIAL_SHOWN, false);
    }

    public boolean shouldShowSalahTutorial() {
        return !prefs.getBoolean(KEY_SALAH_TUTORIAL_SHOWN, false);
    }

    public boolean shouldShowDayContentTutorial() {
        return !prefs.getBoolean(KEY_DAY_CONTENT_TUTORIAL_SHOWN, false);
    }

    public void markHomeTutorialShown() {
        prefs.edit().putBoolean(KEY_HOME_TUTORIAL_SHOWN, true).apply();
    }

    public void markSalahTutorialShown() {
        prefs.edit().putBoolean(KEY_SALAH_TUTORIAL_SHOWN, true).apply();
    }

    public void markDayContentTutorialShown() {
        prefs.edit().putBoolean(KEY_DAY_CONTENT_TUTORIAL_SHOWN, true).apply();
    }

    public void showTutorial(TutorialStep[] steps, OnTutorialCompleteListener listener) {
        this.tutorialSteps = steps;
        this.currentStep = 0;
        showStep(listener);
    }

    private void showStep(OnTutorialCompleteListener listener) {
        if (currentStep >= tutorialSteps.length) {
            if (listener != null) {
                listener.onTutorialComplete();
            }
            return;
        }

        TutorialStep step = tutorialSteps[currentStep];

        // Create overlay
        overlayLayout = new FrameLayout(context);
        overlayLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlayLayout.setBackgroundColor(Color.parseColor("#DD000000"));
        overlayLayout.setClickable(true);

        // Tap anywhere to advance
        overlayLayout.setOnClickListener(v -> {
            removeTutorialOverlay();
            if (currentStep < tutorialSteps.length - 1) {
                // Not last step - go to next
                currentStep++;
                showStep(listener);
            } else {
                // Last step - complete tutorial
                if (listener != null) {
                    listener.onTutorialComplete();
                }
            }
        });

        // Highlight target view
        if (step.targetView != null) {
            highlightTargetView(step.targetView, overlayLayout);
            createPointerBubble(step, overlayLayout, listener);
        } else {
            createCenterTutorial(step, overlayLayout, listener);
        }

        rootView.addView(overlayLayout);
    }

    private void highlightTargetView(View targetView, FrameLayout overlay) {
        int[] location = new int[2];
        targetView.getLocationInWindow(location);

        // Gold border
        View highlightBorder = new View(context);
        highlightBorder.setBackgroundResource(R.drawable.highlight_border);

        FrameLayout.LayoutParams borderParams = new FrameLayout.LayoutParams(
                targetView.getWidth() + 16,
                targetView.getHeight() + 16
        );
        borderParams.leftMargin = location[0] - 8;
        borderParams.topMargin = location[1] - 8;
        highlightBorder.setLayoutParams(borderParams);
        overlay.addView(highlightBorder, 0);

        // Slight highlight on target
        View targetHighlight = new View(context);
        targetHighlight.setBackgroundColor(Color.parseColor("#10FFFFFF"));

        FrameLayout.LayoutParams highlightParams = new FrameLayout.LayoutParams(
                targetView.getWidth(),
                targetView.getHeight()
        );
        highlightParams.leftMargin = location[0];
        highlightParams.topMargin = location[1];
        targetHighlight.setLayoutParams(highlightParams);
        overlay.addView(targetHighlight, 1);
    }

    private void createPointerBubble(TutorialStep step, FrameLayout overlay, OnTutorialCompleteListener listener) {
        int[] location = new int[2];
        step.targetView.getLocationInWindow(location);

        // Bubble container
        LinearLayout bubble = new LinearLayout(context);
        bubble.setOrientation(LinearLayout.HORIZONTAL);
        bubble.setBackgroundResource(R.drawable.tutorial_bubble_background);
        bubble.setPadding(20, 20, 20, 20);
        bubble.setElevation(16);

        // Prevent tap-through on bubble
        bubble.setClickable(true);
        bubble.setOnClickListener(v -> {
            // Do nothing - prevents overlay click from triggering
        });

        // Avatar (compact)
        ImageView avatar = new ImageView(context);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(70, 70);
        avatarParams.rightMargin = 16;
        avatarParams.gravity = Gravity.CENTER_VERTICAL;
        avatar.setLayoutParams(avatarParams);
        avatar.setImageResource(step.avatarImage);
        avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        bubble.addView(avatar);

        // Content
        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        contentLayout.setLayoutParams(contentParams);

        // Title
        TextView titleText = new TextView(context);
        titleText.setText(step.title);
        titleText.setTextSize(15);
        titleText.setTextColor(Color.parseColor("#1A1A1A"));
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = 6;
        titleText.setLayoutParams(titleParams);
        contentLayout.addView(titleText);

        // Message
        TextView messageText = new TextView(context);
        messageText.setText(step.message);
        messageText.setTextSize(12);
        messageText.setTextColor(Color.parseColor("#666666"));
        messageText.setLineSpacing(3, 1.0f);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        messageParams.bottomMargin = 8;
        messageText.setLayoutParams(messageParams);
        contentLayout.addView(messageText);

        // Bottom row: Progress + Skip
        LinearLayout bottomRow = new LinearLayout(context);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);

        // Progress
        TextView progressText = new TextView(context);
        progressText.setText((currentStep + 1) + "/" + tutorialSteps.length);
        progressText.setTextSize(11);
        progressText.setTextColor(Color.parseColor("#D4AF37"));
        progressText.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        progressText.setLayoutParams(progressParams);
        bottomRow.addView(progressText);

        // Skip link (if not last step)
        if (currentStep < tutorialSteps.length - 1) {
            TextView skipText = new TextView(context);
            skipText.setText("Skip tour →");
            skipText.setTextSize(11);
            skipText.setTextColor(Color.parseColor("#999999"));
            skipText.setPadding(8, 8, 8, 8);
            skipText.setOnClickListener(v -> {
                removeTutorialOverlay();
                if (listener != null) {
                    listener.onTutorialComplete();
                }
            });
            bottomRow.addView(skipText);
        }

        contentLayout.addView(bottomRow);
        bubble.addView(contentLayout);

        // Smart positioning to keep within screen bounds
        FrameLayout.LayoutParams bubbleParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bubbleParams.setMargins(20, 0, 20, 0);

        int targetTop = location[1];
        int targetBottom = location[1] + step.targetView.getHeight();
        int targetHeight = step.targetView.getHeight();

        // Calculate bubble height (estimate)
        int estimatedBubbleHeight = 140;

        if (step.pointerPosition == 2) { // Top
            // Position above target
            int desiredTop = targetTop - estimatedBubbleHeight - 16;

            // Check if it goes off top of screen
            if (desiredTop < 60) { // 60dp for status bar
                // Switch to bottom
                bubbleParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                bubbleParams.topMargin = targetBottom + 16;
            } else {
                bubbleParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                bubbleParams.topMargin = desiredTop;
            }
        } else { // Bottom
            // Position below target
            int desiredTop = targetBottom + 16;

            // Check if it goes off bottom of screen
            if (desiredTop + estimatedBubbleHeight > screenHeight - 100) {
                // Switch to top
                bubbleParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                bubbleParams.topMargin = targetTop - estimatedBubbleHeight - 16;

                // Still check if top position is valid
                if (bubbleParams.topMargin < 60) {
                    // Center it instead
                    bubbleParams.gravity = Gravity.CENTER;
                    bubbleParams.topMargin = 0;
                }
            } else {
                bubbleParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                bubbleParams.topMargin = desiredTop;
            }
        }

        bubble.setLayoutParams(bubbleParams);
        overlay.addView(bubble);
    }

    private void createCenterTutorial(TutorialStep step, FrameLayout overlay, OnTutorialCompleteListener listener) {
        LinearLayout centerCard = new LinearLayout(context);
        centerCard.setOrientation(LinearLayout.VERTICAL);
        centerCard.setBackgroundResource(R.drawable.tutorial_card_background);
        centerCard.setPadding(40, 40, 40, 40);
        centerCard.setElevation(16);
        centerCard.setGravity(Gravity.CENTER);

        // Prevent tap-through
        centerCard.setClickable(true);
        centerCard.setOnClickListener(v -> {
            // Do nothing - prevents overlay click
        });

        // Avatar
        ImageView avatar = new ImageView(context);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(180, 180);
        avatarParams.gravity = Gravity.CENTER_HORIZONTAL;
        avatarParams.bottomMargin = 24;
        avatar.setLayoutParams(avatarParams);
        avatar.setImageResource(step.avatarImage);
        avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        centerCard.addView(avatar);

        // Title
        TextView titleText = new TextView(context);
        titleText.setText(step.title);
        titleText.setTextSize(19);
        titleText.setTextColor(Color.parseColor("#1A1A1A"));
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = 16;
        titleText.setLayoutParams(titleParams);
        centerCard.addView(titleText);

        // Message
        TextView messageText = new TextView(context);
        messageText.setText(step.message);
        messageText.setTextSize(14);
        messageText.setTextColor(Color.parseColor("#666666"));
        messageText.setGravity(Gravity.CENTER);
        messageText.setLineSpacing(5, 1.0f);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        messageParams.bottomMargin = 24;
        messageText.setLayoutParams(messageParams);
        centerCard.addView(messageText);

        // Tap anywhere instruction
        TextView tapText = new TextView(context);
        tapText.setText(currentStep == tutorialSteps.length - 1 ? "Tap anywhere to begin" : "Tap anywhere to continue");
        tapText.setTextSize(12);
        tapText.setTextColor(Color.parseColor("#D4AF37"));
        tapText.setGravity(Gravity.CENTER);
        tapText.setTypeface(null, android.graphics.Typeface.BOLD);
        centerCard.addView(tapText);

        FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        centerParams.gravity = Gravity.CENTER;
        centerParams.setMargins(32, 0, 32, 0);
        centerCard.setLayoutParams(centerParams);

        overlay.addView(centerCard);
    }

    private void removeTutorialOverlay() {
        if (overlayLayout != null && rootView != null) {
            rootView.removeView(overlayLayout);
            overlayLayout = null;
        }
    }

    public interface OnTutorialCompleteListener {
        void onTutorialComplete();
    }
}