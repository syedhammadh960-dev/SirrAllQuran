package com.example.sirralquran.models;

import com.google.firebase.database.PropertyName;

/**
 * Model for daily Hadith from Firebase
 * FIXED: Added @PropertyName annotations to match Firebase snake_case
 */
public class DailyHadith {
    private int dayNumber;
    private String arabic;
    private String english;
    private String transliteration;
    private String reference;
    private String book;
    private int hadithNumber;
    private String narrator;
    private String grade;
    private String theme;

    // Empty constructor required for Firebase
    public DailyHadith() {
    }

    public DailyHadith(int dayNumber, String arabic, String english, String reference, String narrator) {
        this.dayNumber = dayNumber;
        this.arabic = arabic;
        this.english = english;
        this.reference = reference;
        this.narrator = narrator;
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

    @PropertyName("book")
    public String getBook() { return book; }

    @PropertyName("hadith_number")
    public int getHadithNumber() { return hadithNumber; }

    @PropertyName("narrator")
    public String getNarrator() { return narrator; }

    @PropertyName("grade")
    public String getGrade() { return grade; }

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

    @PropertyName("book")
    public void setBook(String book) { this.book = book; }

    @PropertyName("hadith_number")
    public void setHadithNumber(int hadithNumber) { this.hadithNumber = hadithNumber; }

    @PropertyName("narrator")
    public void setNarrator(String narrator) { this.narrator = narrator; }

    @PropertyName("grade")
    public void setGrade(String grade) { this.grade = grade; }

    @PropertyName("theme")
    public void setTheme(String theme) { this.theme = theme; }
}