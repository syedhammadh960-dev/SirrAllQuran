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
 * ✅ Ayat Detail Activity with Dua + Hadith Recommendations
 */
public class AyatDetailActivity extends AppCompatActivity {

    private static final String TAG = "AyatDetail";

    // Intent extras
    private int ayatDay;
    private String arabic;
    private String english;
    private String reference;

    // Main views
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView ayatTitleText;
    private TextView ayatReferenceHeader;
    private TextView ayatArabicText;
    private TextView ayatEnglishText;
    private TextView ayatReferenceText;
    private MaterialButton copyAyatButton;
    private MaterialButton shareAyatButton;

    // ✅ NEW: Recommendation views
    private CardView recommendedDuaCard;
    private TextView recommendedDuaText;
    private MaterialButton viewDuaDetailsButton;

    private CardView recommendedHadithCard;
    private TextView recommendedHadithText;
    private MaterialButton viewHadithDetailsButton;

    // ✅ NEW: Recommendation system
    private HadithAyatDuaDataHelper recommendationHelper;
    private AshraDay recommendedDua;
    private DailyHadith recommendedHadith;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayat_detail);

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
        ayatDay = getIntent().getIntExtra("AYAT_DAY", 1);
        arabic = getIntent().getStringExtra("ARABIC");
        english = getIntent().getStringExtra("ENGLISH");
        reference = getIntent().getStringExtra("REFERENCE");
    }

    private void initializeViews() {
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        ayatTitleText = findViewById(R.id.ayatTitleText);
        ayatReferenceHeader = findViewById(R.id.ayatReferenceHeader);
        ayatArabicText = findViewById(R.id.ayatArabicText);
        ayatEnglishText = findViewById(R.id.ayatEnglishText);
        ayatReferenceText = findViewById(R.id.ayatReferenceText);
        copyAyatButton = findViewById(R.id.copyAyatButton);
        shareAyatButton = findViewById(R.id.shareAyatButton);

        // ✅ NEW: Recommendation views
        recommendedDuaCard = findViewById(R.id.recommendedDuaCard);
        recommendedDuaText = findViewById(R.id.recommendedDuaText);
        viewDuaDetailsButton = findViewById(R.id.viewDuaDetailsButton);

        recommendedHadithCard = findViewById(R.id.recommendedHadithCard);
        recommendedHadithText = findViewById(R.id.recommendedHadithText);
        viewHadithDetailsButton = findViewById(R.id.viewHadithDetailsButton);
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
        ayatTitleText.setText("Ayat - Day " + ayatDay);
        ayatReferenceHeader.setText(reference);
        ayatArabicText.setText(arabic);
        ayatEnglishText.setText(english);
        ayatReferenceText.setText(reference);
    }

    // ✅ NEW: Load recommendations
    private void loadRecommendations() {
        String[] keywords = getKeywordsFromAyat();

        recommendedDua = recommendationHelper.getRecommendedDua(keywords);
        recommendedHadith = recommendationHelper.getRecommendedHadith(keywords);

        if (recommendedDua != null) {
            recommendedDuaText.setText(recommendedDua.getDuaTranslation());
            recommendedDuaCard.setVisibility(View.VISIBLE);
        } else {
            recommendedDuaCard.setVisibility(View.GONE);
        }

        if (recommendedHadith != null) {
            recommendedHadithText.setText(recommendedHadith.getEnglish());
            recommendedHadithCard.setVisibility(View.VISIBLE);
        } else {
            recommendedHadithCard.setVisibility(View.GONE);
        }
    }

    // ✅ NEW: Extract keywords from Ayat
    private String[] getKeywordsFromAyat() {
        List<String> keywords = new ArrayList<>();
        String content = (arabic + " " + english).toLowerCase();

        // Ramadan keywords
        if (content.contains("ramadan") || content.contains("رمضان") || content.contains("fasting") || content.contains("صوم")) {
            keywords.add("ramadan");
            keywords.add("fasting");
        }

        // Belief keywords
        if (content.contains("believe") || content.contains("آمن") || content.contains("faith") || content.contains("إيمان")) {
            keywords.add("faith");
            keywords.add("belief");
        }

        // Prayer keywords
        if (content.contains("prayer") || content.contains("salah") || content.contains("صلاة") || content.contains("establish")) {
            keywords.add("prayer");
            keywords.add("salah");
        }

        // Quran keywords
        if (content.contains("quran") || content.contains("قرآن") || content.contains("revelation")) {
            keywords.add("quran");
            keywords.add("revelation");
        }

        // Taqwa keywords
        if (content.contains("taqwa") || content.contains("تقوى") || content.contains("righteous") || content.contains("piety")) {
            keywords.add("taqwa");
            keywords.add("righteousness");
        }

        // Paradise keywords
        if (content.contains("paradise") || content.contains("jannah") || content.contains("جنة") || content.contains("garden")) {
            keywords.add("paradise");
            keywords.add("jannah");
        }

        // Mercy keywords
        if (content.contains("mercy") || content.contains("رحم") || content.contains("merciful") || content.contains("compassion")) {
            keywords.add("mercy");
            keywords.add("compassion");
        }

        // Default
        if (keywords.isEmpty()) {
            keywords.add("ramadan");
        }

        return keywords.toArray(new String[0]);
    }

    private void setupClickListeners() {
        copyAyatButton.setOnClickListener(v -> copyAyat());
        shareAyatButton.setOnClickListener(v -> shareAyat());

        // ✅ NEW: Recommendation buttons
        viewDuaDetailsButton.setOnClickListener(v -> openDuaDetail());
        viewHadithDetailsButton.setOnClickListener(v -> openHadithDetail());
    }

    private void copyAyat() {
        String fullAyat = "📿 Ayat - Day " + ayatDay + "\n\n" +
                "Arabic:\n" + arabic + "\n\n" +
                "English:\n" + english + "\n\n" +
                "Reference: " + reference;

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Ayat", fullAyat);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Ayat copied to clipboard ✓", Toast.LENGTH_SHORT).show();
    }

    private void shareAyat() {
        String fullAyat = "📿 Quranic Ayat - Day " + ayatDay + "\n\n" +
                "Arabic:\n" + arabic + "\n\n" +
                "English:\n" + english + "\n\n" +
                "Reference: " + reference + "\n\n" +
                "From Sirr-Ul-Quran App 🌙";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Quranic Ayat - Day " + ayatDay);
        shareIntent.putExtra(Intent.EXTRA_TEXT, fullAyat);

        startActivity(Intent.createChooser(shareIntent, "Share Ayat via"));
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

    // ✅ NEW: Open Hadith detail
    private void openHadithDetail() {
        if (recommendedHadith == null) return;

        Intent intent = new Intent(this, HadithDetailActivity.class);
        intent.putExtra("HADITH_DAY", recommendedHadith.getDayNumber());
        intent.putExtra("ARABIC", recommendedHadith.getArabic());
        intent.putExtra("ENGLISH", recommendedHadith.getEnglish());
        intent.putExtra("REFERENCE", recommendedHadith.getReference());
        startActivity(intent);
    }

    private void markAsViewed() {
        recommendationHelper.getViewedManager().markAyatAsViewed(ayatDay);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecommendations();
    }
}