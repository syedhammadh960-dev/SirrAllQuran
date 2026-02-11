package com.example.sirralquran.models;

import com.google.firebase.database.PropertyName;
import java.util.List;

/**
 * Model for Ramadan day content from Firebase
 * FIXED: Added @PropertyName annotations to match Firebase snake_case
 */
public class RamadanDayContent {
    private int dayNumber;
    private int juz;
    private String surahRange;
    private String coreTheme;
    private String explanation;
    private List<String> keyTakeaways;
    private String reflectionQuestion;
    private String videoUrl;
    private String audioUrl;
    private String relatedAyah;
    private String relatedHadith;
    private String scholar;
    private int durationMinutes;
    private String difficultyLevel;

    // Empty constructor required for Firebase
    public RamadanDayContent() {
    }

    // Getters with @PropertyName annotations
    @PropertyName("day_number")
    public int getDayNumber() { return dayNumber; }

    @PropertyName("juz")
    public int getJuz() { return juz; }

    @PropertyName("surah_range")
    public String getSurahRange() { return surahRange; }

    @PropertyName("core_theme")
    public String getCoreTheme() { return coreTheme; }

    @PropertyName("explanation")
    public String getExplanation() { return explanation; }

    @PropertyName("key_takeaways")
    public List<String> getKeyTakeaways() { return keyTakeaways; }

    @PropertyName("reflection_question")
    public String getReflectionQuestion() { return reflectionQuestion; }

    @PropertyName("video_url")
    public String getVideoUrl() { return videoUrl; }

    @PropertyName("audio_url")
    public String getAudioUrl() { return audioUrl; }

    @PropertyName("related_ayah")
    public String getRelatedAyah() { return relatedAyah; }

    @PropertyName("related_hadith")
    public String getRelatedHadith() { return relatedHadith; }

    @PropertyName("scholar")
    public String getScholar() { return scholar; }

    @PropertyName("duration_minutes")
    public int getDurationMinutes() { return durationMinutes; }

    @PropertyName("difficulty_level")
    public String getDifficultyLevel() { return difficultyLevel; }

    // Setters with @PropertyName annotations
    @PropertyName("day_number")
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    @PropertyName("juz")
    public void setJuz(int juz) { this.juz = juz; }

    @PropertyName("surah_range")
    public void setSurahRange(String surahRange) { this.surahRange = surahRange; }

    @PropertyName("core_theme")
    public void setCoreTheme(String coreTheme) { this.coreTheme = coreTheme; }

    @PropertyName("explanation")
    public void setExplanation(String explanation) { this.explanation = explanation; }

    @PropertyName("key_takeaways")
    public void setKeyTakeaways(List<String> keyTakeaways) { this.keyTakeaways = keyTakeaways; }

    @PropertyName("reflection_question")
    public void setReflectionQuestion(String reflectionQuestion) { this.reflectionQuestion = reflectionQuestion; }

    @PropertyName("video_url")
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    @PropertyName("audio_url")
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    @PropertyName("related_ayah")
    public void setRelatedAyah(String relatedAyah) { this.relatedAyah = relatedAyah; }

    @PropertyName("related_hadith")
    public void setRelatedHadith(String relatedHadith) { this.relatedHadith = relatedHadith; }

    @PropertyName("scholar")
    public void setScholar(String scholar) { this.scholar = scholar; }

    @PropertyName("duration_minutes")
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    @PropertyName("difficulty_level")
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
}