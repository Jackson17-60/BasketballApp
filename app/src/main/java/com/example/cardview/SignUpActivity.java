package com.example.cardview;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button btnsignUp;
    private ImageView signup_show_password;
    private ImageButton backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        signup_show_password = findViewById(R.id.signup_show_password);
        emailEditText = findViewById(R.id.signup_email);
        passwordEditText = findViewById(R.id.signup_pass);
        btnsignUp = findViewById(R.id.btn_signup);
        backButton = findViewById(R.id.back_button);


        // Set an animation listener to reset the animation on completion

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        signup_show_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginController.togglePasswordVisibility(passwordEditText);
            }
        });

        passwordEditText.addTextChangedListener(new LoginController(signup_show_password));

        btnsignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (!LoginController.isInputValid(email, password)) {
                    LoginController.showEmptyFieldsToast(SignUpActivity.this);
                    return; // Don't proceed with sign-up if fields are empty
                }
                FirebaseApp.initializeApp(getBaseContext());
                FirebaseAuth database = FirebaseAuth.getInstance();
                database.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Registration successful, user account is created and authenticated
                                DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                                FirebaseUser newUser = task.getResult().getUser();
                                User user = new User("","Set Name","Set Gender","Set Height","Set Level","Set Location");
                                db.child("users").child(newUser.getUid()).setValue(user);

                                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
//                                FirebaseUser user = database.getCurrentUser();
                                // You can now navigate the user to the home screen or perform other actions.
                            } else {
                                // Registration failed, handle the error
                                Toast.makeText(SignUpActivity.this, "Registration failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }


}