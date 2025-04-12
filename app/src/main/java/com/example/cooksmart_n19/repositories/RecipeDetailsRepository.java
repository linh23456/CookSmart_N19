package com.example.cooksmart_n19.repositories;

import android.util.Log;

import com.example.cooksmart_n19.models.Recipe;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class RecipeDetailsRepository {

    private FirebaseFirestore db;
    private static final String TAG = "RecipeDetailsRepo";
    private static RecipeDetailsRepository instance;

    // Singleton pattern
    public static synchronized RecipeDetailsRepository getInstance() {
        if (instance == null) {
            instance = new RecipeDetailsRepository();
        }
        return instance;
    }

    public RecipeDetailsRepository() {
        try {
            db = FirebaseFirestore.getInstance();
            Log.d(TAG, "FirebaseFirestore initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing FirebaseFirestore: " + e.getMessage());
        }
    }

    public interface OnRecipeDetailsListener {
        void onSuccess(Recipe recipe);
        void onError(String error);
    }

    public void getRecipeDetails(String recipeId, OnRecipeDetailsListener listener) {
        if (db == null) {
            Log.e(TAG, "FirebaseFirestore is null, cannot proceed with query");
            listener.onError("Firestore không được khởi tạo");
            return;
        }

        Log.d(TAG, "getRecipeDetails called with recipeId: " + recipeId);
        db.collection("recipes").document(recipeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "Firestore onSuccess called");
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Document exists, converting to Recipe");
                        Recipe recipe = documentSnapshot.toObject(Recipe.class);
                        if (recipe != null) {
                            Log.d(TAG, "Recipe loaded: " + recipe.toString());
                            listener.onSuccess(recipe);
                        } else {
                            Log.e(TAG, "Failed to convert document to Recipe object");
                            listener.onError("Không thể chuyển đổi dữ liệu công thức");
                        }
                    } else {
                        Log.e(TAG, "Document does not exist for recipeId: " + recipeId);
                        listener.onError("Công thức không tồn tại");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore error: " + e.getMessage());
                    listener.onError("Lỗi Firestore: " + e.getMessage());
                });
        Log.d(TAG, "Firestore query sent, waiting for response...");
    }
}