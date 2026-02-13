package com.example.sirralquran.utils;

import android.content.Context;
import com.example.sirralquran.models.AshraDay;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to load Ashra data from JSON file
 */
public class AshraDataHelper {

    private Context context;
    private List<AshraDay> allDays;

    public AshraDataHelper(Context context) {
        this.context = context;
        loadData();
    }

    /**
     * Load ashra.json from assets
     */
    private void loadData() {
        allDays = new ArrayList<>();

        try {
            InputStream is = context.getAssets().open("ashra.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject dayObj = jsonArray.getJSONObject(i);

                int dayNumber = dayObj.getInt("day");
                String ashraName = dayObj.getString("ashra");

                // Dua object
                JSONObject duaObj = dayObj.getJSONObject("dua");
                String duaArabic = duaObj.getString("arabic");
                String duaTransliteration = duaObj.getString("transliteration");
                String duaTranslation = duaObj.getString("translation");

                // Other data
                String aboutDay = dayObj.getString("about_day");

                // Daily actions array
                JSONArray actionsArray = dayObj.getJSONArray("daily_actions");
                List<String> actions = new ArrayList<>();
                for (int j = 0; j < actionsArray.length(); j++) {
                    actions.add(actionsArray.getString(j));
                }

                // Hadith object
                JSONObject hadithObj = dayObj.getJSONObject("hadith");
                String hadithText = hadithObj.getString("text");
                String hadithReference = hadithObj.getString("reference");

                // Determine Ashra number
                int ashraNumber = getAshraNumber(ashraName);

                // Create AshraDay object
                AshraDay day = new AshraDay(
                        dayNumber,
                        ashraNumber,
                        getArabicTitle(dayNumber, ashraName),
                        aboutDay,
                        ashraName,
                        duaArabic,
                        duaTransliteration,
                        duaTranslation
                );

                // Set additional data
                day.setActions(actions);
                day.setHadithText(hadithText);
                day.setHadithReference(hadithReference);

                allDays.add(day);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get Ashra number from name
     */
    private int getAshraNumber(String ashraName) {
        if (ashraName.equalsIgnoreCase("Rehmah")) return 1;
        if (ashraName.equalsIgnoreCase("Maghfirah")) return 2;
        if (ashraName.equalsIgnoreCase("Naja'at")) return 3;
        return 1;
    }

    /**
     * Get Arabic title for each day
     */
    private String getArabicTitle(int dayNumber, String ashraName) {
        // Arabic titles based on day themes
        String[] rehmahTitles = {
                "بِدَايَةُ رَمَضَان", // Day 1 - Beginning of Ramadan
                "الشُّكْر", // Day 2 - Gratitude
                "الْحُبّ", // Day 3 - Love
                "الْهِدَايَة", // Day 4 - Guidance
                "التَّوْبَة", // Day 5 - Repentance
                "الْعِلْم", // Day 6 - Knowledge
                "الرَّحْمَة", // Day 7 - Mercy
                "تَطْهِيرُ الْقَلْب", // Day 8 - Purification of Heart
                "الثَّبَات", // Day 9 - Steadfastness
                "الصَّبْر" // Day 10 - Patience
        };

        String[] maghfirahTitles = {
                "الْمَغْفِرَة", // Day 11 - Forgiveness
                "التَّوْبَةُ النَّصُوح", // Day 12 - Sincere Repentance
                "الْعَفْو", // Day 13 - Pardon
                "التَّطْهِير", // Day 14 - Cleansing
                "نِصْفُ رَمَضَان", // Day 15 - Mid Ramadan
                "الِاسْتِغْفَار", // Day 16 - Seeking Forgiveness
                "إِصْلَاحُ الدِّين", // Day 17 - Rectifying Religion
                "الْحِسَابُ الْيَسِير", // Day 18 - Easy Reckoning
                "كَثْرَةُ التَّوْبَة", // Day 19 - Frequent Repentance
                "خَتْمُ الْخَيْر" // Day 20 - Ending with Goodness
        };

        String[] najaatTitles = {
                "لَيْلَةُ الْقَدْر", // Day 21 - Laylatul Qadr
                "الْجَنَّة", // Day 22 - Paradise
                "الْعَفْوُ وَالْمَغْفِرَة", // Day 23 - Pardon & Forgiveness
                "ثِقَلُ الْمِيزَان", // Day 24 - Heavy Scale
                "الْعِتْقُ مِنَ النَّار", // Day 25 - Freedom from Fire
                "الصَّالِحُون", // Day 26 - The Righteous
                "اللَّيْلَةُ الْعَظِيمَة", // Day 27 - The Great Night
                "تَقَبُّلُ الْأَعْمَال", // Day 28 - Acceptance of Deeds
                "التَّأَمُّلُ الْأَخِير", // Day 29 - Final Reflection
                "خِتَامُ رَمَضَان" // Day 30 - Completion of Ramadan
        };

        if (dayNumber >= 1 && dayNumber <= 10) {
            return rehmahTitles[dayNumber - 1];
        } else if (dayNumber >= 11 && dayNumber <= 20) {
            return maghfirahTitles[dayNumber - 11];
        } else if (dayNumber >= 21 && dayNumber <= 30) {
            return najaatTitles[dayNumber - 21];
        }

        return "يَوْم " + dayNumber; // Default: Day X
    }

    /**
     * Get all days for a specific Ashra
     */
    public List<AshraDay> getDaysForAshra(int ashraNumber) {
        List<AshraDay> days = new ArrayList<>();
        for (AshraDay day : allDays) {
            if (day.getAshraNumber() == ashraNumber) {
                days.add(day);
            }
        }
        return days;
    }

    /**
     * Get specific day data
     */
    public AshraDay getDayData(int dayNumber) {
        for (AshraDay day : allDays) {
            if (day.getDayNumber() == dayNumber) {
                return day;
            }
        }
        return null;
    }

    /**
     * Get all days
     */
    public List<AshraDay> getAllDays() {
        return allDays;
    }
}