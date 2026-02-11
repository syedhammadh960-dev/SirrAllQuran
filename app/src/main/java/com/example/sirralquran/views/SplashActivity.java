package com.example.sirralquran.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sirralquran.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * FIXED: Removed trailing slash - this was causing TWO different Firebase instances!
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DURATION = 3000; // 3 seconds

    // CRITICAL: URL must match google-services.json EXACTLY (no trailing slash!)
    private static final String FIREBASE_URL = "https://sirrallquran-default-rtdb.firebaseio.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase FIRST
        initializeFirebase();

        // Navigate to Home after splash duration (NO LOGIN)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DURATION);
    }

    /**
     * Initialize Firebase Realtime Database
     * CRITICAL: This creates the SINGLE instance used throughout the app
     */
    private void initializeFirebase() {
        try {
            // Initialize Firebase App
            FirebaseApp.initializeApp(this);

            // CRITICAL FIX: No trailing slash! Must match google-services.json
            FirebaseDatabase database = FirebaseDatabase.getInstance(FIREBASE_URL);

            // Enable offline persistence
            database.setPersistenceEnabled(true);

            // Keep data synced
            database.getReference().keepSynced(true);

            Log.d(TAG, "✅ Firebase initialized: " + FIREBASE_URL);
            Log.d(TAG, "✅ Persistence enabled, keepSynced enabled");

        } catch (Exception e) {
            Log.e(TAG, "❌ Firebase initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}