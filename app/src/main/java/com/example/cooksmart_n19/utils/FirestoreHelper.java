package com.example.cooksmart_n19.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.cooksmart_n19.interfaces.FirebaseCallback;
import com.example.cooksmart_n19.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public void getUserData(FirebaseCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onUserDataReceived(user);
                    } else {
                        callback.onFailure("Không tìm thấy dữ liệu người dùng");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user data", e);
                    callback.onFailure("Lỗi khi tải dữ liệu người dùng: " + e.getMessage());
                });
    }

    public void updateUserProfile(User user, FirebaseCallback callback) {
        if (user == null || user.getUserId() == null) {
            callback.onFailure("Dữ liệu người dùng không hợp lệ");
            return;
        }

        user.setUpdatedAt(Timestamp.now());

        db.collection("users").document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess("Cập nhật hồ sơ thành công");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user profile", e);
                    callback.onFailure("Lỗi khi cập nhật hồ sơ: " + e.getMessage());
                });
    }

    public void removeFromFavorites(String recipeId, FirebaseCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                List<String> favorites = user.getFavoriteRecipeIds();

                // Remove recipe from favorites
                if (favorites.remove(recipeId)) {
                    user.setFavoriteRecipeIds(favorites);
                    user.setUpdatedAt(Timestamp.now());

                    // Update user document
                    userRef.set(user)
                        .addOnSuccessListener(aVoid -> {
                            callback.onSuccess("Đã xóa khỏi danh sách yêu thích");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error removing from favorites", e);
                            callback.onFailure("Lỗi khi xóa khỏi yêu thích: " + e.getMessage());
                        });
                } else {
                    callback.onFailure("Công thức không có trong danh sách yêu thích");
                }
            } else {
                callback.onFailure("Không tìm thấy dữ liệu người dùng");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error getting user data", e);
            callback.onFailure("Lỗi khi tải dữ liệu người dùng: " + e.getMessage());
        });
    }

    public void deleteUserAccount(FirebaseCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        String userId = currentUser.getUid();

        // Delete user document from Firestore
        db.collection("users").document(userId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                // Now delete the Firebase Auth account
                currentUser.delete()
                    .addOnSuccessListener(aVoid2 -> {
                        callback.onSuccess("Tài khoản đã được xóa thành công");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting auth account", e);
                        callback.onFailure("Lỗi khi xóa tài khoản xác thực: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error deleting user document", e);
                callback.onFailure("Lỗi khi xóa dữ liệu người dùng: " + e.getMessage());
            });
    }
}