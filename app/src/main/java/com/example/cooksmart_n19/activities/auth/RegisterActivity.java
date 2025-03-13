package com.example.cooksmart_n19.activities.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.activities.MainActivity;
import com.example.cooksmart_n19.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtUsername, edtEmail, edtPassword;
    private MaterialButton btnRegister;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        initializeViews();
        setupRegisterButton();
    }

    private void initializeViews() {
        edtUsername = findViewById(R.id.edt_username);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void setupRegisterButton() {
        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    private void validateAndRegister() {
        String name = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (isValidInput(name, email, password)) {
            createUserWithFirebase(name, email, password);
        }
    }

    private boolean isValidInput(String name, String email, String password) {
        boolean valid = true;

        if (name.isEmpty() || name.length() < 3) {
            edtUsername.setError("Tên phải có ít nhất 3 ký tự");
            valid = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            valid = false;
        }

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            valid = false;
        }

        return valid;
    }

    private void createUserWithFirebase(String name, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser.getUid(), name, email);
                        }
                    } else {
                        handleError(task.getException());
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, String email) {
        User newUser = new User(name, email);
        newUser.setUserId(userId);

        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .set(newUser.toFirestoreMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    redirectToMainActivity();
                })
                .addOnFailureListener(e -> {
                    // Xóa tài khoản Firebase nếu lưu Firestore thất bại
                    auth.getCurrentUser().delete();
                    handleError(e);
                });
    }

    private void redirectToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }

    private void handleError(Exception exception) {
        String errorMessage = "Lỗi đăng ký: ";
        if (exception != null) {
            if (exception.getMessage().contains("email address is already in use")) {
                errorMessage += "Email đã được sử dụng";
            } else {
                errorMessage += exception.getMessage();
            }
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
}