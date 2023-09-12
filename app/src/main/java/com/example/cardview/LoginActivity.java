package com.example.cardview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText passwordEditText;
    private TextView signUp;
    private ImageView showpassword,bkbLogo;
    private Button btnLogin;
    private ObjectAnimator bounceAnim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bkbLogo = findViewById(R.id.bkb_logo);
        signUp = findViewById(R.id.sign_up);
         passwordEditText = findViewById(R.id.login_pass);
         btnLogin = findViewById(R.id.btn_login);
        showpassword = findViewById(R.id.show_password);
        showpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginController.togglePasswordVisibility(passwordEditText);
            }
        });
        bounceAnim = ObjectAnimator.ofFloat(bkbLogo, "translationY", 0, 30, 0);
        bounceAnim.setDuration(1000); // Duration in milliseconds
        bounceAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        bounceAnim.setRepeatCount(ObjectAnimator.INFINITE); // Infinite bounce

        // Set an animation listener to reset the animation on completion
        bounceAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animation.start(); // Restart the animation
            }
        });

        // Start the bounce animation
        bounceAnim.start();
        passwordEditText.addTextChangedListener(new LoginController(showpassword));
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String email = emailEditText.getText().toString();
//                String password = passwordEditText.getText().toString();
//                if (!LoginController.isInputValid(email, password)) {
//                    LoginController.showEmptyFieldsToast(LoginActivity.this);
//                    return; // Don't proceed with sign-up if fields are empty
//                }
                FirebaseApp.initializeApp(LoginActivity.this);
                FirebaseAuth database = FirebaseAuth.getInstance();
//                database.signInWithEmailAndPassword(email, password)
                database.signInWithEmailAndPassword("1@gmail.com", "123456")
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Sign-in successful, user is authenticated
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Sign-in failed, handle the error
                                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();

        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        bounceAnim.cancel();
    }


    }

