
package com.example.cooksmart_n19.models;
public class Recipe{
    private String imageRecipeURL;
    private String title;

    private int timeCook;

    private String quantityLove;


    public Recipe(String imageRecipeURL, String title, int timeCook, String quantityLove) {
        this.imageRecipeURL = imageRecipeURL;
        this.title = title;
        this.timeCook = timeCook;
        this.quantityLove = quantityLove;
    }

    public String getImageRecipeURL() {
        return imageRecipeURL;
    }

    public void setImageRecipeURL(String imageRecipeURL) {
        this.imageRecipeURL = imageRecipeURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTimeCook() {
        return timeCook;
    }

    public void setTimeCook(int timeCook) {
        this.timeCook = timeCook;
    }

    public String getQuantityLove() {
        return quantityLove;
    }

    public void setQuantityLove(String quantityLove) {
        this.quantityLove = quantityLove;
    }
}