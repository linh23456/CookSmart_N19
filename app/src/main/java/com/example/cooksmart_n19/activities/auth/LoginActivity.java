package com.example.cooksmart_n19.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.activities.MainActivity;
import com.example.cooksmart_n19.interfaces.FirebaseCallback;
import com.example.cooksmart_n19.models.User;
import com.example.cooksmart_n19.utils.FirebaseAuthHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin;
    private TextView textViewForgotPassword, textViewRegister;
    private FirebaseAuthHelper authHelper;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuthHelper
        authHelper = new FirebaseAuthHelper(this);

        // Check if user is already logged in
        if (authHelper.isUserLoggedIn()) {
            authHelper.navigateToMain();
            return;
        }

        // Initialize UI components
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewRegister = findViewById(R.id.textViewRegister);
        progressBar = findViewById(R.id.progressBar);

        // Set click listeners
        buttonLogin.setOnClickListener(view -> loginUser());
        textViewForgotPassword.setOnClickListener(view -> forgotPassword());
        textViewRegister.setOnClickListener(view -> navigateToRegister());

        // Set up social login options
        findViewById(R.id.imageViewFacebook).setOnClickListener(view -> signInWithFacebook());
        findViewById(R.id.imageViewGoogle).setOnClickListener(view -> signInWithGoogle());
        findViewById(R.id.imageViewPhone).setOnClickListener(view -> signInWithPhone());
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email là bắt buộc");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Mật khẩu là bắt buộc");
            editTextPassword.requestFocus();
            return;
        }

        // Show progress indicator
        showProgressBar(true);

        // Login user
        authHelper.signIn(email, password, new FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                showProgressBar(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                authHelper.navigateToMain();
            }

            @Override
            public void onFailure(String errorMessage) {
                showProgressBar(false);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void forgotPassword() {
        String email = editTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Vui lòng nhập email để đặt lại mật khẩu");
            editTextEmail.requestFocus();
            return;
        }

        showProgressBar(true);

        authHelper.resetPassword(email, new FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                showProgressBar(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                showProgressBar(false);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonLogin.setEnabled(!show);
    }

    // Social login methods (placeholder implementations)

    private void signInWithGoogle() {
        Toast.makeText(this, "Chức năng đăng nhập Google đang được phát triển", Toast.LENGTH_SHORT).show();
        // Implement Google Sign-In
    }

    private void signInWithFacebook() {
        Toast.makeText(this, "Chức năng đăng nhập Facebook đang được phát triển", Toast.LENGTH_SHORT).show();
        // Implement Facebook Sign-In
    }

    private void signInWithPhone() {
        Toast.makeText(this, "Chức năng đăng nhập số điện thoại đang được phát triển", Toast.LENGTH_SHORT).show();
        // Implement Phone Sign-In or navigate to phone sign-in activity
    }
}