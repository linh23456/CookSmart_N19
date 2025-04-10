package com.example.cooksmart_n19.repositories;

import android.util.Log;

import com.example.cooksmart_n19.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteRecipeRepository {

    private static final String TAG = "FavoriteRecipeRepository";
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public FavoriteRecipeRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    // Định nghĩa RecipeCallback với onFailure để xử lý lỗi
    public interface RecipeCallback {
        void onSuccess(List<Recipe> recipes);
        void onFailure(String error);
    }

    public void getFavoriteRecipes(RecipeCallback callback) {
        // Kiểm tra xem người dùng có đăng nhập không
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // Lấy danh sách recipeId từ user_likes
        db.collection("user_likes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> likedRecipeIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String recipeId = document.getString("recipeId");
                        if (recipeId != null) {
                            likedRecipeIds.add(recipeId);
                        }
                    }

                    // Nếu không có công thức nào được thích, trả về danh sách rỗng
                    if (likedRecipeIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    // Lấy từng công thức từ recipes
                    List<Recipe> favoriteRecipes = new ArrayList<>();
                    int totalRecipes = likedRecipeIds.size();
                    final int[] completedCount = {0}; // Đếm số truy vấn đã hoàn thành

                    for (String recipeId : likedRecipeIds) {
                        db.collection("recipes")
                                .document(recipeId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        Recipe recipe = documentSnapshot.toObject(Recipe.class);
                                        if (recipe != null) {
                                            recipe.setRecipeId(documentSnapshot.getId());
                                            favoriteRecipes.add(recipe);
                                        }
                                    }
                                    // Tăng số truy vấn đã hoàn thành
                                    completedCount[0]++;
                                    // Nếu tất cả truy vấn đã hoàn thành, trả về kết quả
                                    if (completedCount[0] == totalRecipes) {
                                        Log.d(TAG, "Đã tải " + favoriteRecipes.size() + " công thức yêu thích");
                                        callback.onSuccess(favoriteRecipes);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Nếu có lỗi với một truy vấn, vẫn tiếp tục với các truy vấn khác
                                    completedCount[0]++;
                                    if (completedCount[0] == totalRecipes) {
                                        Log.d(TAG, "Đã tải " + favoriteRecipes.size() + " công thức yêu thích (có lỗi: " + e.getMessage() + ")");
                                        callback.onSuccess(favoriteRecipes);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải danh sách ID công thức đã thích: " + e.getMessage());
                    callback.onFailure("Không thể tải danh sách yêu thích: " + e.getMessage());
                });
    }

    public interface OnUnlikeListener {
        void onSuccess();
        void onFailure(String error);
    }
}