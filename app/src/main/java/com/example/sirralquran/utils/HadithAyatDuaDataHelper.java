package com.example.sirralquran.utils;

import android.content.Context;
import com.example.sirralquran.models.DailyHadith;
import com.example.sirralquran.models.DailyAyah;
import com.example.sirralquran.models.AshraDay;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ✅ UPDATED: Helper to load Hadith, Ayat, and Duas using existing models
 */
public class HadithAyatDuaDataHelper {

    private Context context;
    private List<DailyHadith> allHadith;
    private List<DailyAyah> allAyat;
    private List<AshraDay> allDuas;
    private ViewedContentManager viewedManager;

    public HadithAyatDuaDataHelper(Context context) {
        this.context = context;
        this.viewedManager = new ViewedContentManager(context);
        loadHadithData();
        loadAyatData();
        loadDuaData();
    }

    /**
     * Load hadith from hadees.json
     */
    private void loadHadithData() {
        allHadith = new ArrayList<>();

        try {
            InputStream is = context.getAssets().open("hadees.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(json);
            JSONArray hadithArray = root.getJSONArray("ramadan_hadith_30_days");

            for (int i = 0; i < hadithArray.length(); i++) {
                JSONObject obj = hadithArray.getJSONObject(i);

                DailyHadith hadith = new DailyHadith();
                hadith.setDayNumber(obj.getInt("day"));
                hadith.setArabic(obj.getString("arabic"));
                hadith.setEnglish(obj.getString("english"));
                hadith.setReference(obj.getString("reference"));

                allHadith.add(hadith);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load ayat from ayat.json
     */
    private void loadAyatData() {
        allAyat = new ArrayList<>();

        try {
            InputStream is = context.getAssets().open("ayat.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(json);
            JSONArray ayatArray = root.getJSONArray("ramadan_ayat_30_days");

            for (int i = 0; i < ayatArray.length(); i++) {
                JSONObject obj = ayatArray.getJSONObject(i);

                DailyAyah ayat = new DailyAyah();
                ayat.setDayNumber(obj.getInt("day"));
                ayat.setArabic(obj.getString("arabic"));
                ayat.setEnglish(obj.getString("english"));
                ayat.setReference(obj.getString("reference"));

                allAyat.add(ayat);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load duas from ashra.json
     */
    private void loadDuaData() {
        allDuas = new ArrayList<>();

        try {
            InputStream is = context.getAssets().open("ashra.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray duaArray = new JSONArray(json);

            for (int i = 0; i < duaArray.length(); i++) {
                JSONObject obj = duaArray.getJSONObject(i);

                int dayNumber = obj.getInt("day");
                String ashraName = obj.getString("ashra");

                JSONObject duaObj = obj.getJSONObject("dua");
                String duaArabic = duaObj.getString("arabic");
                String duaTransliteration = duaObj.getString("transliteration");
                String duaTranslation = duaObj.getString("translation");

                int ashraNumber = getAshraNumber(ashraName);

                AshraDay dua = new AshraDay(
                        dayNumber,
                        ashraNumber,
                        "Dua for Day " + dayNumber,
                        obj.getString("about_day"),
                        ashraName,
                        duaArabic,
                        duaTransliteration,
                        duaTranslation
                );

                allDuas.add(dua);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getAshraNumber(String ashraName) {
        if (ashraName.equalsIgnoreCase("Rehmah")) return 1;
        if (ashraName.equalsIgnoreCase("Maghfirah")) return 2;
        if (ashraName.equalsIgnoreCase("Naja'at")) return 3;
        return 1;
    }

    /**
     * Get recommended Hadith based on keywords
     */
    public DailyHadith getRecommendedHadith(String[] keywords) {
        List<DailyHadith> unviewedWithKeyword = new ArrayList<>();
        List<DailyHadith> viewedWithKeyword = new ArrayList<>();
        List<DailyHadith> unviewed = new ArrayList<>();

        for (DailyHadith hadith : allHadith) {
            boolean hasKeyword = containsKeyword(hadith.getArabic() + " " + hadith.getEnglish(), keywords);
            boolean isViewed = viewedManager.isHadithViewed(hadith.getDayNumber());

            if (hasKeyword && !isViewed) {
                unviewedWithKeyword.add(hadith);
            } else if (hasKeyword && isViewed) {
                viewedWithKeyword.add(hadith);
            } else if (!isViewed) {
                unviewed.add(hadith);
            }
        }

        if (!unviewedWithKeyword.isEmpty()) return unviewedWithKeyword.get(0);
        if (!viewedWithKeyword.isEmpty()) return viewedWithKeyword.get(0);
        if (!unviewed.isEmpty()) return unviewed.get(new Random().nextInt(unviewed.size()));

        return allHadith.isEmpty() ? null : allHadith.get(0);
    }

    /**
     * Get recommended Ayat based on keywords
     */
    public DailyAyah getRecommendedAyat(String[] keywords) {
        List<DailyAyah> unviewedWithKeyword = new ArrayList<>();
        List<DailyAyah> viewedWithKeyword = new ArrayList<>();
        List<DailyAyah> unviewed = new ArrayList<>();

        for (DailyAyah ayat : allAyat) {
            boolean hasKeyword = containsKeyword(ayat.getArabic() + " " + ayat.getEnglish(), keywords);
            boolean isViewed = viewedManager.isAyatViewed(ayat.getDayNumber());

            if (hasKeyword && !isViewed) {
                unviewedWithKeyword.add(ayat);
            } else if (hasKeyword && isViewed) {
                viewedWithKeyword.add(ayat);
            } else if (!isViewed) {
                unviewed.add(ayat);
            }
        }

        if (!unviewedWithKeyword.isEmpty()) return unviewedWithKeyword.get(0);
        if (!viewedWithKeyword.isEmpty()) return viewedWithKeyword.get(0);
        if (!unviewed.isEmpty()) return unviewed.get(new Random().nextInt(unviewed.size()));

        return allAyat.isEmpty() ? null : allAyat.get(0);
    }

    /**
     * ✅ NEW: Get recommended Dua based on keywords
     */
    public AshraDay getRecommendedDua(String[] keywords) {
        List<AshraDay> unviewedWithKeyword = new ArrayList<>();
        List<AshraDay> viewedWithKeyword = new ArrayList<>();
        List<AshraDay> unviewed = new ArrayList<>();

        for (AshraDay dua : allDuas) {
            String searchText = dua.getDuaArabic() + " " + dua.getDuaTransliteration() + " " + dua.getDuaTranslation();
            boolean hasKeyword = containsKeyword(searchText, keywords);
            boolean isViewed = viewedManager.isDuaViewed(dua.getDayNumber());

            if (hasKeyword && !isViewed) {
                unviewedWithKeyword.add(dua);
            } else if (hasKeyword && isViewed) {
                viewedWithKeyword.add(dua);
            } else if (!isViewed) {
                unviewed.add(dua);
            }
        }

        if (!unviewedWithKeyword.isEmpty()) return unviewedWithKeyword.get(0);
        if (!viewedWithKeyword.isEmpty()) return viewedWithKeyword.get(0);
        if (!unviewed.isEmpty()) return unviewed.get(new Random().nextInt(unviewed.size()));

        return allDuas.isEmpty() ? null : allDuas.get(0);
    }

    private boolean containsKeyword(String searchText, String[] keywords) {
        if (keywords == null || keywords.length == 0) return false;

        String lowerText = searchText.toLowerCase();
        for (String keyword : keywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public DailyHadith getHadithByDay(int day) {
        for (DailyHadith hadith : allHadith) {
            if (hadith.getDayNumber() == day) return hadith;
        }
        return null;
    }

    public DailyAyah getAyatByDay(int day) {
        for (DailyAyah ayat : allAyat) {
            if (ayat.getDayNumber() == day) return ayat;
        }
        return null;
    }

    public AshraDay getDuaByDay(int day) {
        for (AshraDay dua : allDuas) {
            if (dua.getDayNumber() == day) return dua;
        }
        return null;
    }

    public ViewedContentManager getViewedManager() {
        return viewedManager;
    }
}