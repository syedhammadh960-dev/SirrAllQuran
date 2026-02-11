package com.example.sirralquran.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.sirralquran.R;
import com.example.sirralquran.controllers.AuthController;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText fullNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button createAccountButton;
    private MaterialButton loginButton;
    private MaterialButton googleButton;
    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeViews();
        authController = new AuthController(this);
        setupClickListeners();
    }

    private void initializeViews() {
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        createAccountButton = findViewById(R.id.createAccountButton);
        loginButton = findViewById(R.id.loginButton);
        googleButton = findViewById(R.id.googleButton);
    }

    private void setupClickListeners() {
        // Create Account Button Click
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignUp();
            }
        });

        // Login Button Click (Go back to login)
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to login screen
            }
        });

        // Google Sign-Up Button Click
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleGoogleSignUp();
            }
        });
    }

    private void handleSignUp() {
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            fullNameInput.setError("Full name is required");
            fullNameInput.requestFocus();
            return;
        }

        if (fullName.length() < 3) {
            fullNameInput.setError("Name must be at least 3 characters");
            fullNameInput.requestFocus();
            return;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        // Check password strength
        if (!isPasswordStrong(password)) {
            passwordInput.setError("Password must contain letters and numbers");
            passwordInput.requestFocus();
            return;
        }

        // Disable button during sign up
        createAccountButton.setEnabled(false);
        createAccountButton.setText("Creating Account...");

        // Perform sign up
        boolean signUpSuccess = authController.signUp(fullName, email, password);

        if (signUpSuccess) {
            Toast.makeText(this, "Welcome to Sirr-Ul-Quran, " + fullName + "!", Toast.LENGTH_SHORT).show();

            // Navigate to Home
            Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Re-enable button
            createAccountButton.setEnabled(true);
            createAccountButton.setText(R.string.signup);

            Toast.makeText(this, "Sign up failed. Email may already be registered.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGoogleSignUp() {
        // TODO: Implement Google Sign-Up
        Toast.makeText(this, "Google Sign-Up coming soon", Toast.LENGTH_SHORT).show();

        // For now, you can add Google Sign-Up implementation here
        // Example: Sign up with Google using Firebase Auth or Google Sign-In SDK
    }

    private boolean isPasswordStrong(String password) {
        // Check if password contains at least one letter and one number
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasLetter && hasDigit) {
                return true;
            }
        }

        return hasLetter && hasDigit;
    }

    @Override
    public void onBackPressed() {
        // Go back to login screen
        super.onBackPressed();
    }
}