package com.example.cooksmart_n19.repositories;

import android.util.Log;

import com.example.cooksmart_n19.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeRepository {
    private static final String TAG = "RecipeRepository";
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private ListenerRegistration listenerRegistration;

    public RecipeRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public interface RecipeCallback {
        void onSuccess(List<Recipe> recipes);
    }

    public interface OnToggleLikeListener {
        void onSuccess(boolean isLiked);
        void onFailure(String error);
    }

    public interface OnRecipeDetailsListener {
        void onSuccess(Recipe recipe);
        void onFailure(String error);
    }

    public void getFeaturedRecipes(RecipeCallback callback) {
        Query query = db.collection("recipes")
                .whereEqualTo("difficulty", "Dễ")
                .limit(5);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setRecipeId(document.getId());
                        recipes.add(recipe);
                    }
                    Log.d(TAG, "Loaded featured recipes: " + recipes.size() + " items");

                    // Kiểm tra trạng thái "thích" nếu người dùng đã đăng nhập
                    String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
                    if (userId != null) {
                        checkUserLikes(userId, recipes, callback);
                    } else {
                        callback.onSuccess(recipes);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading featured recipes: " + e.getMessage());
                    callback.onSuccess(new ArrayList<>());
                });
    }

    public void getRecentRecipes(RecipeCallback callback) {
        Query query = db.collection("recipes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setRecipeId(document.getId());
                        recipes.add(recipe);
                    }
                    Log.d(TAG, "Loaded recent recipes: " + recipes.size() + " items");

                    // Kiểm tra trạng thái "thích" nếu người dùng đã đăng nhập
                    String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
                    if (userId != null) {
                        checkUserLikes(userId, recipes, callback);
                    } else {
                        callback.onSuccess(recipes);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading recent recipes: " + e.getMessage());
                    callback.onSuccess(new ArrayList<>());
                });
    }
    public void searchRecipesByKeyword(String query, String cookingTimeSort, String difficultyFilter, String costSort, RecipeCallback callback) {
        Query baseQuery = db.collection("recipes")
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff");

        // Lọc theo mức độ khó (difficulty)
        if (!difficultyFilter.equals("Tất cả")) {
            baseQuery = baseQuery.whereEqualTo("difficulty", difficultyFilter);
        }

        baseQuery.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setRecipeId(document.getId());
                        recipes.add(recipe);
                    }
                    Log.d(TAG, "Loaded recipes for query '" + query + "': " + recipes.size() + " items");

                    // Sắp xếp theo thời gian nấu (cookingTimeInMinutes) trong ứng dụng
                    if (cookingTimeSort.equals("Thời gian tăng dần")) {
                        recipes.sort((r1, r2) -> Double.compare(r1.getCookingTime(), r2.getCookingTime()));
                    } else if (cookingTimeSort.equals("Thời gian giảm dần")) {
                        recipes.sort((r1, r2) -> Double.compare(r2.getCookingTime(), r1.getCookingTime()));
                    }

                    // Sắp xếp theo giá (cost) trong ứng dụng
                    if (costSort.equals("Giá tăng dần")) {
                        recipes.sort((r1, r2) -> Double.compare(r1.getCost(), r2.getCost()));
                    } else if (costSort.equals("Giá giảm dần")) {
                        recipes.sort((r1, r2) -> Double.compare(r2.getCost(), r1.getCost()));
                    }
                    // Kiểm tra trạng thái "thích" nếu người dùng đã đăng nhập
                    String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
                    if (userId != null) {
                        checkUserLikes(userId, recipes, callback);
                    } else {
                        callback.onSuccess(recipes);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading recipes: " + e.getMessage());
                    callback.onSuccess(new ArrayList<>());
                });
    }
    private void checkUserLikes(String userId, List<Recipe> recipes, RecipeCallback callback) {
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

                    for (Recipe recipe : recipes) {
                        recipe.setLiked(likedRecipeIds.contains(recipe.getRecipeId()));
                    }

                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user likes: " + e.getMessage());
                    callback.onSuccess(recipes);
                });
    }

    public void toggleLike(String recipeId, boolean isCurrentlyLiked, OnToggleLikeListener listener) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            listener.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Toggling like for user: " + userId + ", recipe: " + recipeId);
        db.collection("user_likes")
                .whereEqualTo("userId", userId)
                .whereEqualTo("recipeId", recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Tài liệu đã tồn tại, xóa nó (bỏ thích)
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            db.collection("user_likes")
                                    .document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Successfully unliked recipe: " + recipeId);
                                        listener.onSuccess(false);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to unlike recipe: " + e.getMessage());
                                        listener.onFailure(e.getMessage());
                                    });
                        }
                    } else {
                        // Tài liệu chưa tồn tại, thêm mới (thích)
                        Map<String, Object> likeData = new HashMap<>();
                        likeData.put("userId", userId);
                        likeData.put("recipeId", recipeId);
                        likeData.put("timestamp", System.currentTimeMillis());

                        db.collection("user_likes")
                                .add(likeData)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "Successfully liked recipe: " + recipeId);
                                    listener.onSuccess(true);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to like recipe: " + e.getMessage());
                                    listener.onFailure(e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to query user_likes: " + e.getMessage());
                    listener.onFailure(e.getMessage());
                });
    }

    public void removeListener() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }
}