package com.example.sirralquran.controllers;

import android.content.Context;
import android.util.Log;
import com.example.sirralquran.database.SalahDatabaseHelper;
import com.example.sirralquran.models.Prayer;
import com.example.sirralquran.utils.LocationHelper;
import com.example.sirralquran.utils.PrayerTimesHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * SMART PrayerController with location-aware caching
 * - Checks if location changed before making API calls
 * - Uses cached prayer times when location hasn't changed
 * - Only fetches from API when necessary
 */
public class PrayerController {

    private static final String TAG = "PrayerController";
    private Context context;
    private SalahDatabaseHelper dbHelper;
    private PrayerTimesHelper prayerTimesHelper;
    private LocationHelper locationHelper;

    public PrayerController(Context context) {
        this.context = context;
        this.dbHelper = SalahDatabaseHelper.getInstance(context);
        this.prayerTimesHelper = new PrayerTimesHelper(context);
        this.locationHelper = new LocationHelper(context);
    }

    /**
     * Get today's prayers (from database or API)
     */
    public List<Prayer> getTodaysPrayers() {
        // Try to get from database first
        List<Prayer> prayers = dbHelper.getTodayPrayers();

        if (prayers.isEmpty()) {
            // No data for today, return default times
            prayers = getDefaultPrayers();
            Log.d(TAG, "⚠️ No prayers in database, using defaults");
        } else {
            Log.d(TAG, "✅ Loaded " + prayers.size() + " prayers from database");
        }

        return prayers;
    }

    /**
     * Get default prayer times (fallback)
     */
    private List<Prayer> getDefaultPrayers() {
        List<Prayer> prayers = new ArrayList<>();
        prayers.add(new Prayer("Fajr", "الفجر", "05:15 AM"));
        prayers.add(new Prayer("Dhuhr", "الظهر", "12:30 PM"));
        prayers.add(new Prayer("Asr", "العصر", "04:15 PM"));
        prayers.add(new Prayer("Maghrib", "المغرب", "06:45 PM"));
        prayers.add(new Prayer("Isha", "العشاء", "08:15 PM"));
        return prayers;
    }

    /**
     * SMART: Fetch prayer times from API (with location change detection)
     */
    public void fetchPrayerTimesFromAPI(OnPrayerTimesLoadedListener listener, boolean forceRefresh) {
        // Check location permission
        if (!locationHelper.hasLocationPermission()) {
            Log.e(TAG, "❌ Location permission not granted");
            listener.onError("Location permission required");
            return;
        }

        // Get current location
        locationHelper.getCurrentLocation(new LocationHelper.OnLocationReceivedListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                Log.d(TAG, "📍 Location received: " + latitude + ", " + longitude);

                // Check if location changed significantly
                boolean locationChanged = locationHelper.hasLocationChangedSignificantly(latitude, longitude);

                if (!forceRefresh && !locationChanged) {
                    // Location hasn't changed, check if we have today's data
                    List<Prayer> cachedPrayers = dbHelper.getTodayPrayers();

                    if (!cachedPrayers.isEmpty()) {
                        Log.d(TAG, "✅ Using cached prayer times (location unchanged)");
                        listener.onSuccess(cachedPrayers);
                        return;
                    }
                }

                if (locationChanged) {
                    Log.d(TAG, "📍 Location changed significantly, fetching new prayer times");
                } else {
                    Log.d(TAG, "🔄 Force refresh requested");
                }

                // Fetch prayer times for this location
                prayerTimesHelper.fetchPrayerTimes(latitude, longitude,
                        new PrayerTimesHelper.OnPrayerTimesFetchedListener() {
                            @Override
                            public void onSuccess(List<Prayer> prayers) {
                                Log.d(TAG, "✅ Prayer times fetched from API");

                                // Update cached location
                                locationHelper.updateCachedLocation(latitude, longitude);

                                // Save to database
                                for (Prayer prayer : prayers) {
                                    dbHelper.savePrayer(prayer);
                                }

                                listener.onSuccess(prayers);
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "❌ Failed to fetch prayer times: " + error);
                                listener.onError(error);
                            }
                        });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to get location: " + error);

                // Use cached location
                double[] cachedLocation = locationHelper.getCachedLocation();
                Log.d(TAG, "📍 Using cached location: " + cachedLocation[0] + ", " + cachedLocation[1]);

                prayerTimesHelper.fetchPrayerTimes(cachedLocation[0], cachedLocation[1],
                        new PrayerTimesHelper.OnPrayerTimesFetchedListener() {
                            @Override
                            public void onSuccess(List<Prayer> prayers) {
                                for (Prayer prayer : prayers) {
                                    dbHelper.savePrayer(prayer);
                                }
                                listener.onSuccess(prayers);
                            }

                            @Override
                            public void onError(String error) {
                                listener.onError(error);
                            }
                        });
            }
        });
    }

    /**
     * Update prayer status (completed/qaza)
     */
    public void updatePrayerStatus(Prayer prayer) {
        dbHelper.savePrayer(prayer);
        Log.d(TAG, "✅ Prayer status updated: " + prayer.getName());
    }

    /**
     * Get completed prayers count (from database)
     */
    public int getCompletedPrayersCount() {
        return dbHelper.getCompletedPrayersToday();
    }

    /**
     * Get qaza prayers count (from database)
     */
    public int getQazaPrayersCount() {
        return dbHelper.getQazaPrayersToday();
    }

    /**
     * Delete all prayer data
     */
    public void deleteAllData() {
        dbHelper.deleteAllData();
        Log.d(TAG, "❌ All prayer data deleted");
    }

    /**
     * Delete old data (keep last X days)
     */
    public void deleteOldData(int daysToKeep) {
        dbHelper.deleteOldData(daysToKeep);
    }

    /**
     * Get location helper
     */
    public LocationHelper getLocationHelper() {
        return locationHelper;
    }

    /**
     * Callback interface
     */
    public interface OnPrayerTimesLoadedListener {
        void onSuccess(List<Prayer> prayers);
        void onError(String error);
    }
}