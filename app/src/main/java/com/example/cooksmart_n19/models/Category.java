package com.example.cooksmart_n19.models;

import java.util.List;

public class Category{
    private List<Recipe> recipeList;
    private String titleCategory;

    public Category(List<Recipe> recipeList, String titleCategory) {
        this.recipeList = recipeList;
        this.titleCategory = titleCategory;
    }

    public List<Recipe> getRecipeList() {
        return recipeList;
    }

    public void setRecipeList(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    public String getTitleCategory() {
        return titleCategory;
    }

    public void setTitleCategory(String titleCategory) {
        this.titleCategory = titleCategory;
    }
}