package com.example.cooksmart_n19.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.models.User;
import com.example.cooksmart_n19.utils.FirebaseAuthHelper;
import com.example.cooksmart_n19.utils.FirestoreHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPassword;
    private MaterialButton buttonRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Set up click listeners
        buttonRegister.setOnClickListener(view -> registerUser());

        // Set up social login options
        findViewById(R.id.imageViewFacebook).setOnClickListener(view -> signInWithFacebook());
        findViewById(R.id.imageViewGoogle).setOnClickListener(view -> signInWithGoogle());
        findViewById(R.id.imageViewPhone).setOnClickListener(view -> signInWithPhone());

        // Set up login option
        findViewById(R.id.textViewLogin).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Tên người dùng là bắt buộc");
            editTextUsername.requestFocus();
            return;
        }

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

        if (password.length() < 6) {
            editTextPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            editTextPassword.requestFocus();
            return;
        }

        // Show progress indicator
        showProgressBar(true);

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, create user profile in Firestore
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            createUserProfile(firebaseUser.getUid(), username, email);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        showProgressBar(false);
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserProfile(String userId, String username, String email) {
        User user = new User();
        user.setUserId(userId);
        user.setName(username);
        user.setEmail(email);
        user.setFavoriteRecipeIds(new ArrayList<>());
        user.setCreatedAt(Timestamp.now());
        user.setUpdatedAt(Timestamp.now());

        // Save user to Firestore
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    showProgressBar(false);
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    navigateToLoginActivity();
                })
                .addOnFailureListener(e -> {
                    showProgressBar(false);
                    Toast.makeText(RegisterActivity.this, "Lỗi khi tạo hồ sơ: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showProgressBar(boolean show) {
        View progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        buttonRegister.setEnabled(!show);
    }

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