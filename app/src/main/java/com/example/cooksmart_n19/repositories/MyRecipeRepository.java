package com.example.cooksmart_n19.repositories;

import android.util.Log;

import com.example.cooksmart_n19.models.Recipe;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyRecipeRepository {
    private static final String TAG = "RecipeRepository";
    private static final String COLLECTION_RECIPE = "recipes";
    private final FirebaseFirestore db;
    public MyRecipeRepository(){
        db = FirebaseFirestore.getInstance();
    }
    public void saveRecipe(Recipe recipe, SaveMyRecipeCallback callback){
        if(recipe.getRecipeId() == null || recipe.getRecipeId().isEmpty()){
            String recipeId = db.collection(COLLECTION_RECIPE).document().getId();
            db.collection(COLLECTION_RECIPE)
                    .document(recipeId)
                    .set(recipe)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess(recipe);
                    })
                    .addOnFailureListener(errorMessage -> {
                        callback.onError(errorMessage.getMessage());
                    });
        }else{
            db.collection("recipes").document(recipe.getRecipeId())
                    .set(recipe)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess(recipe);
                    })
                    .addOnFailureListener(error -> {
                        callback.onError(error.getMessage());
                    });
        }
    }
    public void getAllMyRecipe(GetMRecipeCallback callback){
        db.collection(COLLECTION_RECIPE).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipelist = new ArrayList<>();
                    for(DocumentSnapshot document: queryDocumentSnapshots){
                        try{
                            Recipe recipe = document.toObject(Recipe.class);
                            recipe.setRecipeId(document.getId());
                            recipelist.add(recipe);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    callback.onSuccess(recipelist);
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }
    public void deleteMyRecipe(String recipeId, DeleteMyRecipeCallback callback){
        if (recipeId == null) {
            callback.onError("Recipe ID is null");
            return;
        }

        db.collection(COLLECTION_RECIPE)
                .document(recipeId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting recipe: " + e.getMessage(), e);
                    callback.onError("Failed to delete recipe: " + e.getMessage());
                });
    }
    public void getRecipeById(String recipeId, GetSingleRecipeCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
    public interface GetMRecipeCallback{
        void onSuccess(List<Recipe> recipe);
        void onFailure(String errorMessage);
    }
    public interface SaveMyRecipeCallback{
        void onSuccess(Recipe recipe);
        void onError(String errorMessage);
    }
    public interface DeleteMyRecipeCallback{
        void onSuccess();
        void onError(String errorMessage);
    }
    public interface GetSingleRecipeCallback {
        void onSuccess(Recipe recipe);
        void onFailure(String errorMessage);
    }
}
