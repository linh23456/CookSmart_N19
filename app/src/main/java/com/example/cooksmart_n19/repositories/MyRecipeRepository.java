package com.example.cooksmart_n19.repositories;

import android.util.Log;

import com.example.cooksmart_n19.models.CookingStep;
import com.example.cooksmart_n19.models.IngredientItem;
import com.example.cooksmart_n19.models.Recipe;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyRecipeRepository {
    private static final String TAG = "RecipeRepository";
    private static final String COLLECTION_RECIPE = "recipes";
    private final FirebaseFirestore db;

    public MyRecipeRepository() {
        db = FirebaseFirestore.getInstance();
        // Kích hoạt chế độ offline persistence
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    /**
     * Lưu một công thức mới hoặc cập nhật công thức hiện có
     */
    public void saveRecipe(String userId, Recipe recipe, List<IngredientItem> ingredients, List<CookingStep> steps, SaveMyRecipeCallback callback) {
        if (recipe.getRecipeId() == null || recipe.getRecipeId().isEmpty()) {
            // Tạo ID mới nếu recipeId chưa có
            String recipeId = db.collection(COLLECTION_RECIPE).document().getId();
            recipe.setRecipeId(recipeId);
            recipe.setAuthorId(userId);
            recipe.setCreatedAt(Timestamp.now());
            recipe.setUpdatedAt(Timestamp.now());
        }

        // Sử dụng batch để lưu dữ liệu đồng bộ
        WriteBatch batch = db.batch();

        // Lưu thông tin cơ bản của công thức
        batch.set(db.collection(COLLECTION_RECIPE).document(recipe.getRecipeId()), recipe);

        // Lưu danh sách nguyên liệu vào subcollection "ingredients"
        for (IngredientItem ingredient : ingredients) {
            if (ingredient.getIngredientId() == null || ingredient.getIngredientId().isEmpty()) {
                ingredient.setIngredientId(db.collection(COLLECTION_RECIPE)
                        .document(recipe.getRecipeId())
                        .collection("ingredients")
                        .document()
                        .getId());
            }
            batch.set(db.collection(COLLECTION_RECIPE)
                    .document(recipe.getRecipeId())
                    .collection("ingredients")
                    .document(ingredient.getIngredientId()), ingredient);
        }

        // Lưu danh sách bước thực hiện vào subcollection "steps"
        for (CookingStep step : steps) {
            if (step.getStepId() == null || step.getStepId().isEmpty()) {
                step.setStepId(db.collection(COLLECTION_RECIPE)
                        .document(recipe.getRecipeId())
                        .collection("steps")
                        .document()
                        .getId());
            }
            batch.set(db.collection(COLLECTION_RECIPE)
                    .document(recipe.getRecipeId())
                    .collection("steps")
                    .document(step.getStepId()), step);
        }

        // Thực hiện batch write
        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(recipe))
                .addOnFailureListener(error -> callback.onError(error.getMessage()));
    }

    /**
     * Lấy tất cả công thức của một người dùng
     */
    public void getAllMyRecipe(String userId, GetMRecipeCallback callback) {
        Query baseQuery = db.collection(COLLECTION_RECIPE)
                .whereEqualTo("authorId", userId);

        baseQuery.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipeList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Recipe recipe = document.toObject(Recipe.class);
                            if (recipe != null) {
                                recipe.setRecipeId(document.getId());
                                recipeList.add(recipe);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing recipe: " + e.getMessage(), e);
                        }
                    }
                    callback.onSuccess(recipeList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Xóa một công thức và tất cả subcollection của nó
     */
    public void deleteMyRecipe(String recipeId, DeleteMyRecipeCallback callback) {
        if (recipeId == null) {
            callback.onError("Recipe ID is null");
            return;
        }

        // Xóa subcollection "ingredients"
        db.collection(COLLECTION_RECIPE)
                .document(recipeId)
                .collection("ingredients")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    // Xóa subcollection "steps"
                    db.collection(COLLECTION_RECIPE)
                            .document(recipeId)
                            .collection("steps")
                            .get()
                            .addOnSuccessListener(stepSnapshots -> {
                                for (DocumentSnapshot stepDoc : stepSnapshots) {
                                    batch.delete(stepDoc.getReference());
                                }

                                // Xóa document chính của công thức
                                batch.delete(db.collection(COLLECTION_RECIPE).document(recipeId));

                                // Thực hiện batch delete
                                batch.commit()
                                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error deleting recipe: " + e.getMessage(), e);
                                            callback.onError("Failed to delete recipe: " + e.getMessage());
                                        });
                            })
                            .addOnFailureListener(e -> callback.onError("Failed to delete steps: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Failed to delete ingredients: " + e.getMessage()));
    }

    /**
     * Lấy một công thức theo ID (chỉ lấy thông tin cơ bản)
     */
    public void getRecipeById(String recipeId, GetSingleRecipeCallback callback) {
        if (recipeId == null) {
            callback.onFailure("Recipe ID is null");
            return;
        }

        db.collection(COLLECTION_RECIPE)
                .document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Recipe recipe = documentSnapshot.toObject(Recipe.class);
                        if (recipe != null) {
                            recipe.setRecipeId(documentSnapshot.getId());
                            callback.onSuccess(recipe);
                        } else {
                            callback.onFailure("Failed to parse recipe");
                        }
                    } else {
                        callback.onFailure("Recipe not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Failed to load recipe: " + e.getMessage()));
    }

    /**
     * Lấy danh sách nguyên liệu của một công thức
     */
    public void loadIngredients(String recipeId, OnIngredientsLoadedListener listener) {
        if (recipeId == null) {
            listener.onError(new Exception("Recipe ID is null"));
            return;
        }

        db.collection(COLLECTION_RECIPE)
                .document(recipeId)
                .collection("ingredients")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<IngredientItem> ingredients = queryDocumentSnapshots.toObjects(IngredientItem.class);
                    listener.onIngredientsLoaded(ingredients);
                })
                .addOnFailureListener(e -> listener.onError(e));
    }

    /**
     * Lấy danh sách bước thực hiện của một công thức
     */
    public void loadSteps(String recipeId, OnStepsLoadedListener listener) {
        if (recipeId == null) {
            listener.onError(new Exception("Recipe ID is null"));
            return;
        }

        db.collection(COLLECTION_RECIPE)
                .document(recipeId)
                .collection("steps")
                .orderBy("stepNumber")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CookingStep> steps = queryDocumentSnapshots.toObjects(CookingStep.class);
                    listener.onStepsLoaded(steps);
                })
                .addOnFailureListener(e -> listener.onError(e));
    }
    public void submitRating(String recipeId, String userId, float rating, OnRatingSubmittedListener listener) {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("rating", rating);

        db.collection("recipes").document(recipeId).collection("ratings").document(userId)
                .set(ratingData)
                .addOnSuccessListener(aVoid -> updateRecipeRating(recipeId, listener))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    private void updateRecipeRating(String recipeId, OnRatingSubmittedListener listener) {
        db.collection("recipes").document(recipeId).collection("ratings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRating = 0.0;
                    int ratingCount = queryDocumentSnapshots.size();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Double rating = document.getDouble("rating");
                        if (rating != null) {
                            totalRating += rating;
                        }
                    }

                    double averageRating = ratingCount > 0 ? totalRating / ratingCount : 0.0;

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("averageRating", averageRating);
                    updates.put("ratingCount", ratingCount);

                    db.collection("recipes").document(recipeId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> listener.onSuccess(averageRating, ratingCount))
                            .addOnFailureListener(e -> listener.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void checkIfUserRated(String recipeId, String userId, OnCheckUserRatedListener listener) {
        db.collection("recipes").document(recipeId).collection("ratings").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Float rating = documentSnapshot.getDouble("rating").floatValue();
                        listener.onUserRated(rating);
                    } else {
                        listener.onUserNotRated();
                    }
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // Các interface callback
    public interface GetMRecipeCallback {
        void onSuccess(List<Recipe> recipe);
        void onFailure(String errorMessage);
    }

    public interface SaveMyRecipeCallback {
        void onSuccess(Recipe recipe);
        void onError(String errorMessage);
    }

    public interface DeleteMyRecipeCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface GetSingleRecipeCallback {
        void onSuccess(Recipe recipe);
        void onFailure(String errorMessage);
    }

    public interface OnIngredientsLoadedListener {
        void onIngredientsLoaded(List<IngredientItem> ingredients);
        void onError(Exception e);
    }

    public interface OnStepsLoadedListener {
        void onStepsLoaded(List<CookingStep> steps);
        void onError(Exception e);
    }
    public interface OnRatingSubmittedListener {
        void onSuccess(double averageRating, int ratingCount);
        void onError(String errorMessage);
    }

    public interface OnCheckUserRatedListener {
        void onUserRated(float rating);
        void onUserNotRated();
        void onError(String errorMessage);
    }
}