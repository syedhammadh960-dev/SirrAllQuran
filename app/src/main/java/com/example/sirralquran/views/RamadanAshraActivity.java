package com.example.sirralquran.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sirralquran.R;
import com.example.sirralquran.adapters.DayBlockAdapter;
import com.example.sirralquran.controllers.RamadanManager;
import com.example.sirralquran.models.AshraDay;
import com.example.sirralquran.utils.AshraDataHelper;
import com.example.sirralquran.utils.HijriDateHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

/**
 * ✅ Ramadan Ashra Activity with Firebase JSON Integration
 */
public class RamadanAshraActivity extends AppCompatActivity implements DayBlockAdapter.OnDayClickListener {

    private static final String TAG = "RamadanAshra";

    // Header views
    private ImageView backButton;
    private ImageView infoButton;
    private TextView ramadanDayText;

    // Timeline circles
    private FrameLayout ashra1Circle;
    private FrameLayout ashra2Circle;
    private FrameLayout ashra3Circle;
    private View ashra1CircleBg;
    private View ashra2CircleBg;
    private View ashra3CircleBg;

    // Selected Ashra info
    private TextView selectedAshraTitle;
    private TextView selectedAshraTheme;

    // Day blocks
    private RecyclerView dayBlocksRecyclerView;
    private DayBlockAdapter dayBlockAdapter;

    private BottomNavigationView bottomNavigationView;

    private RamadanManager ramadanManager;
    private HijriDateHelper hijriDateHelper;
    private AshraDataHelper ashraDataHelper;

