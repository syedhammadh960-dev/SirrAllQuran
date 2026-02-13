package com.example.sirralquran.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.example.sirralquran.R;
import com.example.sirralquran.models.DailyHadith;
import com.example.sirralquran.models.DailyAyah;
import com.example.sirralquran.utils.HadithAyatDuaDataHelper;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ FIXED: Dua Detail Activity with Smart Recommendations
 * Uses DailyHadith, DailyAyah, HadithAyatDuaDataHelper
 */
public class DuaDetailActivity extends AppCompatActivity {

    private static final String TAG = "DuaDetail";

    // Intent extras
    private int dayNumber;
    private int ashraNumber;
    private String duaArabic;
    private String duaTransliteration;
    private String duaTranslation;

    // Views
    private CollapsingToolbarLayout collapsingToolbar;
    private View headerGradient;
    private TextView duaTitleText;
    private TextView ashraInfoText;
    private TextView duaArabicText;
    private TextView duaTransliterationText;
    private TextView duaTranslationText;
    private MaterialButton copyDuaButton;
    private MaterialButton shareDuaButton;

    // Recommendation views
    private CardView recommendedHadithCard;
    private TextView recommendedHadithText;
    private MaterialButton viewHadithDetailsButton;

    private CardView recommendedAyatCard;
    private TextView recommendedAyatText;
    private MaterialButton viewAyatDetailsButton;

