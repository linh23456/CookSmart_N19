package com.example.cooksmart_n19.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.cooksmart_n19.interfaces.FirebaseCallback;
import com.example.cooksmart_n19.models.User;
import com.example.cooksmart_n19.utils.CloudinaryManager;
import com.example.cooksmart_n19.utils.FirebaseAuthHelper;
import com.example.cooksmart_n19.utils.FirestoreHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UserRepository {
    private static final String TAG = "UserRepository";

    private Context context;
    private FirebaseAuthHelper authHelper;
    private FirestoreHelper firestoreHelper;

    // --- thêm biến để cache user hiện tại ---
    private User currentUser;

    public UserRepository(Context context) {
        this.context = context;
        this.authHelper = new FirebaseAuthHelper(context);
        this.firestoreHelper = new FirestoreHelper();
        CloudinaryManager.init(context);
    }

    /**
     * Trả về User đang được cache (null nếu chưa login hoặc mới khởi tạo).
     */
    public User getCurrentUser() {
        return currentUser;
    }

    public void registerUser(String email, String password, String username, FirebaseCallback callback) {
        authHelper.signUp(email, password, username, new FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                // Sau khi tạo auth thành công, fetch data từ Firestore
                firestoreHelper.getUserData(new FirebaseCallback() {
                    @Override
                    public void onUserDataReceived(User user) {
                        // Cache lại user
                        currentUser = user;
                        callback.onSuccess(message);
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        // Dù fetch thất bại, vẫn coi là đăng ký thành công
                        callback.onSuccess(message);
                    }
                });
            }
            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public void loginUser(String email, String password, boolean rememberMe, FirebaseCallback callback) {
        authHelper.signIn(email, password, new FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                // Fetch và cache user data
                firestoreHelper.getUserData(new FirebaseCallback() {
                    @Override
                    public void onUserDataReceived(User user) {
                        currentUser = user;
                        callback.onSuccess(message);
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        // Vẫn coi là login thành công
                        callback.onSuccess(message);
                    }
                });
            }
            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public void updateUserProfile(User user, Uri profileImageUri, FirebaseCallback callback) {
        if (profileImageUri != null) {
            uploadProfileImage(profileImageUri, new FirebaseCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    user.setProfileImageUrl(imageUrl);
                    updateUserData(user, callback);
                }
                @Override
                public void onFailure(String errorMessage) {
                    callback.onFailure("Lỗi khi tải ảnh lên: " + errorMessage);
                }
            });
        } else {
            updateUserData(user, callback);
        }
    }

    private void updateUserData(User user, FirebaseCallback callback) {
        firestoreHelper.updateUserProfile(user, new FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                // Cache lại user mới
                currentUser = user;

                // Đồng bộ displayName lên FirebaseAuth
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(user.getName())
                            .build();
                    firebaseUser.updateProfile(profileUpdates)
                            .addOnSuccessListener(aVoid -> {
                                callback.onSuccess("Hồ sơ đã được cập nhật thành công");
                            })
                            .addOnFailureListener(e -> {
                                callback.onSuccess("Hồ sơ đã cập nhật, nhưng lỗi cập nhật tên hiển thị");
                            });
                } else {
                    callback.onSuccess("Hồ sơ đã được cập nhật");
                }
            }
            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    private void uploadProfileImage(Uri imageUri, FirebaseCallback callback) {
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            callback.onFailure("Người dùng chưa đăng nhập");
            return;
        }
        CloudinaryManager.uploadImage(imageUri, "users", new CloudinaryManager.CloudinaryUploadCallback() {
            @Override public void onStart() {
                Log.d(TAG, "Starting Cloudinary upload");
            }
            @Override public void onProgress(long bytes, long totalBytes) {
                int progress = (int)((bytes * 100) / totalBytes);
                Log.d(TAG, "Upload progress: " + progress + "%");
            }
            @Override public void onSuccess(String imageUrl) {
                Log.d(TAG, "Image uploaded: " + imageUrl);
                callback.onSuccess(imageUrl);
            }
            @Override public void onError(String errorMessage) {
                Log.e(TAG, "Error uploading image: " + errorMessage);
                callback.onFailure(errorMessage);
            }
        });
    }

    public void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        // Xóa cache
        currentUser = null;
    }

    public void deleteAccount(FirebaseCallback callback) {
        firestoreHelper.deleteUserAccount(new FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                currentUser = null;
                callback.onSuccess(message);
            }
            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public boolean isUserLoggedIn() {
        return authHelper.isUserLoggedIn();
    }

    /**
     * Nếu muốn refresh dữ liệu từ server:
     */
    public void refreshUserData(FirebaseCallback callback) {
        firestoreHelper.getUserData(new FirebaseCallback() {
            @Override
            public void onUserDataReceived(User user) {
                currentUser = user;
                callback.onUserDataReceived(user);
            }
            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }
}
