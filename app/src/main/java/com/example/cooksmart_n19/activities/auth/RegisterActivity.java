package com.example.cooksmart_n19.activities.auth;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.activities.MainActivity;
import com.example.cooksmart_n19.models.User;
import com.example.cooksmart_n19.utils.FirebaseAuthHelper;
import com.example.cooksmart_n19.utils.FirestoreHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseNetworkException;
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

    private static final String CHANNEL_ID = "register_channel";

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Register Notifications";
            String description = "Thông báo khi nhập dữ liệu không hợp lệ";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void sendNotification(String title, String message) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            } else {
                // Just log instead of blocking the flow
                Log.d("RegisterActivity", "No notification permission, but continuing registration");
            }
        } catch (Exception e) {
            // Catch any notification errors to prevent them from blocking registration
            e.printStackTrace();
            Log.e("RegisterActivity", "Notification error: " + e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        createNotificationChannel();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Set up click listeners
        buttonRegister.setOnClickListener(view -> {
            Toast.makeText(RegisterActivity.this, "Register button clicked", Toast.LENGTH_SHORT).show();
            registerUser();
        });

        // Set up social login options
        findViewById(R.id.imageViewFacebook).setOnClickListener(view -> signInWithFacebook());
        findViewById(R.id.imageViewGoogle).setOnClickListener(view -> signInWithGoogle());
        findViewById(R.id.imageViewPhone).setOnClickListener(view -> signInWithPhone());

        // Set up login option
        findViewById(R.id.textViewLogin).setOnClickListener(view -> {
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
            sendNotification("Lỗi đăng ký", "Tên người dùng là bắt buộc");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email là bắt buộc");
            editTextEmail.requestFocus();
            sendNotification("Lỗi đăng ký", "Email là bắt buộc");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Mật khẩu là bắt buộc");
            editTextPassword.requestFocus();
            sendNotification("Lỗi đăng ký", "Password là bắt buộc");
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            editTextPassword.requestFocus();
            sendNotification("Lỗi đăng ký", "Mật khẩu phải có 6 kí tự đổ lên");
            return;
        }

        // Show progress indicator
        showProgressBar(true);

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            createUserProfile(firebaseUser.getUid(), username, email);
                            sendNotification("Đăng ký thành công" , "cảm ơn");
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        showProgressBar(false);
                        String errorMsg = "Đăng kí that bai " + task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        sendNotification("Lỗi đăng ký", errorMsg);
                        Exception e = task.getException();
                        if (e instanceof FirebaseNetworkException) {
                            Toast.makeText(RegisterActivity.this, "Lỗi mạng: Kiểm tra kết nối Internet", Toast.LENGTH_SHORT).show();
                            Log.e("FIREBASE_ERROR", "Network error: " + e.getMessage());
                        } else {
                            Toast.makeText(RegisterActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("FIREBASE_ERROR", "Error: " + e.getMessage());
                        }
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