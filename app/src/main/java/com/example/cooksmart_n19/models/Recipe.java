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
    private double cost;
    private boolean isLiked;

    @ServerTimestamp
    private Timestamp createdAt;
    @ServerTimestamp
    private Timestamp updatedAt;

    public Recipe() {
    }

    public Recipe(String recipeId, String title, String description, List<IngredientItem> ingredients, List<CookingStep> steps, String authorId, String difficulty, int cookingTime, String image, double cost, Timestamp createdAt, Timestamp updatedAt) {
        this.recipeId = recipeId;
        this.title = title;
        this.description = description;
        this.ingredients = ingredients;
        this.steps = steps;
        this.authorId = authorId;
        this.difficulty = difficulty;
        this.cookingTime = cookingTime;
        this.image = image;
        this.cost = cost;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isLiked = false;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<IngredientItem> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientItem> ingredients) {
        this.ingredients = ingredients;
    }

    public List<CookingStep> getSteps() {
        return steps;
    }

    public void setSteps(List<CookingStep> steps) {
        this.steps = steps;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @IgnoreExtraProperties
    public static class IngredientItem {
        private String ingredientName;
        private double quantity;
        private String unit;

        public IngredientItem() {}

        public IngredientItem(String ingredientName, double quantity, String unit) {
            this.ingredientName = ingredientName;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getIngredientName() {
            return ingredientName;
        }

        public void setIngredientName(String ingredientName) {
            this.ingredientName = ingredientName;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }

    @IgnoreExtraProperties
    public static class CookingStep {
        private int stepNumber;
        private String instruction;
        private String images;

        public CookingStep() {}

        public CookingStep(int stepNumber, String instruction, String images) {
            this.stepNumber = stepNumber;
            this.instruction = instruction;
            this.images = images;
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public void setStepNumber(int stepNumber) {
            this.stepNumber = stepNumber;
        }

        public String getInstruction() {
            return instruction;
        }

        public void setInstruction(String instruction) {
            this.instruction = instruction;
        }

        public String getImages() {
            return images;
        }

        public void setImages(String images) {
            this.images = images;
        }

        @Override
        public String toString() {
            return "CookingStep{" +
                    "stepNumber=" + stepNumber +
                    ", instruction='" + instruction + '\'' +
                    ", images='" + images + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "recipeId='" + recipeId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", ingredients=" + ingredients +
                ", steps=" + steps +
                ", authorId='" + authorId + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", cookingTime=" + cookingTime +
                ", image='" + image + '\'' +
                ", cost=" + cost +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
