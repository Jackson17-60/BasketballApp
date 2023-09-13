package com.example.cardview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class LoginController implements TextWatcher {
    private ImageView showPassword;
    private static ObjectAnimator bounceAnim;
    public LoginController(ImageView showPassword) {
        this.showPassword = showPassword;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        showPassword.setVisibility(s.length() > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    public static void togglePasswordVisibility(EditText passwordEditText) {
        int cursorPosition = passwordEditText.getSelectionEnd();
        if (passwordEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        // Restore cursor position
        passwordEditText.setSelection(cursorPosition);
    }
    public static boolean isInputValid(String name,String email, String password , int  selectedRadioButtonId) {
        return  !TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)  && selectedRadioButtonId != -1;
    }

    public static void showEmptyFieldsToast(Context context) {
        Toast.makeText(context, "Please fill in all the field", Toast.LENGTH_SHORT).show();
    }


}
