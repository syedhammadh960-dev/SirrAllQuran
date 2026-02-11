package com.example.sirralquran.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.sirralquran.R;
import com.example.sirralquran.controllers.RamadanManager;
import com.example.sirralquran.controllers.UserController;
import com.example.sirralquran.models.DailyAyah;
import com.example.sirralquran.models.DailyHadith;
import com.example.sirralquran.models.User;
import com.example.sirralquran.utils.FirebaseHelper;
import com.example.sirralquran.utils.TutorialOverlayHelper;
import com.example.sirralquran.utils.TutorialOverlayHelper.TutorialStep;

/**
 * FIXED HomeActivity - Forces Firebase data load immediately
 */
public class HomeActivity extends BaseActivity {

    private static final String TAG = "HomeActivity";

    private TextView userNameText;
    private TextView currentDayText;
    private TextView progressPercentText;
    private TextView motivationalText;
    private ImageView profileImage;
    private CardView continueJourneyCard;
    private CardView salahTrackerCard;
    private CardView thirtyDayCard;
    private CardView ayahCard;
    private CardView hadithCard;
    private Button continueReadingButton;
    private ProgressBar journeyProgressBar;
    private TextView askQuestionText;
    private BottomNavigationView bottomNavigationView;

    // Ayah card views
    private TextView ayahArabicText;
    private TextView ayahEnglishText;
    private TextView ayahReferenceText;

    // Hadith card views
    private TextView hadithArabicText;
    private TextView hadithEnglishText;
    private TextView hadithReferenceText;

    private UserController userController;
    private TutorialOverlayHelper tutorialHelper;
    private FirebaseHelper firebaseHelper;
    private RamadanManager ramadanManager;

    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        userController = new UserController(this);
        tutorialHelper = new TutorialOverlayHelper(this);
        firebaseHelper = FirebaseHelper.getInstance();
        ramadanManager = new RamadanManager(this);

        // CRITICAL FIX: Force immediate Firebase load
        Log.d(TAG, "🚀 Starting Firebase data load...");
        checkRamadanAndLoadContent();

        loadUserData();
        setupClickListeners();
        setupBottomNavigation();