    // ✅ FIXED: Using correct models and helper
    private HadithAyatDuaDataHelper recommendationHelper;
    private DailyHadith recommendedHadith;
    private DailyAyah recommendedAyat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dua_detail);

        recommendationHelper = new HadithAyatDuaDataHelper(this);

        getIntentData();
        initializeViews();
        setupToolbar();
        setupColorTheme();
        loadDuaContent();
        loadRecommendations();
        setupClickListeners();
        markAsViewed();
    }

    private void getIntentData() {
        dayNumber = getIntent().getIntExtra("DAY_NUMBER", 1);
        ashraNumber = getIntent().getIntExtra("ASHRA_NUMBER", 1);
        duaArabic = getIntent().getStringExtra("DUA_ARABIC");
        duaTransliteration = getIntent().getStringExtra("DUA_TRANSLITERATION");
        duaTranslation = getIntent().getStringExtra("DUA_TRANSLATION");
    }

    private void initializeViews() {
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        headerGradient = findViewById(R.id.headerGradient);
        duaTitleText = findViewById(R.id.duaTitleText);
        ashraInfoText = findViewById(R.id.ashraInfoText);
        duaArabicText = findViewById(R.id.duaArabicText);
        duaTransliterationText = findViewById(R.id.duaTransliterationText);
        duaTranslationText = findViewById(R.id.duaTranslationText);
        copyDuaButton = findViewById(R.id.copyDuaButton);
        shareDuaButton = findViewById(R.id.shareDuaButton);

        // Recommendation views
        recommendedHadithCard = findViewById(R.id.recommendedHadithCard);
        recommendedHadithText = findViewById(R.id.recommendedHadithText);
        viewHadithDetailsButton = findViewById(R.id.viewHadithDetailsButton);

        recommendedAyatCard = findViewById(R.id.recommendedAyatCard);
        recommendedAyatText = findViewById(R.id.recommendedAyatText);
        viewAyatDetailsButton = findViewById(R.id.viewAyatDetailsButton);
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
        int gradientRes;

        switch (ashraNumber) {
            case 1:
                gradientRes = R.drawable.gradient_ashra_gold;
                break;
            case 2:
                gradientRes = R.drawable.gradient_ashra_teal;
                break;
            case 3:
                gradientRes = R.drawable.gradient_ashra_purple;
                break;
            default:
                gradientRes = R.drawable.gradient_ashra_gold;
        }

        headerGradient.setBackgroundResource(gradientRes);
    }

    private void loadDuaContent() {
        duaTitleText.setText("Dua for Day " + dayNumber);
        ashraInfoText.setText(getAshraInfo());

        duaArabicText.setText(duaArabic);
        duaTransliterationText.setText(duaTransliteration);
        duaTranslationText.setText(duaTranslation);
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

    /**
     * Load recommendations based on keywords
     */
    private void loadRecommendations() {
        String[] keywords = getKeywordsForDua();

        // Get recommended Hadith and Ayat
        recommendedHadith = recommendationHelper.getRecommendedHadith(keywords);
        recommendedAyat = recommendationHelper.getRecommendedAyat(keywords);

        // Display recommendations
        if (recommendedHadith != null) {
            displayRecommendedHadith();
        } else {
            recommendedHadithCard.setVisibility(View.GONE);
        }

        if (recommendedAyat != null) {
            displayRecommendedAyat();
        } else {
            recommendedAyatCard.setVisibility(View.GONE);
        }
    }

    /**
     * Auto-detect keywords from dua content
     */
    private String[] getKeywordsForDua() {
        List<String> keywords = new ArrayList<>();

        String duaContent = (duaArabic + " " + duaTransliteration + " " + duaTranslation).toLowerCase();

        // Ramadan keywords
        if (duaContent.contains("ramadan") || duaContent.contains("رمضان")) {
            keywords.add("ramadan");
            keywords.add("fasting");
            keywords.add("صوم");
        }

        // Eid keywords
        if (duaContent.contains("eid") || duaContent.contains("عيد")) {
            keywords.add("eid");
            keywords.add("عيد");
        }

        // Forgiveness keywords
        if (duaContent.contains("forgive") || duaContent.contains("غفر") || duaContent.contains("maghfir")) {
            keywords.add("forgive");
            keywords.add("forgiveness");
            keywords.add("غفر");
            keywords.add("mercy");
        }

        // Laylatul Qadr keywords
        if (duaContent.contains("laylatul qadr") || duaContent.contains("ليلة القدر") || duaContent.contains("qadr")) {
            keywords.add("laylatul qadr");
            keywords.add("night");
            keywords.add("ليلة");
            keywords.add("قدر");
        }

        // Prayer keywords
        if (duaContent.contains("prayer") || duaContent.contains("salah") || duaContent.contains("صلاة")) {
            keywords.add("prayer");
            keywords.add("salah");
            keywords.add("صلاة");
        }

        // Quran keywords
        if (duaContent.contains("quran") || duaContent.contains("qur'an") || duaContent.contains("قرآن")) {
            keywords.add("quran");
            keywords.add("قرآن");
        }

        // Taqwa keywords
        if (duaContent.contains("taqwa") || duaContent.contains("تقوى")) {
            keywords.add("taqwa");
            keywords.add("تقوى");
        }

        // Paradise keywords
        if (duaContent.contains("paradise") || duaContent.contains("jannah") || duaContent.contains("جنة")) {
            keywords.add("paradise");
            keywords.add("jannah");
            keywords.add("جنة");
        }

        // Default to Ramadan if no keywords found
        if (keywords.isEmpty()) {
            keywords.add("ramadan");
            keywords.add("fasting");
        }

        return keywords.toArray(new String[0]);
    }

    private void displayRecommendedHadith() {
        recommendedHadithText.setText(recommendedHadith.getEnglish());
        recommendedHadithCard.setVisibility(View.VISIBLE);
    }

    private void displayRecommendedAyat() {
        recommendedAyatText.setText(recommendedAyat.getEnglish());
        recommendedAyatCard.setVisibility(View.VISIBLE);
    }

    private void setupClickListeners() {
        copyDuaButton.setOnClickListener(v -> copyDua());
        shareDuaButton.setOnClickListener(v -> shareDua());

        viewHadithDetailsButton.setOnClickListener(v -> openHadithDetail());
        viewAyatDetailsButton.setOnClickListener(v -> openAyatDetail());
    }

    private void copyDua() {
        String fullDua = "🤲 Dua for Day " + dayNumber + "\n\n" +
                "Arabic:\n" + duaArabic + "\n\n" +
                "Transliteration:\n" + duaTransliteration + "\n\n" +
                "Translation:\n" + duaTranslation;

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Ramadan Dua", fullDua);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Dua copied to clipboard ✓", Toast.LENGTH_SHORT).show();
    }

    private void shareDua() {
        String fullDua = "🤲 Ramadan Dua - Day " + dayNumber + "\n\n" +
                "Arabic:\n" + duaArabic + "\n\n" +
                "Transliteration:\n" + duaTransliteration + "\n\n" +
                "Translation:\n" + duaTranslation + "\n\n" +
                "From Sirr-Ul-Quran App 🌙";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Ramadan Dua - Day " + dayNumber);
        shareIntent.putExtra(Intent.EXTRA_TEXT, fullDua);

        startActivity(Intent.createChooser(shareIntent, "Share Dua via"));
    }

    private void openHadithDetail() {
        if (recommendedHadith == null) return;

        Intent intent = new Intent(this, HadithDetailActivity.class);
        intent.putExtra("HADITH_DAY", recommendedHadith.getDayNumber());
        intent.putExtra("ARABIC", recommendedHadith.getArabic());
        intent.putExtra("ENGLISH", recommendedHadith.getEnglish());
        intent.putExtra("REFERENCE", recommendedHadith.getReference());
        startActivity(intent);
    }

    private void openAyatDetail() {
        if (recommendedAyat == null) return;

        Intent intent = new Intent(this, AyatDetailActivity.class);
        intent.putExtra("AYAT_DAY", recommendedAyat.getDayNumber());
        intent.putExtra("ARABIC", recommendedAyat.getArabic());
        intent.putExtra("ENGLISH", recommendedAyat.getEnglish());
        intent.putExtra("REFERENCE", recommendedAyat.getReference());
        startActivity(intent);
    }

    private void markAsViewed() {
        recommendationHelper.getViewedManager().markDuaAsViewed(dayNumber);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh recommendations in case user viewed something
        loadRecommendations();
    }
}