    private int selectedAshraNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ramadan_ashra);

        initializeViews();
        initializeControllers();
        setupClickListeners();
        setupRecyclerView();
        setupBottomNavigation();
        loadData();

        autoSelectCurrentAshra();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        infoButton = findViewById(R.id.infoButton);
        ramadanDayText = findViewById(R.id.ramadanDayText);

        ashra1Circle = findViewById(R.id.ashra1Circle);
        ashra2Circle = findViewById(R.id.ashra2Circle);
        ashra3Circle = findViewById(R.id.ashra3Circle);

        ashra1CircleBg = findViewById(R.id.ashra1CircleBg);
        ashra2CircleBg = findViewById(R.id.ashra2CircleBg);
        ashra3CircleBg = findViewById(R.id.ashra3CircleBg);

        selectedAshraTitle = findViewById(R.id.selectedAshraTitle);
        selectedAshraTheme = findViewById(R.id.selectedAshraTheme);

        dayBlocksRecyclerView = findViewById(R.id.dayBlocksRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void initializeControllers() {
        ramadanManager = new RamadanManager(this);
        hijriDateHelper = new HijriDateHelper(this);
        ashraDataHelper = new AshraDataHelper(this); // Load JSON data
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        infoButton.setOnClickListener(v -> showAshraInfoDialog());

        ashra1Circle.setOnClickListener(v -> selectAshra(1));
        ashra2Circle.setOnClickListener(v -> selectAshra(2));
        ashra3Circle.setOnClickListener(v -> selectAshra(3));
    }

    private void setupRecyclerView() {
        dayBlocksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dayBlockAdapter = new DayBlockAdapter(this, ashraDataHelper.getDaysForAshra(1), 1, this);
        dayBlocksRecyclerView.setAdapter(dayBlockAdapter);
    }

    private void loadData() {
        String hijriDate = hijriDateHelper.getCachedHijriDate();
        ramadanDayText.setText(hijriDate);
    }

    private void autoSelectCurrentAshra() {
        int currentDay = ramadanManager.getCurrentRamadanDay();
        int currentAshra = getCurrentAshraNumber(currentDay);
        selectAshra(currentAshra);
    }

    private void selectAshra(int ashraNumber) {
        selectedAshraNumber = ashraNumber;
        updateCircleStates();
        updateAshraInfo();
        loadDayBlocks();
    }

    private void updateCircleStates() {
        ashra1CircleBg.setBackgroundResource(R.drawable.circle_ashra_inactive);
        ashra2CircleBg.setBackgroundResource(R.drawable.circle_ashra_inactive);
        ashra3CircleBg.setBackgroundResource(R.drawable.circle_ashra_inactive);

        switch (selectedAshraNumber) {
            case 1:
                ashra1CircleBg.setBackgroundResource(R.drawable.circle_ashra_gold);
                break;
            case 2:
                ashra2CircleBg.setBackgroundResource(R.drawable.circle_ashra_teal);
                break;
            case 3:
                ashra3CircleBg.setBackgroundResource(R.drawable.circle_ashra_purple);
                break;
        }

        animateCircle(selectedAshraNumber);
    }

    private void animateCircle(int ashraNumber) {
        FrameLayout selectedCircle;
        switch (ashraNumber) {
            case 1:
                selectedCircle = ashra1Circle;
                break;
            case 2:
                selectedCircle = ashra2Circle;
                break;
            case 3:
                selectedCircle = ashra3Circle;
                break;
            default:
                return;
        }

        selectedCircle.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction(() -> {
                    selectedCircle.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }

    private void updateAshraInfo() {
        switch (selectedAshraNumber) {
            case 1:
                selectedAshraTitle.setText("1st Ashra - Days of Mercy");
                selectedAshraTheme.setText("رَحْمَة - Mercy");
                selectedAshraTheme.setTextColor(getResources().getColor(R.color.accent_gold));
                break;
            case 2:
                selectedAshraTitle.setText("2nd Ashra - Days of Forgiveness");
                selectedAshraTheme.setText("مَغْفِرَة - Forgiveness");
                selectedAshraTheme.setTextColor(getResources().getColor(R.color.accent_teal));
                break;
            case 3:
                selectedAshraTitle.setText("3rd Ashra - Days of Protection");
                selectedAshraTheme.setText("نَجَاة مِنَ النَّار - Safety from Hellfire");
                selectedAshraTheme.setTextColor(getResources().getColor(R.color.accent_purple));
                break;
        }
    }

    private void loadDayBlocks() {
        List<AshraDay> days = ashraDataHelper.getDaysForAshra(selectedAshraNumber);
        dayBlockAdapter.updateData(days, selectedAshraNumber);
    }

    private int getCurrentAshraNumber(int day) {
        if (day >= 1 && day <= 10) return 1;
        if (day >= 11 && day <= 20) return 2;
        if (day >= 21 && day <= 30) return 3;
        return 1;
    }

    private void showAshraInfoDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("About Ramadan Ashra")
                .setMessage("Ramadan is divided into three blessed periods:\n\n" +
                        "🌟 1st Ashra (Days 1-10): MERCY\n" +
                        "Allah's mercy descends upon believers.\n\n" +
                        "🌟 2nd Ashra (Days 11-20): FORGIVENESS\n" +
                        "Seek forgiveness for all sins.\n\n" +
                        "🌟 3rd Ashra (Days 21-30): PROTECTION\n" +
                        "Seeking safety from Hellfire and contains Laylatul Qadr.")
                .setPositiveButton("Got it", null)
                .show();
    }

    @Override
    public void onDayClick(AshraDay day) {
        Intent intent = new Intent(this, AshraDayDetailActivity.class);
        intent.putExtra("DAY_NUMBER", day.getDayNumber());
        intent.putExtra("ASHRA_NUMBER", day.getAshraNumber());
        intent.putExtra("TITLE", day.getTitle());
        intent.putExtra("DESCRIPTION", day.getDescription());
        intent.putExtra("DUA_ARABIC", day.getDuaArabic());
        intent.putExtra("DUA_TRANSLITERATION", day.getDuaTransliteration());
        intent.putExtra("DUA_TRANSLATION", day.getDuaTranslation());
        intent.putExtra("ACTIONS", day.getActions() != null ? day.getActions().toArray(new String[0]) : new String[0]);
        intent.putExtra("HADITH_TEXT", day.getHadithText());
        intent.putExtra("HADITH_REFERENCE", day.getHadithReference());
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_thirty_day);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_salah) {
                startActivity(new Intent(this, SalahTrackerActivity.class));
                finish();
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

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}