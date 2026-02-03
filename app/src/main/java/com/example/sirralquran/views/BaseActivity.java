package com.example.sirralquran.views;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Base Activity that handles:
 * 1. Hiding navigation bar (back/home buttons)
 * 2. Automatically adding status bar padding to ALL activities
 * 3. Immersive fullscreen experience
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Don't hide system UI here - window not ready yet!
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        // Now window is ready, we can hide system UI
        hideSystemUI();

        // Automatically apply status bar padding after content is set
        applyStatusBarPaddingToRoot();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);

        // Now window is ready, we can hide system UI
        hideSystemUI();

        // Automatically apply status bar padding after content is set
        applyStatusBarPaddingToRoot();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-hide navigation bar when returning to activity
        hideSystemUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Re-hide navigation bar when window regains focus
            hideSystemUI();
        }
    }

    /**
     * Hides the navigation bar (back/home buttons)
     * Makes the app truly fullscreen
     */
    private void hideSystemUI() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // For Android 11 and above
                WindowInsetsController controller = getWindow().getInsetsController();
                if (controller != null) {
                    // Hide navigation bar
                    controller.hide(WindowInsets.Type.navigationBars());

                    // Set behavior: swipe to show, auto-hide again
                    controller.setSystemBarsBehavior(
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    );
                }

                // Keep status bar visible but add padding for content
                getWindow().setDecorFitsSystemWindows(false);

            } else {
                // For Android 10 and below
                View decorView = getWindow().getDecorView();
                if (decorView != null) {
                    int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

                    decorView.setSystemUiVisibility(flags);
                }
            }
        } catch (Exception e) {
            // If something goes wrong, just continue without hiding nav bar
            e.printStackTrace();
        }
    }

    /**
     * Automatically applies status bar padding to the root view
     * Called after setContentView()
     */
    private void applyStatusBarPaddingToRoot() {
        try {
            // Get the root view
            View rootView = findViewById(android.R.id.content);
            if (rootView != null) {
                ViewGroup decorView = (ViewGroup) rootView;
                if (decorView.getChildCount() > 0) {
                    View contentView = decorView.getChildAt(0);
                    applyStatusBarPadding(contentView);
                }
            }
        } catch (Exception e) {
            // If something goes wrong, just continue without padding
            e.printStackTrace();
        }
    }

    /**
     * Apply status bar padding to a specific view
     * This prevents content from being hidden behind the status bar
     * Padding is reduced to 60% for better visual balance
     */
    protected void applyStatusBarPadding(View view) {
        if (view == null) return;

        try {
            ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
                // Get status bar height
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

                // Reduce padding to 60% of status bar height for better balance
                int reducedPadding = (int) (statusBarHeight * 0.6f);

                // Apply reduced padding to top (preserve existing padding)
                v.setPadding(
                        v.getPaddingLeft(),
                        reducedPadding + v.getPaddingTop(),
                        v.getPaddingRight(),
                        v.getPaddingBottom()
                );

                return insets;
            });

            // Request to apply insets
            ViewCompat.requestApplyInsets(view);
        } catch (Exception e) {
            // If something goes wrong, just continue
            e.printStackTrace();
        }
    }

    /**
     * Show navigation bar temporarily (for special cases)
     */
    protected void showSystemUI() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController controller = getWindow().getInsetsController();
                if (controller != null) {
                    controller.show(WindowInsets.Type.navigationBars());
                }
            } else {
                View decorView = getWindow().getDecorView();
                if (decorView != null) {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}