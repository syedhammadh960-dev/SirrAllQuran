package com.example.sirralquran.models;

import com.google.firebase.database.PropertyName;

/**
 * Model for daily Ayah from Firebase
 * FIXED: Added @PropertyName annotations to match Firebase snake_case
 */
public class DailyAyah {
    private int dayNumber;
    private String arabic;
    private String english;
    private String transliteration;
    private String reference;
    private String surahName;
    private int surahNumber;
    private int ayahNumber;
    private int juz;
    private String theme;

    // Empty constructor required for Firebase
    public DailyAyah() {
    }

    public DailyAyah(int dayNumber, String arabic, String english, String reference, String surahName) {
        this.dayNumber = dayNumber;
        this.arabic = arabic;
        this.english = english;
        this.reference = reference;
        this.surahName = surahName;
    }

    // Getters with @PropertyName annotations
    @PropertyName("day_number")
    public int getDayNumber() { return dayNumber; }

    @PropertyName("arabic")
    public String getArabic() { return arabic; }

    @PropertyName("english")
    public String getEnglish() { return english; }

    @PropertyName("transliteration")
    public String getTransliteration() { return transliteration; }

    @PropertyName("reference")
    public String getReference() { return reference; }

    @PropertyName("surah_name")
    public String getSurahName() { return surahName; }

    @PropertyName("surah_number")
    public int getSurahNumber() { return surahNumber; }

    @PropertyName("ayah_number")
    public int getAyahNumber() { return ayahNumber; }

    @PropertyName("juz")
    public int getJuz() { return juz; }

    @PropertyName("theme")
    public String getTheme() { return theme; }

    // Setters with @PropertyName annotations
    @PropertyName("day_number")
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    @PropertyName("arabic")
    public void setArabic(String arabic) { this.arabic = arabic; }

    @PropertyName("english")
    public void setEnglish(String english) { this.english = english; }

    @PropertyName("transliteration")
    public void setTransliteration(String transliteration) { this.transliteration = transliteration; }

    @PropertyName("reference")
    public void setReference(String reference) { this.reference = reference; }

    @PropertyName("surah_name")
    public void setSurahName(String surahName) { this.surahName = surahName; }

    @PropertyName("surah_number")
    public void setSurahNumber(int surahNumber) { this.surahNumber = surahNumber; }

    @PropertyName("ayah_number")
    public void setAyahNumber(int ayahNumber) { this.ayahNumber = ayahNumber; }

    @PropertyName("juz")
    public void setJuz(int juz) { this.juz = juz; }

    @PropertyName("theme")
    public void setTheme(String theme) { this.theme = theme; }
}