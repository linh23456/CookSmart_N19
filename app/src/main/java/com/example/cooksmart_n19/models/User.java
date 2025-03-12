package com.example.cooksmart_n19.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;
import java.util.*;


public class User {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private List<String> favoriteRecipeIds = new ArrayList<>();
    private List<String> shoppingListIds = new ArrayList<>();
    private Map<String, Object> preferences = new HashMap<>();
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
