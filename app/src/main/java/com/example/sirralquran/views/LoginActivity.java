package com.example.sirralquran.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.sirralquran.R;
import com.example.sirralquran.controllers.AuthController;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private MaterialButton createAccountButton;
    private MaterialButton googleButton;
    private TextView forgotPasswordText;
    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        authController = new AuthController(this);
        setupClickListeners();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);
        googleButton = findViewById(R.id.googleButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
    }

    private void setupClickListeners() {
        // Login Button Click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // Create Account Button Click
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // Google Sign-In Button Click
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleGoogleLogin();
            }
        });

        // Forgot Password Click
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleForgotPassword();
            }
        });
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

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

        // Disable button during login
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        // Perform login
        boolean loginSuccess = authController.login(email, password);

        if (loginSuccess) {
            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();

            // Navigate to Home
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Re-enable button
            loginButton.setEnabled(true);
            loginButton.setText(R.string.login);

            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGoogleLogin() {
        // TODO: Implement Google Sign-In
        Toast.makeText(this, "Google Sign-In coming soon", Toast.LENGTH_SHORT).show();

        // For now, you can add Google Sign-In implementation here
        // Example: Sign in with Google using Firebase Auth or Google Sign-In SDK
    }

    private void handleForgotPassword() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show();
            emailInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            emailInput.requestFocus();
            return;
        }

        // TODO: Implement password reset functionality
        Toast.makeText(this, "Password reset link will be sent to: " + email, Toast.LENGTH_LONG).show();

        // You can implement Firebase password reset here
        // Example: mAuth.sendPasswordResetEmail(email)
    }

    @Override
    public void onBackPressed() {
        // Exit app when back is pressed on login screen
        finishAffinity();
    }
}