package com.example.sirralquran.models;

import java.util.List;

/**
 * Complete model class for Ashra Day with all data
 */
public class AshraDay {
    private int dayNumber;
    private int ashraNumber;
    private String title;
    private String description;
    private String theme;

    // Dua fields
    private String duaArabic;
    private String duaTransliteration;
    private String duaTranslation;

    // Additional fields
    private List<String> actions;
    private String hadithText;
    private String hadithReference;

    // Basic constructor
    public AshraDay(int dayNumber, int ashraNumber, String title, String description) {
        this.dayNumber = dayNumber;
        this.ashraNumber = ashraNumber;
        this.title = title;
        this.description = description;
    }

    // Full constructor
    public AshraDay(int dayNumber, int ashraNumber, String title, String description,
                    String theme, String duaArabic, String duaTransliteration, String duaTranslation) {
        this.dayNumber = dayNumber;
        this.ashraNumber = ashraNumber;
        this.title = title;
        this.description = description;
        this.theme = theme;
        this.duaArabic = duaArabic;
        this.duaTransliteration = duaTransliteration;
        this.duaTranslation = duaTranslation;
    }

    // Getters
    public int getDayNumber() { return dayNumber; }
    public int getAshraNumber() { return ashraNumber; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTheme() { return theme; }
    public String getDuaArabic() { return duaArabic; }
    public String getDuaTransliteration() { return duaTransliteration; }
    public String getDuaTranslation() { return duaTranslation; }
    public List<String> getActions() { return actions; }
    public String getHadithText() { return hadithText; }
    public String getHadithReference() { return hadithReference; }

    // Setters
    public void setTheme(String theme) { this.theme = theme; }
    public void setDuaArabic(String duaArabic) { this.duaArabic = duaArabic; }
    public void setDuaTransliteration(String duaTransliteration) { this.duaTransliteration = duaTransliteration; }
    public void setDuaTranslation(String duaTranslation) { this.duaTranslation = duaTranslation; }
    public void setActions(List<String> actions) { this.actions = actions; }
    public void setHadithText(String hadithText) { this.hadithText = hadithText; }
    public void setHadithReference(String hadithReference) { this.hadithReference = hadithReference; }
}