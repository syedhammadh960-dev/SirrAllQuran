package com.example.sirralquran.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * ✅ FIXED HijriDateHelper - Correct API endpoint
 * API Changed: /v1/gToH/{timestamp} → /v1/gToHCalendar/{month}/{year}
 */
public class HijriDateHelper {
    private static final String TAG = "HijriDateHelper";
    private static final String PREFS_NAME = "HijriDatePrefs";
    private static final String KEY_HIJRI_DATE = "hijri_date_";
    private static final String KEY_HIJRI_MONTH = "hijri_month_";
    private static final String KEY_HIJRI_YEAR = "hijri_year_";

    private final Context context;
    private final SharedPreferences prefs;

    public HijriDateHelper(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get cached Hijri date (returns immediately)
     */
    public String getCachedHijriDate() {
        String today = getTodayDate();
        String cachedDate = prefs.getString(KEY_HIJRI_DATE + today, null);

        if (cachedDate != null) {
            return cachedDate;
        }

        // Default fallback
        return "15 Ramadan 1446";
    }

    /**
     * ✅ FIXED: Fetch Hijri date from API (correct endpoint)
     * OLD: https://api.aladhan.com/v1/gToH/{timestamp} ❌ (404 error)
     * NEW: https://api.aladhan.com/v1/gToHCalendar/{month}/{year} ✅
     */
    public void fetchHijriDate(final HijriDateCallback callback) {
        new Thread(() -> {
            try {
                String today = getTodayDate();

                // Check cache first
                String cached = prefs.getString(KEY_HIJRI_DATE + today, null);
                if (cached != null) {
                    Log.d(TAG, "✅ Using cached Hijri date: " + cached);
                    callback.onSuccess(cached);
                    return;
                }

                // Get current month and year
                Calendar cal = Calendar.getInstance();
                int month = cal.get(Calendar.MONTH) + 1;  // 1-12
                int year = cal.get(Calendar.YEAR);
                int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

                // ✅ FIXED: Use gToHCalendar endpoint
                String apiUrl = "https://api.aladhan.com/v1/gToHCalendar/" + month + "/" + year;

                Log.d(TAG, "🌐 Fetching Hijri date from: " + apiUrl);
                Log.d(TAG, "📅 Today: " + month + "/" + dayOfMonth + "/" + year);

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "📡 API Response Code: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse JSON
                    JSONObject json = new JSONObject(response.toString());

                    if (!json.getString("code").equals("200")) {
                        callback.onError("API returned error");
                        return;
                    }

                    JSONArray dataArray = json.getJSONArray("data");

                    if (dayOfMonth > dataArray.length()) {
                        Log.e(TAG, "❌ Day out of range: " + dayOfMonth + " > " + dataArray.length());
                        callback.onError("Invalid date");
                        return;
                    }

                    // Find today's date in the array (1-indexed)
                    JSONObject todayData = dataArray.getJSONObject(dayOfMonth - 1);
                    JSONObject gregorian = todayData.getJSONObject("gregorian");
                    JSONObject hijri = todayData.getJSONObject("hijri");

                    String day = hijri.getString("day");
                    String monthEn = hijri.getJSONObject("month").getString("en");
                    String hijriYear = hijri.getString("year");

                    String hijriDate = day + " " + monthEn + " " + hijriYear;

                    // Cache the result
                    prefs.edit()
                            .putString(KEY_HIJRI_DATE + today, hijriDate)
                            .putString(KEY_HIJRI_MONTH + today, monthEn)
                            .putString(KEY_HIJRI_YEAR + today, hijriYear)
                            .apply();

                    Log.d(TAG, "✅ Hijri date fetched: " + hijriDate);
                    Log.d(TAG, "📊 Gregorian: " + gregorian.getString("date"));

                    callback.onSuccess(hijriDate);
                } else {
                    Log.e(TAG, "❌ API error: " + responseCode);
                    callback.onError("Failed to fetch Hijri date (Code: " + responseCode + ")");
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "❌ Error fetching Hijri date: " + e.getMessage());
                e.printStackTrace();
                callback.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * Get today's date in YYYY-MM-DD format
     */
    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return sdf.format(new Date());
    }

    /**
     * Callback for async Hijri date fetching
     */
    public interface HijriDateCallback {
        void onSuccess(String hijriDate);
        void onError(String error);
    }
}