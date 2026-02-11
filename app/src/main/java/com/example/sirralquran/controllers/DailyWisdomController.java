package com.example.sirralquran.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.sirralquran.models.DailyAyah;
import com.example.sirralquran.models.DailyHadith;
import com.example.sirralquran.models.DailyWisdom;
import com.example.sirralquran.utils.FirebaseHelper;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Controller for fetching daily Ayah & Hadith
 * Logic:
 * - First load: Fetch from Firebase (Ramadan) or API (normal)
 * - Reload/refresh: Fetch from API
 */
public class DailyWisdomController {

    private static final String TAG = "DailyWisdomController";
    private static final String PREFS_NAME = "DailyWisdomPrefs";
    private static final String KEY_LAST_FETCH_TIME = "last_fetch_time";
    private static final String KEY_HAS_FETCHED_TODAY = "has_fetched_today";

    private Context context;
    private SharedPreferences prefs;
    private FirebaseHelper firebaseHelper;
    private RamadanManager ramadanManager;
    private OkHttpClient httpClient;

    // API endpoints
    private static final String AYAH_API = "https://qurani.ai/en/api/v1/ayah/random";
    private static final String HADITH_API = "https://api.hadith.gading.dev/books/bukhari?range=1-100";

    public DailyWisdomController(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.firebaseHelper = FirebaseHelper.getInstance();
        this.ramadanManager = new RamadanManager(context);
        this.httpClient = new OkHttpClient();
    }

    /**
     * Get today's wisdom
     * Logic: First time → Firebase, Reload → API
     */
    public void getTodaysWisdom(boolean isReload, OnWisdomLoadedListener listener) {
        boolean hasFetchedToday = prefs.getBoolean(KEY_HAS_FETCHED_TODAY, false);

        if (ramadanManager.isRamadan() && !isReload && !hasFetchedToday) {
            // RAMADAN + FIRST LOAD → Fetch from Firebase
            fetchFromFirebase(listener);
        } else {
            // RELOAD or NORMAL DAY → Fetch from API
            fetchFromAPI(listener);
        }
    }

    /**
     * Fetch from Firebase (Ramadan content)
     */
    private void fetchFromFirebase(OnWisdomLoadedListener listener) {
        int currentDay = ramadanManager.getCurrentRamadanDay();

        Log.d(TAG, "Fetching from Firebase for Ramadan day " + currentDay);

        // Fetch both Ayah and Hadith
        final DailyWisdom[] wisdom = {new DailyWisdom()};
        final int[] completed = {0};

        // Fetch Ayah
        firebaseHelper.getAyahByDay(currentDay, new FirebaseHelper.OnDataLoadListener<DailyAyah>() {
            @Override
            public void onSuccess(DailyAyah ayah) {
                wisdom[0].setAyah(ayah);
                wisdom[0].setSource("firebase");
                completed[0]++;

                if (completed[0] == 2) {
                    markFetchedToday();
                    listener.onSuccess(wisdom[0]);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to fetch Ayah: " + error);
                listener.onError("Failed to load Ayah");
            }
        });

        // Fetch Hadith
        firebaseHelper.getHadithByDay(currentDay, new FirebaseHelper.OnDataLoadListener<DailyHadith>() {
            @Override
            public void onSuccess(DailyHadith hadith) {
                wisdom[0].setHadith(hadith);
                wisdom[0].setSource("firebase");
                completed[0]++;

                if (completed[0] == 2) {
                    markFetchedToday();
                    listener.onSuccess(wisdom[0]);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to fetch Hadith: " + error);
                listener.onError("Failed to load Hadith");
            }
        });
    }

    /**
     * Fetch from API (Random Ayah + Hadith)
     */
    private void fetchFromAPI(OnWisdomLoadedListener listener) {
        Log.d(TAG, "Fetching from API (random content)");

        final DailyWisdom[] wisdom = {new DailyWisdom()};
        final int[] completed = {0};

        // Fetch random Ayah
        Request ayahRequest = new Request.Builder()
                .url(AYAH_API)
                .build();

        httpClient.newCall(ayahRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API call failed: " + e.getMessage());
                listener.onError("Failed to fetch Ayah from API");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        DailyAyah ayah = parseAyahFromAPI(json);
                        wisdom[0].setAyah(ayah);
                        wisdom[0].setSource("api");
                        completed[0]++;

                        if (completed[0] == 2) {
                            listener.onSuccess(wisdom[0]);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse Ayah: " + e.getMessage());
                        listener.onError("Failed to parse Ayah");
                    }
                }
            }
        });

        // Fetch random Hadith
        Request hadithRequest = new Request.Builder()
                .url(HADITH_API)
                .build();

        httpClient.newCall(hadithRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API call failed: " + e.getMessage());
                listener.onError("Failed to fetch Hadith from API");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        DailyHadith hadith = parseHadithFromAPI(json);
                        wisdom[0].setHadith(hadith);
                        wisdom[0].setSource("api");
                        completed[0]++;

                        if (completed[0] == 2) {
                            listener.onSuccess(wisdom[0]);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse Hadith: " + e.getMessage());
                        listener.onError("Failed to parse Hadith");
                    }
                }
            }
        });
    }

    /**
     * Parse Ayah from API JSON
     */
    private DailyAyah parseAyahFromAPI(String json) throws Exception {
        JSONObject obj = new JSONObject(json);

        DailyAyah ayah = new DailyAyah();
        ayah.setArabic(obj.optString("arabic", ""));
        ayah.setEnglish(obj.optString("english", ""));
        ayah.setReference(obj.optString("reference", ""));
        ayah.setSurahName(obj.optString("surah_name", ""));
        ayah.setSurahNumber(obj.optInt("surah_number", 0));

        return ayah;
    }

    /**
     * Parse Hadith from API JSON
     */
    private DailyHadith parseHadithFromAPI(String json) throws Exception {
        JSONObject obj = new JSONObject(json);

        DailyHadith hadith = new DailyHadith();
        hadith.setArabic(obj.optString("arabic", ""));
        hadith.setEnglish(obj.optString("english", ""));
        hadith.setReference(obj.optString("reference", "Sahih al-Bukhari"));
        hadith.setNarrator(obj.optString("narrator", "Companions (RA)"));

        return hadith;
    }

    /**
     * Mark that we've fetched content today
     */
    private void markFetchedToday() {
        prefs.edit()
                .putBoolean(KEY_HAS_FETCHED_TODAY, true)
                .putLong(KEY_LAST_FETCH_TIME, System.currentTimeMillis())
                .apply();
    }

    /**
     * Reset daily fetch status (call at midnight)
     */
    public void resetDailyFetch() {
        prefs.edit().putBoolean(KEY_HAS_FETCHED_TODAY, false).apply();
    }

    /**
     * Listener interface
     */
    public interface OnWisdomLoadedListener {
        void onSuccess(DailyWisdom wisdom);
        void onError(String error);
    }
}