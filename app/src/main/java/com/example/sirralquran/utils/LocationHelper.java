package com.example.sirralquran.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * COMPLETE Generic LocationHelper
 * - Handles BOTH precise (FINE) and approximate (COARSE) permissions
 * - Works on emulator and real device
 * - Generic (no hardcoded locations)
 * - Smart fallback system
 */
public class LocationHelper {

    private static final String TAG = "LocationHelper";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private static final String PREFS_NAME = "LocationPrefs";
    private static final String KEY_LAST_LATITUDE = "last_latitude";
    private static final String KEY_LAST_LONGITUDE = "last_longitude";
    private static final String KEY_LAST_CITY = "last_city";

    // Default to Lahore (fallback if no location available)
    private static final double DEFAULT_LATITUDE = 31.5204;
    private static final double DEFAULT_LONGITUDE = 74.3587;
    private static final String DEFAULT_CITY = "Lahore, Pakistan";

    // Distance threshold (500m = significant change)
    private static final float SIGNIFICANT_DISTANCE_METERS = 500.0f;

    private Context context;
    private LocationManager locationManager;
    private SharedPreferences prefs;
    private OnLocationReceivedListener listener;
    private boolean isRequestingLocation = false;

    public LocationHelper(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if ANY location permission is granted (FINE or COARSE)
     */
    public boolean hasLocationPermission() {
        boolean hasFine = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean hasCoarse = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        Log.d(TAG, "🔐 Permission Status:");
        Log.d(TAG, "   FINE (Precise): " + hasFine);
        Log.d(TAG, "   COARSE (Approximate): " + hasCoarse);

        return hasFine || hasCoarse;
    }

    /**
     * Check if precise location is granted
     */
    public boolean hasPreciseLocation() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request location permissions
     */
    public void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST);
    }

    /**
     * Check if GPS is enabled
     */
    public boolean isGPSEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Check if Network location is enabled
     */
    public boolean isNetworkEnabled() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Check if location has changed significantly
     */
    public boolean hasLocationChangedSignificantly(double newLat, double newLng) {
        double[] lastLocation = getCachedLocation();

        if (lastLocation[0] == 0.0 && lastLocation[1] == 0.0) {
            return true;
        }

        float[] results = new float[1];
        Location.distanceBetween(
                lastLocation[0], lastLocation[1],
                newLat, newLng,
                results
        );

        float distanceInMeters = results[0];
        Log.d(TAG, "📏 Distance from last location: " + distanceInMeters + "m");

        return distanceInMeters >= SIGNIFICANT_DISTANCE_METERS;
    }

    /**
     * ✅ COMPLETE: Get current location with smart fallback
     */
    public void getCurrentLocation(OnLocationReceivedListener listener) {
        this.listener = listener;

        // Check permission
        if (!hasLocationPermission()) {
            Log.e(TAG, "❌ No location permission granted");
            Log.d(TAG, "📍 Using cached or default location");

            // Use cached or default
            double[] cached = getCachedLocation();
            listener.onLocationReceived(cached[0], cached[1]);
            return;
        }

        if (isRequestingLocation) {
            Log.w(TAG, "⚠️ Already requesting location");
            return;
        }

        isRequestingLocation = true;

        try {
            Log.d(TAG, "📍 Requesting location...");
            Log.d(TAG, "   GPS Enabled: " + isGPSEnabled());
            Log.d(TAG, "   Network Enabled: " + isNetworkEnabled());
            Log.d(TAG, "   Precise Permission: " + hasPreciseLocation());

            // Try GPS first (if precise permission)
            if (hasPreciseLocation() && isGPSEnabled()) {
                requestLocationFromProvider(LocationManager.GPS_PROVIDER);
            }
            // Try Network (works with both FINE and COARSE)
            else if (isNetworkEnabled()) {
                requestLocationFromProvider(LocationManager.NETWORK_PROVIDER);
            }
            // No provider available
            else {
                Log.w(TAG, "⚠️ No location provider available");
                useFallbackLocation();
            }

        } catch (SecurityException e) {
            Log.e(TAG, "❌ Security exception: " + e.getMessage());
            useFallbackLocation();
        }
    }

    /**
     * Request location from specific provider
     */
    private void requestLocationFromProvider(String provider) {
        try {
            Log.d(TAG, "📡 Requesting from provider: " + provider);

            // Get last known location first
            Location lastLocation = locationManager.getLastKnownLocation(provider);

            if (lastLocation != null && !isLocationStale(lastLocation)) {
                Log.d(TAG, "✅ Using last known location from " + provider);
                handleLocation(lastLocation);
                return;
            }

            // Request fresh location update
            Log.d(TAG, "🔄 Requesting fresh location from " + provider);

            locationManager.requestSingleUpdate(
                    provider,
                    locationListener,
                    Looper.getMainLooper()
            );

            // Timeout after 10 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(10000);
                    if (isRequestingLocation) {
                        Log.w(TAG, "⏰ Location request timeout");
                        useFallbackLocation();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SecurityException e) {
            Log.e(TAG, "❌ Permission error: " + e.getMessage());
            useFallbackLocation();
        }
    }

    /**
     * Check if location is stale (older than 5 minutes)
     */
    private boolean isLocationStale(Location location) {
        long age = System.currentTimeMillis() - location.getTime();
        long fiveMinutes = 5 * 60 * 1000;
        return age > fiveMinutes;
    }

    /**
     * Use fallback location (cached or default)
     */
    private void useFallbackLocation() {
        isRequestingLocation = false;

        double[] cached = getCachedLocation();

        Log.d(TAG, "📍 Using fallback location:");
        Log.d(TAG, "   Latitude: " + cached[0]);
        Log.d(TAG, "   Longitude: " + cached[1]);
        Log.d(TAG, "   City: " + getCachedCity());

        if (listener != null) {
            listener.onLocationReceived(cached[0], cached[1]);
        }
    }

    /**
     * Handle received location
     */
    private void handleLocation(Location location) {
        if (location == null) {
            useFallbackLocation();
            return;
        }

        isRequestingLocation = false;

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Log.d(TAG, "✅ Location received:");
        Log.d(TAG, "   Latitude: " + latitude);
        Log.d(TAG, "   Longitude: " + longitude);
        Log.d(TAG, "   Accuracy: " + location.getAccuracy() + "m");
        Log.d(TAG, "   Provider: " + location.getProvider());

        // Get city name
        String cityName = getCityName(latitude, longitude);

        Log.d(TAG, "📍 City: " + cityName);

        // Save to cache
        saveLocation(latitude, longitude, cityName);

        // Notify listener
        if (listener != null) {
            listener.onLocationReceived(latitude, longitude);
        }
    }

    /**
     * Get city name from coordinates (reverse geocoding)
     */
    private String getCityName(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                String country = address.getCountryName();

                if (city != null) {
                    return city + ", " + country;
                } else if (country != null) {
                    return country;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Geocoding error: " + e.getMessage());
        }

        return "Current Location";
    }

    /**
     * Get cached location (or default if none)
     */
    public double[] getCachedLocation() {
        double lat = Double.longBitsToDouble(
                prefs.getLong(KEY_LAST_LATITUDE, Double.doubleToLongBits(DEFAULT_LATITUDE)));
        double lng = Double.longBitsToDouble(
                prefs.getLong(KEY_LAST_LONGITUDE, Double.doubleToLongBits(DEFAULT_LONGITUDE)));

        return new double[]{lat, lng};
    }

    /**
     * Get cached city name
     */
    public String getCachedCity() {
        return prefs.getString(KEY_LAST_CITY, DEFAULT_CITY);
    }

    /**
     * Save location to cache
     */
    private void saveLocation(double latitude, double longitude, String city) {
        prefs.edit()
                .putLong(KEY_LAST_LATITUDE, Double.doubleToLongBits(latitude))
                .putLong(KEY_LAST_LONGITUDE, Double.doubleToLongBits(longitude))
                .putString(KEY_LAST_CITY, city)
                .apply();

        Log.d(TAG, "💾 Cached location: " + city + " (" + latitude + ", " + longitude + ")");
    }

    /**
     * Update cached location (for API responses)
     */
    public void updateCachedLocation(double latitude, double longitude) {
        prefs.edit()
                .putLong(KEY_LAST_LATITUDE, Double.doubleToLongBits(latitude))
                .putLong(KEY_LAST_LONGITUDE, Double.doubleToLongBits(longitude))
                .apply();
        Log.d(TAG, "💾 Updated cached location: " + latitude + ", " + longitude);
    }

    /**
     * Location listener
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "📡 Location update received");
            handleLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "✅ Provider enabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "❌ Provider disabled: " + provider);
        }
    };

    /**
     * Stop location updates
     */
    public void stopLocationUpdates() {
        try {
            locationManager.removeUpdates(locationListener);
            isRequestingLocation = false;
            Log.d(TAG, "🛑 Location updates stopped");
        } catch (SecurityException e) {
            Log.e(TAG, "Error stopping updates: " + e.getMessage());
        }
    }

    /**
     * Clear cached location
     */
    public void clearCache() {
        prefs.edit().clear().apply();
        Log.d(TAG, "🗑️ Location cache cleared");
    }

    /**
     * Callback interface
     */
    public interface OnLocationReceivedListener {
        void onLocationReceived(double latitude, double longitude);
        void onError(String error);
    }
}