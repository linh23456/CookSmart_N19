package com.example.cooksmart_n19.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

@IgnoreExtraProperties
public class Recipe {
    private String recipeId;
    private String authorId;
    private String title;
    private String description;
    private long cost;
    private int cookingTime;
    private String difficulty;
    private String image;
    private double averageRating;
    private int ratingCount;
    private boolean isLiked;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;

    // Constructor mặc định (yêu cầu bởi Firestore)
    public Recipe() {
        this.averageRating = 0.0;
        this.ratingCount = 0;
        this.isLiked = false;
    }

    public Recipe(String recipeId, String authorId, String title, String description, long cost, int cookingTime, String difficulty, String image, double averageRating, int ratingCount, Date createdAt, Date updatedAt) {
        this.recipeId = recipeId;
        this.authorId = authorId;
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.cookingTime = cookingTime;
        this.difficulty = difficulty;
        this.image = image;
        this.averageRating = 0.0;
        this.ratingCount = 0;
        this.isLiked = false;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters và setters
    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
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

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public Date getCreateAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createAt) {
        this.createdAt = createAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "recipeId='" + recipeId + '\'' +
                ", authorId='" + authorId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", cost=" + cost +
                ", cookingTime=" + cookingTime +
                ", difficulty='" + difficulty + '\'' +
                ", image='" + image + '\'' +
                ", averageRating=" + averageRating +
                ", ratingCount=" + ratingCount +
                '}';
    }
}