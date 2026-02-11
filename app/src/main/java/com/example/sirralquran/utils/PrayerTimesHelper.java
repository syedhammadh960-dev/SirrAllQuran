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
 * FIXED Prayer Times Helper with proper API parsing
 */
public class PrayerTimesHelper {

    private static final String TAG = "PrayerTimesHelper";
    private static final String API_BASE_URL = "https://api.aladhan.com/v1/timings";
    private static final String PREFS_NAME = "PrayerTimesPrefs";
    private static final String KEY_LAST_FETCH = "last_fetch_date";
    private static final String KEY_CACHED_TIMES = "cached_times";

    private Context context;
    private SharedPreferences prefs;

    public PrayerTimesHelper(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void fetchPrayerTimes(double latitude, double longitude, OnPrayerTimesFetchedListener listener) {
        String today = getTodayDate();
        String lastFetch = prefs.getString(KEY_LAST_FETCH, "");

        if (today.equals(lastFetch)) {
            String cachedTimes = prefs.getString(KEY_CACHED_TIMES, "");
            if (!cachedTimes.isEmpty()) {
                Log.d(TAG, "✅ Using cached prayer times");
                List<Prayer> prayers = parseCachedTimes(cachedTimes);
                if (!prayers.isEmpty()) {
                    listener.onSuccess(prayers);
                    return;
                }
            }
        }

        new Thread(() -> {
            try {
                long timestamp = System.currentTimeMillis() / 1000;
                String urlString = API_BASE_URL + "/" + timestamp +
                        "?latitude=" + latitude +
                        "&longitude=" + longitude +
                        "&method=1";  // Karachi method

                Log.d(TAG, "🌐 Fetching: " + urlString);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "📡 Response: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());

                    // Log full response for debugging
                    Log.d(TAG, "📄 Full API Response: " + jsonResponse.toString());

                    if (jsonResponse.getString("code").equals("200")) {
                        JSONObject data = jsonResponse.getJSONObject("data");
                        JSONObject timings = data.getJSONObject("timings");

                        // Extract and log each prayer time
                        String fajrRaw = timings.getString("Fajr");
                        String dhuhrRaw = timings.getString("Dhuhr");
                        String asrRaw = timings.getString("Asr");
                        String maghribRaw = timings.getString("Maghrib");
                        String ishaRaw = timings.getString("Isha");

                        Log.d(TAG, "🕌 RAW API Times:");
                        Log.d(TAG, "   Fajr RAW: " + fajrRaw);
                        Log.d(TAG, "   Dhuhr RAW: " + dhuhrRaw);
                        Log.d(TAG, "   Asr RAW: " + asrRaw);
                        Log.d(TAG, "   Maghrib RAW: " + maghribRaw);
                        Log.d(TAG, "   Isha RAW: " + ishaRaw);

                        // Format times
                        String fajr = formatTime(fajrRaw);
                        String dhuhr = formatTime(dhuhrRaw);
                        String asr = formatTime(asrRaw);
                        String maghrib = formatTime(maghribRaw);
                        String isha = formatTime(ishaRaw);

                        Log.d(TAG, "🕌 FORMATTED Times:");
                        Log.d(TAG, "   Fajr: " + fajr);
                        Log.d(TAG, "   Dhuhr: " + dhuhr);
                        Log.d(TAG, "   Asr: " + asr + " ← CHECK THIS");
                        Log.d(TAG, "   Maghrib: " + maghrib);
                        Log.d(TAG, "   Isha: " + isha);

                        List<Prayer> prayers = new ArrayList<>();
                        prayers.add(new Prayer("Fajr", "الفجر", fajr));
                        prayers.add(new Prayer("Dhuhr", "الظهر", dhuhr));
                        prayers.add(new Prayer("Asr", "العصر", asr));
                        prayers.add(new Prayer("Maghrib", "المغرب", maghrib));
                        prayers.add(new Prayer("Isha", "العشاء", isha));

                        cachePrayerTimes(prayers);

                        Log.d(TAG, "✅ Prayer times fetched successfully");
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
     * FIXED: Format time from API
     */
    private String formatTime(String apiTime) {
        try {
            // API returns: "HH:mm" or "HH:mm (PKT)"
            // Remove timezone if present
            String cleanTime = apiTime.split(" ")[0].trim();

            Log.d(TAG, "   Formatting: " + apiTime + " → " + cleanTime);

            // Parse 24-hour format
            String[] parts = cleanTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            // Convert to 12-hour format
            String ampm = hour >= 12 ? "PM" : "AM";
            hour = hour % 12;
            if (hour == 0) hour = 12;

            String formatted = String.format(Locale.ENGLISH, "%02d:%02d %s", hour, minute, ampm);
            Log.d(TAG, "   Result: " + formatted);

            return formatted;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error formatting time: " + apiTime + " - " + e.getMessage());
            return apiTime;
        }
    }

    private void cachePrayerTimes(List<Prayer> prayers) {
        StringBuilder sb = new StringBuilder();
        for (Prayer prayer : prayers) {
            sb.append(prayer.getName()).append("|")
                    .append(prayer.getNameArabic()).append("|")
                    .append(prayer.getTime()).append(",");
        }

        prefs.edit()
                .putString(KEY_LAST_FETCH, getTodayDate())
                .putString(KEY_CACHED_TIMES, sb.toString())
                .apply();

        Log.d(TAG, "💾 Cached prayer times");
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