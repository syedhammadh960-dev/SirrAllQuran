package com.example.sirralquran.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.sirralquran.R;
import com.example.sirralquran.controllers.AuthController;
import com.example.sirralquran.controllers.UserController;
import com.example.sirralquran.models.User;

public class ProfileActivity extends AppCompatActivity {

    private ImageView backButton;
    private ImageView profileImage;
    private TextView userNameText;
    private TextView userRoleText;
    private TextView completedCountText;
    private TextView progressPercentText;
    private LinearLayout feedbackLayout;
    private LinearLayout settingsLayout;
    private LinearLayout logoutLayout;
    private BottomNavigationView bottomNavigationView;
    
    private UserController userController;
    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        userController = new UserController(this);
        authController = new AuthController(this);
        loadUserProfile();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        profileImage = findViewById(R.id.profileImage);
        userNameText = findViewById(R.id.userNameText);
        userRoleText = findViewById(R.id.userRoleText);
        completedCountText = findViewById(R.id.completedCountText);
        progressPercentText = findViewById(R.id.progressPercentText);
        feedbackLayout = findViewById(R.id.feedbackLayout);
        settingsLayout = findViewById(R.id.settingsLayout);
        logoutLayout = findViewById(R.id.logoutLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void loadUserProfile() {
        User user = userController.getCurrentUser();
        if (user != null) {
            userNameText.setText(user.getFullName());
            completedCountText.setText(user.getCompletedDays() + "/30");
            progressPercentText.setText(user.getProgressPercentage() + "%");
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        feedbackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle feedback
            }
        });

        settingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle settings
            }
        });

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        authController.logout();
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_salah) {
                startActivity(new Intent(ProfileActivity.this, SalahTrackerActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_thirty_day) {
                startActivity(new Intent(ProfileActivity.this, ThirtyDayJourneyActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            
            return false;
        });
    }
}
