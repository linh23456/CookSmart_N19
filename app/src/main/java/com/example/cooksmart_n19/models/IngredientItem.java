package com.example.cooksmart_n19.models;

import com.google.firebase.firestore.DocumentId;

public class IngredientItem {
    @DocumentId
    private String ingredientId;
    private String ingredientName;
    private long quantity;
    private String unit;

    public IngredientItem() {}

    public IngredientItem(String ingredientId, String ingredientName, long quantity, String unit) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(String ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "IngredientItem{" +
                "ingredientId='" + ingredientId + '\'' +
                ", ingredientName='" + ingredientName + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                '}';
    }
}