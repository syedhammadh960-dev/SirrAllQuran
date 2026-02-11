package com.example.sirralquran.models;

/**
 * Wrapper model for daily Ayah + Hadith
 * Used to combine both in a single response
 */
public class DailyWisdom {
    private DailyAyah ayah;
    private DailyHadith hadith;
    private String date;
    private boolean fromCache;
    private String source; // "firebase" or "api"

    public DailyWisdom() {
    }

    public DailyWisdom(DailyAyah ayah, DailyHadith hadith) {
        this.ayah = ayah;
        this.hadith = hadith;
    }

    // Getters & Setters
    public DailyAyah getAyah() {
        return ayah;
    }

    public void setAyah(DailyAyah ayah) {
        this.ayah = ayah;
    }

    public DailyHadith getHadith() {
        return hadith;
    }

    public void setHadith(DailyHadith hadith) {
        this.hadith = hadith;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}