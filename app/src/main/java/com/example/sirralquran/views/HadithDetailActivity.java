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
import com.example.sirralquran.models.AshraDay;
import com.example.sirralquran.utils.HadithAyatDuaDataHelper;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ Hadith Detail Activity with Dua + Ayat Recommendations
 */
public class HadithDetailActivity extends AppCompatActivity {

    private static final String TAG = "HadithDetail";

    // Intent extras
    private int hadithDay;
    private String arabic;
    private String english;
    private String reference;

    // Main views
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView hadithTitleText;
    private TextView hadithReferenceHeader;
    private TextView hadithArabicText;
    private TextView hadithEnglishText;
    private TextView hadithReferenceText;
    private MaterialButton copyHadithButton;
    private MaterialButton shareHadithButton;

    // ✅ NEW: Recommendation views
    private CardView recommendedDuaCard;
    private TextView recommendedDuaText;
    private MaterialButton viewDuaDetailsButton;

    private CardView recommendedAyatCard;
    private TextView recommendedAyatText;
    private MaterialButton viewAyatDetailsButton;

    // ✅ NEW: Recommendation system
    private HadithAyatDuaDataHelper recommendationHelper;
    private AshraDay recommendedDua;
    private DailyAyah recommendedAyat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hadith_detail);

        recommendationHelper = new HadithAyatDuaDataHelper(this);

        getIntentData();
        initializeViews();
        setupToolbar();
        loadContent();
        loadRecommendations(); // ✅ NEW
        setupClickListeners();
        markAsViewed();
    }

    private void getIntentData() {
        hadithDay = getIntent().getIntExtra("HADITH_DAY", 1);
        arabic = getIntent().getStringExtra("ARABIC");
        english = getIntent().getStringExtra("ENGLISH");
        reference = getIntent().getStringExtra("REFERENCE");
    }

    private void initializeViews() {
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        hadithTitleText = findViewById(R.id.hadithTitleText);
        hadithReferenceHeader = findViewById(R.id.hadithReferenceHeader);
        hadithArabicText = findViewById(R.id.hadithArabicText);
        hadithEnglishText = findViewById(R.id.hadithEnglishText);
        hadithReferenceText = findViewById(R.id.hadithReferenceText);
        copyHadithButton = findViewById(R.id.copyHadithButton);
        shareHadithButton = findViewById(R.id.shareHadithButton);

        // ✅ NEW: Recommendation views
        recommendedDuaCard = findViewById(R.id.recommendedDuaCard);
        recommendedDuaText = findViewById(R.id.recommendedDuaText);
        viewDuaDetailsButton = findViewById(R.id.viewDuaDetailsButton);

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

    private void loadContent() {
        hadithTitleText.setText("Hadith - Day " + hadithDay);
        hadithReferenceHeader.setText(reference);
        hadithArabicText.setText(arabic);
        hadithEnglishText.setText(english);
        hadithReferenceText.setText(reference);
    }

    // ✅ NEW: Load recommendations
    private void loadRecommendations() {
        String[] keywords = getKeywordsFromHadith();

        recommendedDua = recommendationHelper.getRecommendedDua(keywords);
        recommendedAyat = recommendationHelper.getRecommendedAyat(keywords);

        if (recommendedDua != null) {
            recommendedDuaText.setText(recommendedDua.getDuaTranslation());
            recommendedDuaCard.setVisibility(View.VISIBLE);
        } else {
            recommendedDuaCard.setVisibility(View.GONE);
        }

        if (recommendedAyat != null) {
            recommendedAyatText.setText(recommendedAyat.getEnglish());
            recommendedAyatCard.setVisibility(View.VISIBLE);
        } else {
            recommendedAyatCard.setVisibility(View.GONE);
        }
    }

    // ✅ NEW: Extract keywords from Hadith
    private String[] getKeywordsFromHadith() {
        List<String> keywords = new ArrayList<>();
        String content = (arabic + " " + english).toLowerCase();

        // Ramadan keywords
        if (content.contains("ramadan") || content.contains("رمضان") || content.contains("fasting") || content.contains("صوم")) {
            keywords.add("ramadan");
            keywords.add("fasting");
        }

        // Forgiveness keywords
        if (content.contains("forgive") || content.contains("غفر") || content.contains("mercy") || content.contains("رحم")) {
            keywords.add("forgiveness");
            keywords.add("mercy");
        }

        // Prayer keywords
        if (content.contains("prayer") || content.contains("salah") || content.contains("صلاة")) {
            keywords.add("prayer");
            keywords.add("salah");
        }

        // Laylatul Qadr
        if (content.contains("laylatul qadr") || content.contains("ليلة القدر")) {
            keywords.add("laylatul qadr");
            keywords.add("night");
        }

        // Paradise
        if (content.contains("paradise") || content.contains("jannah") || content.contains("جنة")) {
            keywords.add("paradise");
            keywords.add("jannah");
        }

        // Default
        if (keywords.isEmpty()) {
            keywords.add("ramadan");
        }

        return keywords.toArray(new String[0]);
    }

    private void setupClickListeners() {
        copyHadithButton.setOnClickListener(v -> copyHadith());
        shareHadithButton.setOnClickListener(v -> shareHadith());

        // ✅ NEW: Recommendation buttons
        viewDuaDetailsButton.setOnClickListener(v -> openDuaDetail());
        viewAyatDetailsButton.setOnClickListener(v -> openAyatDetail());
    }

    private void copyHadith() {
        String fullHadith = "📖 Hadith - Day " + hadithDay + "\n\n" +
                "Arabic:\n" + arabic + "\n\n" +
                "English:\n" + english + "\n\n" +
                "Reference: " + reference;

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Hadith", fullHadith);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Hadith copied to clipboard ✓", Toast.LENGTH_SHORT).show();
    }

    private void shareHadith() {
        String fullHadith = "📖 Ramadan Hadith - Day " + hadithDay + "\n\n" +
                "Arabic:\n" + arabic + "\n\n" +
                "English:\n" + english + "\n\n" +
                "Reference: " + reference + "\n\n" +
                "From Sirr-Ul-Quran App 🌙";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Ramadan Hadith - Day " + hadithDay);
        shareIntent.putExtra(Intent.EXTRA_TEXT, fullHadith);

        startActivity(Intent.createChooser(shareIntent, "Share Hadith via"));
    }

    // ✅ NEW: Open Dua detail
    private void openDuaDetail() {
        if (recommendedDua == null) return;

        Intent intent = new Intent(this, DuaDetailActivity.class);
        intent.putExtra("DAY_NUMBER", recommendedDua.getDayNumber());
        intent.putExtra("ASHRA_NUMBER", recommendedDua.getAshraNumber());
        intent.putExtra("DUA_ARABIC", recommendedDua.getDuaArabic());
        intent.putExtra("DUA_TRANSLITERATION", recommendedDua.getDuaTransliteration());
        intent.putExtra("DUA_TRANSLATION", recommendedDua.getDuaTranslation());
        startActivity(intent);
    }

    // ✅ NEW: Open Ayat detail
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
        recommendationHelper.getViewedManager().markHadithAsViewed(hadithDay);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecommendations();
    }
}