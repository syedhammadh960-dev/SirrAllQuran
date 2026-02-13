package com.example.sirralquran.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.sirralquran.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ Ashra Day Detail Activity - Shows complete day content from JSON
 */
public class AshraDayDetailActivity extends AppCompatActivity {

    private static final String TAG = "AshraDayDetail";
    private static final String PREFS_NAME = "AshraDayPrefs";

    // Intent extras
    private int dayNumber;
    private int ashraNumber;
    private String dayTitle;
    private String dayDescription;
    private String duaArabic;
    private String duaTransliteration;
    private String duaTranslation;
    private String[] actions;
    private String hadithText;
    private String hadithReference;

    // Header views
    private CollapsingToolbarLayout collapsingToolbar;
    private View headerGradient;
    private View dayCircle;
    private TextView dayNumberText;
    private TextView dayTitleText;
    private TextView ashraInfoText;

    // Content views
    private TextView arabicThemeText;
    private TextView englishThemeText;
    private TextView dayDescriptionText;

    // Dua views
    private TextView duaArabicText;
    private TextView duaTransliterationText;
    private TextView duaTranslationText;
    private MaterialButton viewDuaDetailsButton;

    // Checklist views
    private CheckBox action1Checkbox;
    private CheckBox action2Checkbox;
    private CheckBox action3Checkbox;
    private TextView action1Text;
    private TextView action2Text;
    private TextView action3Text;
    private TextView progressText;
    private TextView progressPercentText;

    // Hadith views
    private TextView hadithTextView;
    private TextView hadithReferenceTextView;

