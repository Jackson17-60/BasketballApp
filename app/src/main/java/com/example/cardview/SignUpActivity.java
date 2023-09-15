package com.example.cardview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.cardview.Controller.LoginController;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.cardview.Model_Class.User;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText,nameEditText;
    private Button btnsignUp;
    private ImageView signup_show_password;
    private ImageButton backButton;
    private String selectedLevel;
    private FirebaseAuth database;
    private RadioGroup signUplevelRadioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        signup_show_password = findViewById(R.id.signup_show_password);
        emailEditText = findViewById(R.id.signup_email);
        passwordEditText = findViewById(R.id.signup_pass);
        nameEditText = findViewById(R.id.signup_name);
        FirebaseApp.initializeApp(this);
        database = FirebaseAuth.getInstance();
        btnsignUp = findViewById(R.id.btn_signup);
        backButton = findViewById(R.id.back_button);
        signUplevelRadioGroup = findViewById(R.id.signuplevelRadioGroup);
        setupSignUpLevelRadioGroupListener();

        // Set an animation listener to reset the animation on completion

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        signup_show_password.setOnClickListener(v -> LoginController.togglePasswordVisibility(passwordEditText));

        passwordEditText.addTextChangedListener(new LoginController(signup_show_password));

        btnsignUp.setOnClickListener(v -> {

            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String name = nameEditText.getText().toString();
            int selectedRadioButtonId = signUplevelRadioGroup.getCheckedRadioButtonId();

            if (!LoginController.isInputValid(name,email, password,selectedRadioButtonId)) {
                LoginController.showEmptyFieldsToast(SignUpActivity.this);
                return; // Don't proceed with sign-up if fields are empty
            }

            database.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignUpActivity.this, task -> {
                        if (task.isSuccessful()) {
                            // Registration successful, user account is created and authenticated
                            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                            FirebaseUser newUser = task.getResult().getUser();
                            User user = new User("",name,"Set Gender","Set Height In",selectedLevel,"Set Address");
                            assert newUser != null;
                            db.child("users").child(newUser.getUid()).setValue(user);

                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // Registration failed, handle the error
                            Toast.makeText(SignUpActivity.this, "Registration failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
    private void setupSignUpLevelRadioGroupListener() {
        signUplevelRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.signup_beginner_radiobtn) {
                selectedLevel = "Beginner";
            } else if (checkedId == R.id.signup_ama_radiobtn) {
                selectedLevel = "Amateur";
            }
            else if(checkedId == R.id.signup_pro_radiobtn) {
                selectedLevel = "Professional";
            }
        });
    }

}