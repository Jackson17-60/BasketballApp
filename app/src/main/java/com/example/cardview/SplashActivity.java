package com.example.cardview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseApp.initializeApp(this);

        // Add a delay before checking the authentication state (optional)
        checkAuthenticationState();
    }
    private void checkAuthenticationState() {
        // Get Firebase auth instance
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            // User is logged in, navigate to MainActivity
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            // User is not logged in, navigate to LoginActivity
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }

        // Finish the SplashActivity so it's removed from the activity stack
        finish();
    }
}