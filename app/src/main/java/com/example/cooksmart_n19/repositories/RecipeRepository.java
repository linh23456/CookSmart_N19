package com.example.cooksmart_n19.repositories;

import android.util.Log;

import com.example.cooksmart_n19.models.Recipe;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecipeRepository {
    private static final String TAG = "RecipeRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listenerRegistration;

    public interface RecipeCallback {
        void onSuccess(List<Recipe> recipes);
    }

    public void getFeaturedRecipes(RecipeCallback callback) {
        db.collection("recipes")
                .whereEqualTo("difficulty", "Dễ")
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setRecipeId(document.getId());
                        recipes.add(recipe);
                    }
                    Log.d(TAG, "Loaded featured recipes: " + recipes.size() + " items");
                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading featured recipes: " + e.getMessage());
                    callback.onSuccess(new ArrayList<>());
                });
    }

    public void getRecentRecipes(RecipeCallback callback) {
        db.collection("recipes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setRecipeId(document.getId());
                        recipes.add(recipe);
                    }
                    Log.d(TAG, "Loaded recent recipes: " + recipes.size() + " items");
                    callback.onSuccess(recipes);
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

        // Thực hiện truy vấn Firestore mà không có orderBy
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
                        recipes.sort((r1, r2) -> Integer.compare(r1.getCookingTime(), r2.getCookingTime()));
                    } else if (cookingTimeSort.equals("Thời gian giảm dần")) {
                        recipes.sort((r1, r2) -> Integer.compare(r2.getCookingTime(), r1.getCookingTime()));
                    }

                    // Sắp xếp theo giá (cost) trong ứng dụng
                    if (costSort.equals("Giá tăng dần")) {
                        recipes.sort((r1, r2) -> Double.compare(r1.getCost(), r2.getCost()));
                    } else if (costSort.equals("Giá giảm dần")) {
                        recipes.sort((r1, r2) -> Double.compare(r2.getCost(), r1.getCost()));
                    }

                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading recipes: " + e.getMessage());
                    callback.onSuccess(new ArrayList<>());
                });
    }

    public void removeListener() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}