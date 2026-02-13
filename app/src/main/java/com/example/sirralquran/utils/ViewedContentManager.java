package com.example.sirralquran.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

/**
 * ✅ UPDATED: Manager to track viewed Hadith, Ayat, AND Duas
 */
public class ViewedContentManager {

    private static final String PREFS_NAME = "ViewedContent";
    private static final String KEY_VIEWED_HADITH = "viewed_hadith_";
    private static final String KEY_VIEWED_AYAT = "viewed_ayat_";
    private static final String KEY_VIEWED_DUA = "viewed_dua_"; // NEW

    private SharedPreferences prefs;
    private Context context;

    public ViewedContentManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ============ HADITH TRACKING ============

    public void markHadithAsViewed(int hadithDay) {
        Set<String> viewedHadith = getViewedHadith();
        viewedHadith.add(String.valueOf(hadithDay));
        prefs.edit().putStringSet(KEY_VIEWED_HADITH, viewedHadith).apply();
    }

    public boolean isHadithViewed(int hadithDay) {
        Set<String> viewedHadith = getViewedHadith();
        return viewedHadith.contains(String.valueOf(hadithDay));
    }

    public Set<String> getViewedHadith() {
        return new HashSet<>(prefs.getStringSet(KEY_VIEWED_HADITH, new HashSet<>()));
    }

    public int getViewedHadithCount() {
        return getViewedHadith().size();
    }

    // ============ AYAT TRACKING ============

    public void markAyatAsViewed(int ayatDay) {
        Set<String> viewedAyat = getViewedAyat();
        viewedAyat.add(String.valueOf(ayatDay));
        prefs.edit().putStringSet(KEY_VIEWED_AYAT, viewedAyat).apply();
    }

    public boolean isAyatViewed(int ayatDay) {
        Set<String> viewedAyat = getViewedAyat();
        return viewedAyat.contains(String.valueOf(ayatDay));
    }

    public Set<String> getViewedAyat() {
        return new HashSet<>(prefs.getStringSet(KEY_VIEWED_AYAT, new HashSet<>()));
    }

    public int getViewedAyatCount() {
        return getViewedAyat().size();
    }

    // ============ DUA TRACKING (NEW) ============

    public void markDuaAsViewed(int duaDay) {
        Set<String> viewedDua = getViewedDua();
        viewedDua.add(String.valueOf(duaDay));
        prefs.edit().putStringSet(KEY_VIEWED_DUA, viewedDua).apply();
    }

    public boolean isDuaViewed(int duaDay) {
        Set<String> viewedDua = getViewedDua();
        return viewedDua.contains(String.valueOf(duaDay));
    }

    public Set<String> getViewedDua() {
        return new HashSet<>(prefs.getStringSet(KEY_VIEWED_DUA, new HashSet<>()));
    }

    public int getViewedDuaCount() {
        return getViewedDua().size();
    }

    // ============ RESET & STATS ============

    public void resetAllViewed() {
        prefs.edit()
                .remove(KEY_VIEWED_HADITH)
                .remove(KEY_VIEWED_AYAT)
                .remove(KEY_VIEWED_DUA)
                .apply();
    }

    public int getTotalViewedCount() {
        return getViewedHadithCount() + getViewedAyatCount() + getViewedDuaCount();
    }

    public float getCompletionPercentage() {
        int total = 30 + 30 + 30; // 30 Hadith + 30 Ayat + 30 Duas
        int viewed = getTotalViewedCount();
        return (viewed * 100f) / total;
    }
}