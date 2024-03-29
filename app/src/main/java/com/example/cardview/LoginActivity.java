package com.example.cardview;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cardview.Controller.LoginController;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private EditText passwordEditText,emailEditText;
    private TextView signUp;
    private ImageView showpassword,bkbLogo;
    private boolean isActivityBeingDestroyed = false;
    private Button btnLogin;
    private ObjectAnimator bounceAnim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeViews();
        setupAnimations();
        setupListeners();
    }
    private void initializeViews() {
        bkbLogo = findViewById(R.id.bkb_logo);
        signUp = findViewById(R.id.sign_up);
        emailEditText = findViewById(R.id.login_email);
        passwordEditText = findViewById(R.id.login_pass);
        btnLogin = findViewById(R.id.btn_login);
        showpassword = findViewById(R.id.show_password);
    }
    private void setupListeners() {
        showpassword.setOnClickListener(v -> LoginController.togglePasswordVisibility(passwordEditText));
        passwordEditText.addTextChangedListener(new LoginController(showpassword));

        btnLogin.setOnClickListener(v -> handleLoginClick());
        signUp.setOnClickListener(v -> handleSignUpClick());
    }
    private void setupAnimations() {
        bounceAnim = ObjectAnimator.ofFloat(bkbLogo, "translationY", 0, 30, 0);
        bounceAnim.setDuration(1000); // Duration in milliseconds
        bounceAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        bounceAnim.setRepeatCount(ObjectAnimator.INFINITE); // Infinite bounce

        // Set an animation listener to reset the animation on completion
        bounceAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!isActivityBeingDestroyed) {
                    animation.start(); // Restart the animation
                }
            }
        });

        // Start the bounce animation
        bounceAnim.start();
    }
    private void handleSignUpClick() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }


    private void handleLoginClick() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (!LoginController.isInputValidLogin(email, password)) {
            LoginController.showEmptyFieldsToast(LoginActivity.this);
            return; // Don't proceed with sign-up if fields are empty
        }
        FirebaseApp.initializeApp(LoginActivity.this);
        FirebaseAuth database = FirebaseAuth.getInstance();
        database.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in successful, user is authenticated
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(tokenTask -> {
                                    if (!tokenTask.isSuccessful()) {
                                        Log.w("FCM", "Fetching FCM registration token failed", tokenTask.getException());
                                        return;
                                    }

                                    // Get and set the new FCM registration token
                                    String token = tokenTask.getResult();
                                    DatabaseReference db = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                                    db.child("fcmToken").setValue(token)
                                            .addOnSuccessListener(aVoid -> Log.d("FCM", "Token updated successfully"))
                                            .addOnFailureListener(e -> Log.e("FCM", "Failed to update token", e));
                                });

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Sign-in failed, handle the error
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityBeingDestroyed = true;
        bounceAnim.cancel();
        passwordEditText = null;
        signUp = null;
        showpassword = null;
        btnLogin = null;
        bkbLogo = null;
    }


    }

