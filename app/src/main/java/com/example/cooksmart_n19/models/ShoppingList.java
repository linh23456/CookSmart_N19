package com.example.cooksmart_n19.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.List;

public class ShoppingList {
    private String listId;
    private String userId;
    private String title;
    private List<ShoppingItem> items = new ArrayList<>();
    @ServerTimestamp
    private Timestamp createdAt;
    @ServerTimestamp
    private Timestamp updatedAt;

    @IgnoreExtraProperties
    public static class ShoppingItem {
        private String ingredientId;
        private double quantity;
        private String unit;
        private boolean purchased;

        public ShoppingItem() {}
        // Getters & Setters
    }
}
