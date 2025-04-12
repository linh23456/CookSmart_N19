package com.example.cooksmart_n19.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.*;

@IgnoreExtraProperties
public class User {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private List<String> favoriteRecipeIds = new ArrayList<>();
    @ServerTimestamp
    private Timestamp createdAt;
    @ServerTimestamp
    private Timestamp updatedAt;

    public User() {
    }

    public User(String userId, String name, String email, List<String> favoriteRecipeIds, Timestamp createdAt, Timestamp updatedAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.favoriteRecipeIds = favoriteRecipeIds != null ? favoriteRecipeIds : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public User(String name, String email) {
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public List<String> getFavoriteRecipeIds() {
        return favoriteRecipeIds;
    }



    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setFavoriteRecipeIds(List<String> favoriteRecipeIds) {
        this.favoriteRecipeIds = favoriteRecipeIds;
    }


    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Object> toFirestoreMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("name", name);
        map.put("email", email);
        map.put("phoneNumber", phoneNumber);
        map.put("profileImageUrl", profileImageUrl);
        map.put("favoriteRecipeIds", favoriteRecipeIds);
        map.put("createdAt", FieldValue.serverTimestamp());
        map.put("updatedAt", FieldValue.serverTimestamp());
        return map;
    }

    // Sửa phương thức fromFirestore
    public static User fromFirestore(DocumentSnapshot doc) {
        if (doc.exists() && doc.getData() != null) {
            return new User(
                    doc.getId(),
                    doc.getString("name"),
                    doc.getString("email"),
                    (List<String>) doc.get("favoriteRecipeIds"),
                    doc.getTimestamp("createdAt"),
                    doc.getTimestamp("updatedAt")
            );
        }
        return null;
    }
}
