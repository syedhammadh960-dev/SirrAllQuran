package com.example.sirralquran.controllers;

import android.content.Context;
import android.util.Log;
import com.example.sirralquran.database.SalahDatabaseHelper;
import com.example.sirralquran.dialogs.FiqhSelectionDialog;
import com.example.sirralquran.models.Prayer;
import com.example.sirralquran.utils.LocationHelper;
import com.example.sirralquran.utils.PrayerTimesHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * FIXED PrayerController - No variable access errors
 * BUG FIXES:
 * 1. Uses updatePrayerTimesOnly() instead of savePrayer() on refresh
 * 2. Uses updatePrayerStatus() for user status changes only
 * 3. Passes Fiqh method to database for caching
 * 4. Fixed forceRefresh variable access error
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
     * ✅ FIXED: Fetch prayer times with calculation method
     * Uses updatePrayerTimesOnly to PRESERVE user data
     * Fixed forceRefresh variable access error
     */
    public void fetchPrayerTimesFromAPI(OnPrayerTimesLoadedListener listener, boolean forceRefresh) {
        // Check location permission
        if (!locationHelper.hasLocationPermission()) {
            Log.e(TAG, "❌ Location permission not granted");
            listener.onError("Location permission required");
            return;
        }

        // Get saved calculation method
        final int calculationMethod = FiqhSelectionDialog.getSavedMethod(context);
        Log.d(TAG, "📊 Using calculation method: " + calculationMethod);

        // Get current location
        locationHelper.getCurrentLocation(new LocationHelper.OnLocationReceivedListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                Log.d(TAG, "📍 Location received: " + latitude + ", " + longitude);

                // Check if location changed significantly
                boolean locationChanged = locationHelper.hasLocationChangedSignificantly(latitude, longitude);

                // Check if Fiqh method changed
                int storedMethod = dbHelper.getStoredFiqhMethod();
                boolean methodChanged = (storedMethod != calculationMethod);

                // ✅ FIX: Use local variable instead of modifying parameter
                boolean shouldForceRefresh = forceRefresh;

                if (methodChanged) {
                    Log.d(TAG, "📖 Fiqh method changed: " + storedMethod + " → " + calculationMethod);
                    shouldForceRefresh = true;  // Force refresh if method changed
                }

                if (!shouldForceRefresh && !locationChanged && !methodChanged) {
                    // Location AND method unchanged, check if we have today's data
                    List<Prayer> cachedPrayers = dbHelper.getTodayPrayers();

                    if (!cachedPrayers.isEmpty()) {
                        Log.d(TAG, "✅ Using cached prayer times (no changes)");
                        listener.onSuccess(cachedPrayers);
                        return;
                    }
                }

                if (locationChanged) {
                    Log.d(TAG, "📍 Location changed significantly, fetching new prayer times");
                } else if (methodChanged) {
                    Log.d(TAG, "📖 Method changed, fetching new prayer times");
                } else {
                    Log.d(TAG, "🔄 Force refresh requested");
                }

                // Fetch prayer times with selected calculation method
                prayerTimesHelper.fetchPrayerTimes(latitude, longitude, calculationMethod,
                        new PrayerTimesHelper.OnPrayerTimesFetchedListener() {
                            @Override
                            public void onSuccess(List<Prayer> prayers) {
                                Log.d(TAG, "✅ Prayer times fetched from API");

                                // Update cached location
                                locationHelper.updateCachedLocation(latitude, longitude);

                                // ✅ CRITICAL FIX: Use updatePrayerTimesOnly to preserve user data
                                dbHelper.updatePrayerTimesOnly(prayers, calculationMethod);

                                Log.d(TAG, "✅ Prayer times updated (user data preserved)");

                                // Reload from database to get merged data
                                List<Prayer> mergedPrayers = dbHelper.getTodayPrayers();
                                listener.onSuccess(mergedPrayers);
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

                prayerTimesHelper.fetchPrayerTimes(cachedLocation[0], cachedLocation[1], calculationMethod,
                        new PrayerTimesHelper.OnPrayerTimesFetchedListener() {
                            @Override
                            public void onSuccess(List<Prayer> prayers) {
                                // ✅ CRITICAL FIX: Use updatePrayerTimesOnly
                                dbHelper.updatePrayerTimesOnly(prayers, calculationMethod);

                                // Reload merged data
                                List<Prayer> mergedPrayers = dbHelper.getTodayPrayers();
                                listener.onSuccess(mergedPrayers);
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
     * ✅ CRITICAL FIX: Update prayer status (completed/qaza/notification settings)
     * Uses updatePrayerStatus() to update ONLY user status, not prayer times
     */
    public void updatePrayerStatus(Prayer prayer) {
        dbHelper.updatePrayerStatus(prayer);  // ✅ FIX: Use updatePrayerStatus instead of savePrayer
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