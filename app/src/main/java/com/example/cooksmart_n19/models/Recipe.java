package com.example.cooksmart_n19.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.List;


@IgnoreExtraProperties
public class Recipe {
    private String recipeId;
    private String title;
    private String description;
    private List<IngredientItem> ingredients = new ArrayList<>();
    private List<CookingStep> steps = new ArrayList<>();
    private String authorId;
    private String difficulty;
    private int cookingTime;

    private String image;

    @ServerTimestamp
    private Timestamp createdAt;
    @ServerTimestamp
    private Timestamp updatedAt;

    @IgnoreExtraProperties
    public static class IngredientItem {
        private String ingredientId;
        private double quantity;
        private String unit;

        public IngredientItem() {}
        // Getters & Setters
    }

    @IgnoreExtraProperties
    public static class CookingStep {
        private int stepNumber;
        private String instruction;
        private List<String> images;

        public CookingStep() {}
        // Getters & Setters
    }


}