        // Show tutorial for new users
        new Handler().postDelayed(() -> {
            if (tutorialHelper.shouldShowHomeTutorial()) {
                showHomeTutorial();
            }
        }, 500);
    }

    private void initializeViews() {
        userNameText = findViewById(R.id.userNameText);
        currentDayText = findViewById(R.id.currentDayText);
        progressPercentText = findViewById(R.id.progressPercentText);
        motivationalText = findViewById(R.id.motivationalText);
        profileImage = findViewById(R.id.profileImage);
        continueJourneyCard = findViewById(R.id.continueJourneyCard);
        salahTrackerCard = findViewById(R.id.salahTrackerCard);
        thirtyDayCard = findViewById(R.id.thirtyDayCard);
        ayahCard = findViewById(R.id.ayahCard);
        hadithCard = findViewById(R.id.hadithCard);
        continueReadingButton = findViewById(R.id.continueReadingButton);
        journeyProgressBar = findViewById(R.id.journeyProgressBar);
        askQuestionText = findViewById(R.id.askQuestionText);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Ayah card views
        ayahArabicText = findViewById(R.id.ayahArabicText);
        ayahEnglishText = findViewById(R.id.ayahEnglishText);
        ayahReferenceText = findViewById(R.id.ayahReferenceText);

        // Hadith card views
        hadithArabicText = findViewById(R.id.hadithArabicText);
        hadithEnglishText = findViewById(R.id.hadithEnglishText);
        hadithReferenceText = findViewById(R.id.hadithReferenceText);
    }

    /**
     * FIXED: Force Firebase check with timeout
     */
    private void checkRamadanAndLoadContent() {
        final boolean[] dataLoaded = {false};

        Log.d(TAG, "📡 Checking Ramadan status from Firebase...");

        // 10-second timeout
        new Handler().postDelayed(() -> {
            if (!dataLoaded[0]) {
                Log.e(TAG, "⏱️ Firebase timeout! Using fallback Day 3");
                // Fallback: Load Day 3 directly
                loadDailyWisdomFromFirebase(3);
                currentDayText.setText("Day 1");
            }
        }, 10000);

        ramadanManager.checkRamadanStatus(new RamadanManager.OnRamadanStatusListener() {
            @Override
            public void onStatusChecked(boolean isRamadan, int currentDay) {
                dataLoaded[0] = true;

                Log.d(TAG, "✅ Firebase responded: isRamadan=" + isRamadan + ", currentDay=" + currentDay);

                if (isRamadan && currentDay > 0) {
                    // Ramadan mode: Load day-specific content from Firebase
                    Log.d(TAG, "🌙 Ramadan active! Loading Day " + currentDay);
                    loadDailyWisdomFromFirebase(currentDay);
                    currentDayText.setText("Day " + currentDay);
                } else {
                    // Normal mode: Show placeholder
                    Log.d(TAG, "⚠️ Ramadan not active, showing placeholders");
                    showPlaceholderContent();
                    currentDayText.setText("Welcome");
                }
            }
        });
    }

    /**
     * FIXED: Load daily Ayah and Hadith with proper error handling and timeout
     */
    private void loadDailyWisdomFromFirebase(int dayNumber) {
        Log.d(TAG, "🔍 Loading Ayah & Hadith for Day " + dayNumber);

        final boolean[] ayahLoaded = {false};
        final boolean[] hadithLoaded = {false};

        // 10-second timeout for each
        new Handler().postDelayed(() -> {
            if (!ayahLoaded[0]) {
                Log.e(TAG, "⏱️ Ayah load timeout!");
                Toast.makeText(this, "⚠️ Ayah data not loading. Check Firebase.", Toast.LENGTH_LONG).show();
            }
        }, 10000);

        new Handler().postDelayed(() -> {
            if (!hadithLoaded[0]) {
                Log.e(TAG, "⏱️ Hadith load timeout!");
                Toast.makeText(this, "⚠️ Hadith data not loading. Check Firebase.", Toast.LENGTH_LONG).show();
            }
        }, 10000);

        // Load Ayah
        firebaseHelper.getAyahByDay(dayNumber, new FirebaseHelper.OnDataLoadListener<DailyAyah>() {
            @Override
            public void onSuccess(DailyAyah ayah) {
                ayahLoaded[0] = true;
                Log.d(TAG, "✅ Ayah loaded successfully!");
                displayAyah(ayah);
            }

            @Override
            public void onError(String error) {
                ayahLoaded[0] = true;
                Log.e(TAG, "❌ Failed to load Ayah: " + error);
                Toast.makeText(HomeActivity.this, "Ayah error: " + error, Toast.LENGTH_LONG).show();
            }
        });

        // Load Hadith
        firebaseHelper.getHadithByDay(dayNumber, new FirebaseHelper.OnDataLoadListener<DailyHadith>() {
            @Override
            public void onSuccess(DailyHadith hadith) {
                hadithLoaded[0] = true;
                Log.d(TAG, "✅ Hadith loaded successfully!");
                displayHadith(hadith);
            }

            @Override
            public void onError(String error) {
                hadithLoaded[0] = true;
                Log.e(TAG, "❌ Failed to load Hadith: " + error);
                Toast.makeText(HomeActivity.this, "Hadith error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Display Ayah in card
     */
    private void displayAyah(DailyAyah ayah) {
        Log.d(TAG, "📖 Displaying Ayah: " + ayah.getReference());
        if (ayahArabicText != null) {
            ayahArabicText.setText(ayah.getArabic());
        }
        if (ayahEnglishText != null) {
            ayahEnglishText.setText(ayah.getEnglish());
        }
        if (ayahReferenceText != null) {
            ayahReferenceText.setText(ayah.getReference());
        }
    }

    /**
     * Display Hadith in card
     */
    private void displayHadith(DailyHadith hadith) {
        Log.d(TAG, "📜 Displaying Hadith: " + hadith.getReference());
        if (hadithArabicText != null) {
            hadithArabicText.setText(hadith.getArabic());
        }
        if (hadithEnglishText != null) {
            hadithEnglishText.setText(hadith.getEnglish());
        }
        if (hadithReferenceText != null) {
            hadithReferenceText.setText(hadith.getReference());
        }
    }

    /**
     * Show placeholder content (when not Ramadan)
     */
    private void showPlaceholderContent() {
        if (ayahEnglishText != null) {
            ayahEnglishText.setText("Daily Ayah will appear here during Ramadan");
        }
        if (hadithEnglishText != null) {
            hadithEnglishText.setText("Daily Hadith will appear here during Ramadan");
        }
    }

    private void loadUserData() {
        User user = userController.getCurrentUser();
        if (user != null) {
            userNameText.setText(user.getFullName());
            int progress = ramadanManager.getProgressPercentage();
            progressPercentText.setText(progress + "% Complete");
            journeyProgressBar.setProgress(progress);
        } else {
            userNameText.setText("Hammad");
            int progress = ramadanManager.getProgressPercentage();
            progressPercentText.setText(progress + "% Complete");
            journeyProgressBar.setProgress(progress);
        }
    }

    private void showHomeTutorial() {
        TutorialStep[] steps = new TutorialStep[]{
                new TutorialStep(
                        "Welcome to Sirr-Ul-Quran! 👋",
                        "Assalamu Alaikum! Let me show you around.",
                        R.drawable.gview2,
                        null,
                        0
                ),
                new TutorialStep(
                        "Your 30-Day Journey",
                        "Track progress and continue daily lessons here.",
                        R.drawable.gview1,
                        continueJourneyCard,
                        2
                ),
                new TutorialStep(
                        "Daily Wisdom",
                        "Reflect on daily Ayah and Hadith.",
                        R.drawable.gview2,
                        ayahCard,
                        3
                ),
                new TutorialStep(
                        "Track Your Salah",
                        "Monitor your five daily prayers.",
                        R.drawable.gview1,
                        salahTrackerCard,
                        3
                ),
                new TutorialStep(
                        "Easy Navigation",
                        "Switch between sections easily.",
                        R.drawable.gview2,
                        bottomNavigationView,
                        2
                ),
                new TutorialStep(
                        "Ready to Begin! 🌟",
                        "May Allah bless your journey. Let's start!",
                        R.drawable.gview2,
                        null,
                        0
                )
        };

        tutorialHelper.showTutorial(steps, () -> {
            tutorialHelper.markHomeTutorialShown();
        });
    }

    private void setupClickListeners() {
        continueReadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentDay = ramadanManager.getCurrentRamadanDay();
                Intent intent = new Intent(HomeActivity.this, DayContentActivity.class);
                intent.putExtra("DAY_NUMBER", currentDay);
                startActivity(intent);
            }
        });

        continueJourneyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentDay = ramadanManager.getCurrentRamadanDay();
                Intent intent = new Intent(HomeActivity.this, DayContentActivity.class);
                intent.putExtra("DAY_NUMBER", currentDay);
                startActivity(intent);
            }
        });

        salahTrackerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SalahTrackerActivity.class);
                startActivity(intent);
            }
        });

        thirtyDayCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ThirtyDayJourneyActivity.class);
                startActivity(intent);
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Reload content on card click
        ayahCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Reloading wisdom...", Toast.LENGTH_SHORT).show();
                int currentDay = ramadanManager.getCurrentRamadanDay();
                loadDailyWisdomFromFirebase(currentDay);
            }
        });

        askQuestionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle ask question
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_salah) {
                startActivity(new Intent(HomeActivity.this, SalahTrackerActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_thirty_day) {
                startActivity(new Intent(HomeActivity.this, ThirtyDayJourneyActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        // Refresh content when returning to home
        if (!isFirstLoad) {
            checkRamadanAndLoadContent();
        }
        isFirstLoad = false;
    }
}