    // Reflection views
    private TextInputEditText reflectionInput;
    private MaterialButton saveReflectionButton;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ashra_day_detail);

        getIntentData();
        initializeViews();
        setupToolbar();
        setupColorTheme();
        loadContent();
        setupClickListeners();
        loadSavedData();
    }

    private void getIntentData() {
        dayNumber = getIntent().getIntExtra("DAY_NUMBER", 1);
        ashraNumber = getIntent().getIntExtra("ASHRA_NUMBER", 1);
        dayTitle = getIntent().getStringExtra("TITLE");
        dayDescription = getIntent().getStringExtra("DESCRIPTION");
        duaArabic = getIntent().getStringExtra("DUA_ARABIC");
        duaTransliteration = getIntent().getStringExtra("DUA_TRANSLITERATION");
        duaTranslation = getIntent().getStringExtra("DUA_TRANSLATION");
        actions = getIntent().getStringArrayExtra("ACTIONS");
        hadithText = getIntent().getStringExtra("HADITH_TEXT");
        hadithReference = getIntent().getStringExtra("HADITH_REFERENCE");

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void initializeViews() {
        // Header
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        headerGradient = findViewById(R.id.headerGradient);
        dayCircle = findViewById(R.id.dayCircle);
        dayNumberText = findViewById(R.id.dayNumberText);
        dayTitleText = findViewById(R.id.dayTitleText);
        ashraInfoText = findViewById(R.id.ashraInfoText);

        // Theme
        arabicThemeText = findViewById(R.id.arabicThemeText);
        englishThemeText = findViewById(R.id.englishThemeText);
        dayDescriptionText = findViewById(R.id.dayDescriptionText);

        // Dua
        duaArabicText = findViewById(R.id.duaArabicText);
        duaTransliterationText = findViewById(R.id.duaTransliterationText);
        duaTranslationText = findViewById(R.id.duaTranslationText);
        viewDuaDetailsButton = findViewById(R.id.viewDuaDetailsButton);

        // Checklist
        action1Checkbox = findViewById(R.id.action1Checkbox);
        action2Checkbox = findViewById(R.id.action2Checkbox);
        action3Checkbox = findViewById(R.id.action3Checkbox);
        action1Text = findViewById(R.id.action1Text);
        action2Text = findViewById(R.id.action2Text);
        action3Text = findViewById(R.id.action3Text);
        progressText = findViewById(R.id.progressText);
        progressPercentText = findViewById(R.id.progressPercentText);

        // Hadith
        hadithTextView = findViewById(R.id.hadithText);
        hadithReferenceTextView = findViewById(R.id.hadithReferenceText);

        // Reflection
        reflectionInput = findViewById(R.id.reflectionInput);
        saveReflectionButton = findViewById(R.id.saveReflectionButton);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
        collapsingToolbar.setTitleEnabled(false);
    }

    private void setupColorTheme() {
        int gradientRes, circleRes;

        switch (ashraNumber) {
            case 1:
                gradientRes = R.drawable.gradient_ashra_gold;
                circleRes = R.drawable.circle_day_detail_gold;
                break;
            case 2:
                gradientRes = R.drawable.gradient_ashra_teal;
                circleRes = R.drawable.circle_day_detail_teal;
                break;
            case 3:
                gradientRes = R.drawable.gradient_ashra_purple;
                circleRes = R.drawable.circle_day_detail_purple;
                break;
            default:
                gradientRes = R.drawable.gradient_ashra_gold;
                circleRes = R.drawable.circle_day_detail_gold;
        }

        headerGradient.setBackgroundResource(gradientRes);
        dayCircle.setBackgroundResource(circleRes);
    }

    private void loadContent() {
        // Set header
        dayNumberText.setText(String.valueOf(dayNumber));
        dayTitleText.setText(dayTitle);
        ashraInfoText.setText(getAshraInfo());

        // Set theme
        String[] theme = getAshraTheme();
        arabicThemeText.setText(theme[0]);
        englishThemeText.setText(theme[1]);
        int themeColor = getThemeColor();
        arabicThemeText.setTextColor(themeColor);

        // Set description
        dayDescriptionText.setText(dayDescription);

        // Set dua
        duaArabicText.setText(duaArabic);
        duaTransliterationText.setText(duaTransliteration);
        duaTranslationText.setText(duaTranslation);

        // Set actions
        if (actions != null && actions.length >= 3) {
            action1Text.setText(actions[0]);
            action2Text.setText(actions[1]);
            action3Text.setText(actions[2]);
        }

        // Set hadith
        hadithTextView.setText(hadithText != null ? hadithText : "Continue seeking knowledge and remembrance.");
        hadithReferenceTextView.setText(hadithReference != null ? "— " + hadithReference : "");
    }

    private String getAshraInfo() {
        switch (ashraNumber) {
            case 1:
                return "1st Ashra - Days of Mercy (رَحْمَة)";
            case 2:
                return "2nd Ashra - Days of Forgiveness (مَغْفِرَة)";
            case 3:
                return "3rd Ashra - Days of Protection (نَجَاة)";
            default:
                return "Ramadan Ashra";
        }
    }

    private String[] getAshraTheme() {
        switch (ashraNumber) {
            case 1:
                return new String[]{"رَحْمَة", "Mercy"};
            case 2:
                return new String[]{"مَغْفِرَة", "Forgiveness"};
            case 3:
                return new String[]{"نَجَاة مِنَ النَّار", "Safety from Hellfire"};
            default:
                return new String[]{"رَحْمَة", "Mercy"};
        }
    }

    private int getThemeColor() {
        switch (ashraNumber) {
            case 1:
                return getResources().getColor(R.color.accent_gold);
            case 2:
                return getResources().getColor(R.color.accent_teal);
            case 3:
                return getResources().getColor(R.color.accent_purple);
            default:
                return getResources().getColor(R.color.accent_gold);
        }
    }

    private void setupClickListeners() {
        // View Dua Details button
        viewDuaDetailsButton.setOnClickListener(v -> openDuaDetail());

        // Checkboxes
        action1Checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveCheckboxState(1, isChecked);
            updateProgress();
        });

        action2Checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveCheckboxState(2, isChecked);
            updateProgress();
        });

        action3Checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveCheckboxState(3, isChecked);
            updateProgress();
        });

        // Save reflection
        saveReflectionButton.setOnClickListener(v -> saveReflection());
    }

    private void openDuaDetail() {
        Intent intent = new Intent(this, DuaDetailActivity.class);
        intent.putExtra("DAY_NUMBER", dayNumber);
        intent.putExtra("ASHRA_NUMBER", ashraNumber);
        intent.putExtra("DUA_ARABIC", duaArabic);
        intent.putExtra("DUA_TRANSLITERATION", duaTransliteration);
        intent.putExtra("DUA_TRANSLATION", duaTranslation);
        startActivity(intent);
    }

    private void saveCheckboxState(int actionNumber, boolean checked) {
        prefs.edit()
                .putBoolean("day_" + dayNumber + "_action_" + actionNumber, checked)
                .apply();
    }

    private void updateProgress() {
        int completed = 0;
        if (action1Checkbox.isChecked()) completed++;
        if (action2Checkbox.isChecked()) completed++;
        if (action3Checkbox.isChecked()) completed++;

        int percent = (completed * 100) / 3;

        progressText.setText(completed + " of 3 completed");
        progressPercentText.setText(percent + "%");
    }

    private void saveReflection() {
        String reflection = reflectionInput.getText().toString();

        if (reflection.isEmpty()) {
            Toast.makeText(this, "Please write something first", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit()
                .putString("day_" + dayNumber + "_reflection", reflection)
                .apply();

        Toast.makeText(this, "Reflection saved ✓", Toast.LENGTH_SHORT).show();
    }

    private void loadSavedData() {
        action1Checkbox.setChecked(prefs.getBoolean("day_" + dayNumber + "_action_1", false));
        action2Checkbox.setChecked(prefs.getBoolean("day_" + dayNumber + "_action_2", false));
        action3Checkbox.setChecked(prefs.getBoolean("day_" + dayNumber + "_action_3", false));

        updateProgress();

        String savedReflection = prefs.getString("day_" + dayNumber + "_reflection", "");
        if (!savedReflection.isEmpty()) {
            reflectionInput.setText(savedReflection);
        }
    }
}