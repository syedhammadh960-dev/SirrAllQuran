package com.example.sirralquran.utils;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.sirralquran.models.DailyAyah;
import com.example.sirralquran.models.DailyHadith;
import com.example.sirralquran.models.RamadanDayContent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * FIXED FirebaseHelper - Uses getInstance() to get the SAME database instance initialized in SplashActivity
 */
public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private static FirebaseHelper instance;
    private DatabaseReference database;

    // CRITICAL: This MUST match SplashActivity and google-services.json (NO trailing slash!)
    private static final String FIREBASE_URL = "https://sirrallquran-default-rtdb.firebaseio.com";

    // Collection names in Firebase
    private static final String COLLECTION_AYAT = "ramadan_special_ayat";
    private static final String COLLECTION_HADITH = "ramadan_special_hadith";
    private static final String COLLECTION_DAY_CONTENT = "ramadan_day_content";
    private static final String COLLECTION_APP_CONFIG = "app_config";

    private FirebaseHelper() {
        try {
            // CRITICAL FIX: Use getInstance(URL) to get the SAME instance created in SplashActivity
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_URL);
            database = firebaseDatabase.getReference();

            Log.d(TAG, "✅ Firebase connected to: " + FIREBASE_URL);
        } catch (Exception e) {
            Log.e(TAG, "❌ FirebaseHelper init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    /**
     * Check if Ramadan is active from Firebase config
     */
    public void isRamadanActive(OnDataLoadListener<Boolean> listener) {
        String path = COLLECTION_APP_CONFIG + "/ramadan/is_ramadan_active";
        Log.d(TAG, "🔍 Checking Ramadan status: " + path);

        database.child(COLLECTION_APP_CONFIG)
                .child("ramadan")
                .child("is_ramadan_active")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "📡 Ramadan status callback FIRED!");
                        Log.d(TAG, "📊 Snapshot exists: " + snapshot.exists());
                        Log.d(TAG, "📊 Snapshot value: " + snapshot.getValue());

                        Boolean isActive = snapshot.getValue(Boolean.class);
                        Log.d(TAG, "✅ Ramadan active: " + (isActive != null && isActive));
                        listener.onSuccess(isActive != null && isActive);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "❌ Firebase error: " + error.getMessage());
                        Log.e(TAG, "❌ Error code: " + error.getCode());
                        Log.e(TAG, "❌ Error details: " + error.getDetails());
                        listener.onError(error.getMessage());
                    }
                });
    }

    /**
     * Get current Ramadan day (1-30) from Firebase
     */
    public void getCurrentRamadanDay(OnDataLoadListener<Integer> listener) {
        String path = COLLECTION_APP_CONFIG + "/ramadan/current_day";
        Log.d(TAG, "🔍 Fetching current day: " + path);

        database.child(COLLECTION_APP_CONFIG)
                .child("ramadan")
                .child("current_day")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "📡 Current day callback FIRED!");
                        Log.d(TAG, "📊 Snapshot exists: " + snapshot.exists());
                        Log.d(TAG, "📊 Snapshot value: " + snapshot.getValue());

                        Integer currentDay = snapshot.getValue(Integer.class);
                        Log.d(TAG, "✅ Current day: " + currentDay);
                        listener.onSuccess(currentDay != null ? currentDay : 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "❌ Firebase error: " + error.getMessage());
                        Log.e(TAG, "❌ Error code: " + error.getCode());
                        listener.onError(error.getMessage());
                    }
                });
    }

    /**
     * Fetch Ayah for specific Ramadan day
     */
    public void getAyahByDay(int dayNumber, OnDataLoadListener<DailyAyah> listener) {
        String dayKey = "day_" + dayNumber;
        String path = COLLECTION_AYAT + "/" + dayKey;
        Log.d(TAG, "🔍 Fetching Ayah: " + path);

        database.child(COLLECTION_AYAT)
                .child(dayKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "📡 Ayah callback FIRED!");
                        Log.d(TAG, "📊 Exists: " + snapshot.exists());

                        if (snapshot.exists()) {
                            try {
                                DailyAyah ayah = snapshot.getValue(DailyAyah.class);
                                if (ayah != null) {
                                    Log.d(TAG, "✅ Ayah loaded for day " + dayNumber);
                                    listener.onSuccess(ayah);
                                } else {
                                    Log.e(TAG, "❌ Ayah is null after parsing");
                                    listener.onError("Failed to parse Ayah data");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error parsing Ayah: " + e.getMessage());
                                e.printStackTrace();
                                listener.onError("Error parsing Ayah: " + e.getMessage());
                            }
                        } else {
                            Log.e(TAG, "❌ Ayah not found for day " + dayNumber);
                            listener.onError("Ayah not found for day " + dayNumber);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "❌ Firebase error: " + error.getMessage());
                        Log.e(TAG, "❌ Error code: " + error.getCode());
                        listener.onError(error.getMessage());
                    }
                });
    }

    /**
     * Fetch Hadith for specific Ramadan day
     */
    public void getHadithByDay(int dayNumber, OnDataLoadListener<DailyHadith> listener) {
        String dayKey = "day_" + dayNumber;
        String path = COLLECTION_HADITH + "/" + dayKey;
        Log.d(TAG, "🔍 Fetching Hadith: " + path);

        database.child(COLLECTION_HADITH)
                .child(dayKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "📡 Hadith callback FIRED!");
                        Log.d(TAG, "📊 Exists: " + snapshot.exists());

                        if (snapshot.exists()) {
                            try {
                                DailyHadith hadith = snapshot.getValue(DailyHadith.class);
                                if (hadith != null) {
                                    Log.d(TAG, "✅ Hadith loaded for day " + dayNumber);
                                    listener.onSuccess(hadith);
                                } else {
                                    Log.e(TAG, "❌ Hadith is null after parsing");
                                    listener.onError("Failed to parse Hadith data");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error parsing Hadith: " + e.getMessage());
                                e.printStackTrace();
                                listener.onError("Error parsing Hadith: " + e.getMessage());
                            }
                        } else {
                            Log.e(TAG, "❌ Hadith not found for day " + dayNumber);
                            listener.onError("Hadith not found for day " + dayNumber);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "❌ Firebase error: " + error.getMessage());
                        Log.e(TAG, "❌ Error code: " + error.getCode());
                        listener.onError(error.getMessage());
                    }
                });
    }

    /**
     * Fetch day content for specific Ramadan day
     */
    public void getDayContent(int dayNumber, OnDataLoadListener<RamadanDayContent> listener) {
        String dayKey = "day_" + dayNumber;
        String path = COLLECTION_DAY_CONTENT + "/" + dayKey;
        Log.d(TAG, "🔍 Fetching Day Content: " + path);

        database.child(COLLECTION_DAY_CONTENT)
                .child(dayKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "📡 Day Content callback FIRED!");
                        Log.d(TAG, "📊 Exists: " + snapshot.exists());

                        if (snapshot.exists()) {
                            try {
                                RamadanDayContent content = snapshot.getValue(RamadanDayContent.class);
                                if (content != null) {
                                    Log.d(TAG, "✅ Day content loaded for day " + dayNumber);
                                    listener.onSuccess(content);
                                } else {
                                    Log.e(TAG, "❌ Day content is null after parsing");
                                    listener.onError("Failed to parse day content");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error parsing day content: " + e.getMessage());
                                e.printStackTrace();
                                listener.onError("Error parsing content: " + e.getMessage());
                            }
                        } else {
                            Log.e(TAG, "❌ Content not found for day " + dayNumber);
                            listener.onError("Content not found for day " + dayNumber);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "❌ Firebase error: " + error.getMessage());
                        Log.e(TAG, "❌ Error code: " + error.getCode());
                        listener.onError(error.getMessage());
                    }
                });
    }

    /**
     * Generic data load listener interface
     */
    public interface OnDataLoadListener<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}