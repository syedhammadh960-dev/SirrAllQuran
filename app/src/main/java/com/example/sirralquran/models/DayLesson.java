package com.example.sirralquran.models;

public class DayLesson {
    private int dayNumber;
    private String title;
    private String description;
    private String theme;
    private int surahNumber;
    private String surahInfo;
    private String tafseer;
    private String scholarQuote;
    private String videoUrl;
    private boolean completed;
    private boolean locked;

    // Full constructor
    public DayLesson(int dayNumber, String title, String description, String theme,
                     int surahNumber, String surahInfo, String tafseer, String scholarQuote,
                     String videoUrl, boolean completed, boolean locked) {
        this.dayNumber = dayNumber;
        this.title = title;
        this.description = description;
        this.theme = theme;
        this.surahNumber = surahNumber;
        this.surahInfo = surahInfo;
        this.tafseer = tafseer;
        this.scholarQuote = scholarQuote;
        this.videoUrl = videoUrl;
        this.completed = completed;
        this.locked = locked;
    }

    // Basic constructor
    public DayLesson(int dayNumber, String title, String description, String theme) {
        this.dayNumber = dayNumber;
        this.title = title;
        this.description = description;
        this.theme = theme;
        this.surahNumber = 0;
        this.surahInfo = "";
        this.tafseer = "";
        this.scholarQuote = "";
        this.videoUrl = "";
        this.completed = false;
        this.locked = true;
    }

    // Getters
    public int getDayNumber() {
        return dayNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTheme() {
        return theme;
    }

    public int getSurahNumber() {
        return surahNumber;
    }

    public String getSurahInfo() {
        return surahInfo;
    }

    public String getTafseer() {
        return tafseer;
    }

    public String getScholarQuote() {
        return scholarQuote;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isLocked() {
        return locked;
    }

    // Setters
    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setSurahNumber(int surahNumber) {
        this.surahNumber = surahNumber;
    }

    public void setSurahInfo(String surahInfo) {
        this.surahInfo = surahInfo;
    }

    public void setTafseer(String tafseer) {
        this.tafseer = tafseer;
    }

    public void setScholarQuote(String scholarQuote) {
        this.scholarQuote = scholarQuote;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public String toString() {
        return "DayLesson{" +
                "dayNumber=" + dayNumber +
                ", title='" + title + '\'' +
                ", theme='" + theme + '\'' +
                ", completed=" + completed +
                ", locked=" + locked +
                '}';
    }
}