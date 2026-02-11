package com.example.sirralquran.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Enhanced LocationHelper with location change detection
 */
public class LocationHelper {

    private static final String TAG = "LocationHelper";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private static final String PREFS_NAME = "LocationPrefs";
    private static final String KEY_LAST_LATITUDE = "last_latitude";
    private static final String KEY_LAST_LONGITUDE = "last_longitude";
    private static final String KEY_LAST_CITY = "last_city";

    // Distance threshold in meters (500m = significant location change)
    private static final float SIGNIFICANT_DISTANCE_METERS = 500.0f;

    private Context context;
    private LocationManager locationManager;
    private SharedPreferences prefs;
    private OnLocationReceivedListener listener;

    public LocationHelper(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if location permissions are granted
     */
    public boolean hasLocationPermission() {
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
     * Check if location has changed significantly
     */
    public boolean hasLocationChangedSignificantly(double newLat, double newLng) {
        double[] lastLocation = getCachedLocation();

        if (lastLocation[0] == 0.0 && lastLocation[1] == 0.0) {
            // No previous location saved
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
     * Get current location (or last known)
     */
    public void getCurrentLocation(OnLocationReceivedListener listener) {
        this.listener = listener;

        if (!hasLocationPermission()) {
            Log.e(TAG, "❌ Location permission not granted");
            listener.onError("Location permission required");
            return;
        }

        try {
            // Try GPS first
            if (isGPSEnabled()) {
                Log.d(TAG, "📍 Getting GPS location...");
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0,
                        locationListener
                );

                // Also get last known location immediately
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    Log.d(TAG, "✅ Using last known GPS location");
                    handleLocation(lastLocation);
                }
            }
            // Fallback to network location
            else {
                Log.d(TAG, "📍 GPS disabled, using network location...");
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        0,
                        locationListener
                );

                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastLocation != null) {
                    Log.d(TAG, "✅ Using last known network location");
                    handleLocation(lastLocation);
                }
            }

        } catch (SecurityException e) {
            Log.e(TAG, "❌ Security exception: " + e.getMessage());
            listener.onError("Permission denied");
        }
    }

    /**
     * Get cached location (from last fetch)
     */
    public double[] getCachedLocation() {
        double lat = Double.longBitsToDouble(prefs.getLong(KEY_LAST_LATITUDE, Double.doubleToLongBits(31.5204)));  // Default: Lahore
        double lng = Double.longBitsToDouble(prefs.getLong(KEY_LAST_LONGITUDE, Double.doubleToLongBits(74.3587)));
        return new double[]{lat, lng};
    }

    /**
     * Get cached city name
     */
    public String getCachedCity() {
        return prefs.getString(KEY_LAST_CITY, "Lahore, Pakistan");
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
     * Update cached location (for manual updates)
     */
    public void updateCachedLocation(double latitude, double longitude) {
        prefs.edit()
                .putLong(KEY_LAST_LATITUDE, Double.doubleToLongBits(latitude))
                .putLong(KEY_LAST_LONGITUDE, Double.doubleToLongBits(longitude))
                .apply();
        Log.d(TAG, "💾 Updated cached location: " + latitude + ", " + longitude);
    }

    /**
     * Handle received location
     */
    private void handleLocation(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Log.d(TAG, "✅ Location received: " + latitude + ", " + longitude);

            // Save to cache
            saveLocation(latitude, longitude, "Current Location");

            // Stop updates
            locationManager.removeUpdates(locationListener);

            // Notify listener
            if (listener != null) {
                listener.onLocationReceived(latitude, longitude);
            }
        }
    }

    /**
     * Location listener
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            handleLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "Provider enabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "Provider disabled: " + provider);
        }
    };

    /**
     * Stop location updates
     */
    public void stopLocationUpdates() {
        locationManager.removeUpdates(locationListener);
    }

    /**
     * Callback interface
     */
    public interface OnLocationReceivedListener {
        void onLocationReceived(double latitude, double longitude);
        void onError(String error);
    }
}