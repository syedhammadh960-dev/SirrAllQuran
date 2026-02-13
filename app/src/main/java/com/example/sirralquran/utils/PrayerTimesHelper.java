package com.example.sirralquran.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.sirralquran.models.Prayer;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * FIXED PrayerTimesHelper with Hanafi school parameter
 * API Parameters:
 * - method: Calculation method (0-5)
 * - school: Asr calculation (0=Shafi/Standard, 1=Hanafi)
 */
public class PrayerTimesHelper {

    private static final String TAG = "PrayerTimesHelper";
    private static final String API_BASE_URL = "https://api.aladhan.com/v1/timings";
    private static final String PREFS_NAME = "PrayerTimesPrefs";
    private static final String KEY_LAST_FETCH = "last_fetch_date";
    private static final String KEY_CACHED_TIMES = "cached_times";
    private static final String KEY_CACHED_METHOD = "cached_fiqh_method";

    private Context context;
    private SharedPreferences prefs;

    public PrayerTimesHelper(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * ✅ FIXED: Fetch with proper school parameter for Hanafi
     */
    public void fetchPrayerTimes(double latitude, double longitude, int calculationMethod, OnPrayerTimesFetchedListener listener) {
        String today = getTodayDate();
        String lastFetch = prefs.getString(KEY_LAST_FETCH, "");
        int cachedMethod = prefs.getInt(KEY_CACHED_METHOD, -1);

        // Validate cache
        if (today.equals(lastFetch) && cachedMethod == calculationMethod) {
            String cachedTimes = prefs.getString(KEY_CACHED_TIMES, "");
            if (!cachedTimes.isEmpty()) {
                Log.d(TAG, "✅ Using cached prayer times");
                List<Prayer> prayers = parseCachedTimes(cachedTimes);
                if (!prayers.isEmpty()) {
                    listener.onSuccess(prayers);
                    return;
                }
            }
        } else if (cachedMethod != calculationMethod && cachedMethod != -1) {
            Log.d(TAG, "🔄 Fiqh method changed: " + getMethodName(cachedMethod) + " → " + getMethodName(calculationMethod));
        }

        // Fetch from API
        new Thread(() -> {
            try {
                long timestamp = System.currentTimeMillis() / 1000;

                // ✅ FIX: Determine school parameter
                // Method 1 (Karachi/Hanafi) needs school=1
                // Method 0 (Shia) uses method-specific calculation
                int school = (calculationMethod == 1) ? 1 : 0;

                String urlString = API_BASE_URL + "/" + timestamp +
                        "?latitude=" + latitude +
                        "&longitude=" + longitude +
                        "&method=" + calculationMethod +
                        "&school=" + school;  // ✅ CRITICAL FIX

                Log.d(TAG, "");
                Log.d(TAG, "========================================");
                Log.d(TAG, "🔍 DIAGNOSTIC API CALL");
                Log.d(TAG, "========================================");
                Log.d(TAG, "📍 Location:");
                Log.d(TAG, "   Latitude: " + latitude);
                Log.d(TAG, "   Longitude: " + longitude);
                Log.d(TAG, "📊 Calculation Method:");
                Log.d(TAG, "   Method: " + calculationMethod);
                Log.d(TAG, "   Name: " + getMethodName(calculationMethod));
                Log.d(TAG, "   Details: " + getMethodDetails(calculationMethod));
                Log.d(TAG, "   School: " + school + " (" + (school == 1 ? "HANAFI" : "STANDARD/SHAFI") + ")");
                Log.d(TAG, "🌐 Full API URL:");
                Log.d(TAG, "   " + urlString);
                Log.d(TAG, "⏰ Timestamp: " + timestamp);
                Log.d(TAG, "📅 Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date()));
                Log.d(TAG, "========================================");

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "📡 HTTP Response Code: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String fullResponse = response.toString();
                    Log.d(TAG, "");
                    Log.d(TAG, "========================================");
                    Log.d(TAG, "📦 FULL API RESPONSE:");
                    Log.d(TAG, "========================================");

                    if (fullResponse.length() > 2000) {
                        Log.d(TAG, fullResponse.substring(0, 2000) + "...");
                    } else {
                        Log.d(TAG, fullResponse);
                    }
                    Log.d(TAG, "========================================");

                    JSONObject jsonResponse = new JSONObject(fullResponse);

                    if (jsonResponse.getString("code").equals("200")) {
                        JSONObject data = jsonResponse.getJSONObject("data");
                        JSONObject timings = data.getJSONObject("timings");

                        JSONObject meta = data.optJSONObject("meta");
                        if (meta != null) {
                            Log.d(TAG, "");
                            Log.d(TAG, "========================================");
                            Log.d(TAG, "📊 API METADATA:");
                            Log.d(TAG, "========================================");
                            Log.d(TAG, "   Latitude: " + meta.optString("latitude", "N/A"));
                            Log.d(TAG, "   Longitude: " + meta.optString("longitude", "N/A"));
                            Log.d(TAG, "   Method: " + meta.optJSONObject("method").optString("name", "N/A"));
                            Log.d(TAG, "   School: " + meta.optString("school", "N/A"));  // ✅ Check school
                            Log.d(TAG, "   Timezone: " + meta.optString("timezone", "N/A"));
                            Log.d(TAG, "========================================");
                        }

                        // Extract raw times
                        String fajrRaw = timings.getString("Fajr");
                        String dhuhrRaw = timings.getString("Dhuhr");
                        String asrRaw = timings.getString("Asr");
                        String maghribRaw = timings.getString("Maghrib");
                        String ishaRaw = timings.getString("Isha");

                        Log.d(TAG, "");
                        Log.d(TAG, "========================================");
                        Log.d(TAG, "🕌 RAW API TIMES (Method " + calculationMethod + " - " + getMethodName(calculationMethod) + "):");
                        Log.d(TAG, "========================================");
                        Log.d(TAG, "   Fajr RAW:    " + fajrRaw);
                        Log.d(TAG, "   Dhuhr RAW:   " + dhuhrRaw);
                        Log.d(TAG, "   Asr RAW:     " + asrRaw + (school == 1 ? " (HANAFI)" : " (STANDARD)"));
                        Log.d(TAG, "   Maghrib RAW: " + maghribRaw);
                        Log.d(TAG, "   Isha RAW:    " + ishaRaw);
                        Log.d(TAG, "========================================");

                        // Format times
                        String fajr = formatTime(fajrRaw);
                        String dhuhr = formatTime(dhuhrRaw);
                        String asr = formatTime(asrRaw);
                        String maghrib = formatTime(maghribRaw);
                        String isha = formatTime(ishaRaw);

                        Log.d(TAG, "");
                        Log.d(TAG, "========================================");
                        Log.d(TAG, "🕌 FORMATTED TIMES (App Display):");
                        Log.d(TAG, "========================================");
                        Log.d(TAG, "   Fajr:    " + fajr);
                        Log.d(TAG, "   Dhuhr:   " + dhuhr);
                        Log.d(TAG, "   Asr:     " + asr + (school == 1 ? " (HANAFI)" : ""));
                        Log.d(TAG, "   Maghrib: " + maghrib);
                        Log.d(TAG, "   Isha:    " + isha);
                        Log.d(TAG, "========================================");

                        validateTimesForMethod(calculationMethod, school, fajr, dhuhr, asr, maghrib, isha);

                        List<Prayer> prayers = new ArrayList<>();
                        prayers.add(new Prayer("Fajr", "الفجر", fajr));
                        prayers.add(new Prayer("Dhuhr", "الظهر", dhuhr));
                        prayers.add(new Prayer("Asr", "العصر", asr));
                        prayers.add(new Prayer("Maghrib", "المغرب", maghrib));
                        prayers.add(new Prayer("Isha", "العشاء", isha));

                        cachePrayerTimes(prayers, calculationMethod);

                        Log.d(TAG, "✅ Prayer times fetched successfully for: " + getMethodName(calculationMethod));
                        Log.d(TAG, "");
                        listener.onSuccess(prayers);
                    } else {
                        Log.e(TAG, "❌ API error: " + jsonResponse.getString("status"));
                        listener.onError("Failed to fetch");
                    }
                } else {
                    Log.e(TAG, "❌ HTTP error: " + responseCode);
                    listener.onError("Server error: " + responseCode);
                }

                conn.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "❌ Error fetching: " + e.getMessage());
                e.printStackTrace();
                listener.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Validate if times make sense for the method and school
     */
    private void validateTimesForMethod(int method, int school, String fajr, String dhuhr, String asr, String maghrib, String isha) {
        Log.d(TAG, "");
        Log.d(TAG, "========================================");
        Log.d(TAG, "🔍 VALIDATION CHECKS:");
        Log.d(TAG, "========================================");

        switch (method) {
            case 0: // Shia
                Log.d(TAG, "📖 Shia Method Expectations:");
                Log.d(TAG, "   ✓ Fajr: Earlier dawn (16° angle)");
                Log.d(TAG, "   ✓ Maghrib: Should be LATER than Sunni (10-15 min)");
                Log.d(TAG, "   ✓ Isha: Should be close to Maghrib (14° angle)");
                break;

            case 1: // Karachi (Hanafi)
                Log.d(TAG, "📖 Hanafi Method Expectations:");
                Log.d(TAG, "   ✓ Fajr: Standard dawn (18° angle)");
                Log.d(TAG, "   ✓ Asr: Should be LATER than Shafi (school=" + school + ")");
                if (school == 1) {
                    Log.d(TAG, "   ✅ HANAFI School Applied: Asr uses 2x shadow");
                } else {
                    Log.w(TAG, "   ⚠️ WARNING: School=0 (Standard), NOT Hanafi!");
                }
                Log.d(TAG, "   ✓ Isha: Standard (18° angle)");
                break;
        }

        Log.d(TAG, "========================================");
    }

    private String getMethodName(int method) {
        switch (method) {
            case 0: return "Shia Ithna-Ashari";
            case 1: return "Karachi (Hanafi)";
            case 2: return "ISNA";
            case 3: return "Muslim World League";
            case 4: return "Umm Al-Qura";
            case 5: return "Egyptian";
            default: return "Unknown";
        }
    }

    private String getMethodDetails(int method) {
        switch (method) {
            case 0: return "Fajr 16°, Isha 14°";
            case 1: return "Fajr 18°, Isha 18° (Hanafi Asr)";
            case 2: return "Fajr 15°, Isha 15°";
            case 3: return "Fajr 18°, Isha 17°";
            case 4: return "Fajr 18.5°, Isha 90min after Maghrib";
            case 5: return "Fajr 19.5°, Isha 17.5°";
            default: return "Unknown";
        }
    }

    private String formatTime(String apiTime) {
        try {
            String cleanTime = apiTime.split(" ")[0].trim();
            String[] parts = cleanTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            String ampm = hour >= 12 ? "PM" : "AM";
            hour = hour % 12;
            if (hour == 0) hour = 12;

            return String.format(Locale.ENGLISH, "%02d:%02d %s", hour, minute, ampm);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error formatting time: " + apiTime + " - " + e.getMessage());
            return apiTime;
        }
    }

    private void cachePrayerTimes(List<Prayer> prayers, int calculationMethod) {
        StringBuilder sb = new StringBuilder();
        for (Prayer prayer : prayers) {
            sb.append(prayer.getName()).append("|")
                    .append(prayer.getNameArabic()).append("|")
                    .append(prayer.getTime()).append(",");
        }

        prefs.edit()
                .putString(KEY_LAST_FETCH, getTodayDate())
                .putString(KEY_CACHED_TIMES, sb.toString())
                .putInt(KEY_CACHED_METHOD, calculationMethod)
                .apply();

        Log.d(TAG, "💾 Cached prayer times with method: " + calculationMethod);
    }

    private List<Prayer> parseCachedTimes(String cached) {
        List<Prayer> prayers = new ArrayList<>();
        try {
            String[] entries = cached.split(",");
            for (String entry : entries) {
                if (entry.trim().isEmpty()) continue;
                String[] parts = entry.split("\\|");
                if (parts.length == 3) {
                    prayers.add(new Prayer(parts[0], parts[1], parts[2]));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing cached times: " + e.getMessage());
        }
        return prayers;
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return sdf.format(new Date());
    }

    public interface OnPrayerTimesFetchedListener {
        void onSuccess(List<Prayer> prayers);
        void onError(String error);
    }
}