package com.example.cooksmart_n19.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.cooksmart_n19.activities.MainActivity;
import com.example.cooksmart_n19.activities.auth.LoginActivity;
import com.example.cooksmart_n19.interfaces.FirebaseCallback;
import com.example.cooksmart_n19.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FirebaseAuthHelper {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Context context;

    public FirebaseAuthHelper(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void signUp(String email, String password, String username, FirebaseCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                createUserInFirestore(firebaseUser.getUid(), username, email, callback);
                            }
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }

    private void createUserInFirestore(String userId, String username, String email, FirebaseCallback callback) {
        User user = new User();
        user.setUserId(userId);
        user.setName(username);
        user.setEmail(email);
        user.setFavoriteRecipeIds(new ArrayList<>());
        user.setCreatedAt(Timestamp.now());
        user.setUpdatedAt(Timestamp.now());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Đăng ký thành công"))
                .addOnFailureListener(e -> callback.onFailure("Lỗi khi tạo hồ sơ: " + e.getMessage()));
    }

    public void signIn(String email, String password, FirebaseCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Đăng nhập thành công");
                    } else {
                        callback.onFailure("Đăng nhập thất bại: " + task.getException().getMessage());
                    }
                });
    }

    public void signOut() {
        mAuth.signOut();
        navigateToLogin();
    }

    public void resetPassword(String email, FirebaseCallback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Email đặt lại mật khẩu đã được gửi");
                    } else {
                        callback.onFailure("Không thể gửi email đặt lại mật khẩu: " + task.getException().getMessage());
                    }
                });
    }

    public void getUserData(String userId, FirebaseCallback callback) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    User user = document.toObject(User.class);
                    callback.onUserDataReceived(user);
                } else {
                    callback.onFailure("Không tìm thấy dữ liệu người dùng");
                }
            } else {
                callback.onFailure("Lỗi khi tải dữ liệu người dùng: " + task.getException().getMessage());
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public void navigateToMain() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }
}