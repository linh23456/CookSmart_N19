package com.example.cooksmart_n19.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.fragments.ExploreFragment;
import com.example.cooksmart_n19.fragments.HomeFragment;
import com.example.cooksmart_n19.models.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;
    private final OnItemClickListener onLikeClickListener;
    private final OnItemClickListener onDetailClickListener;
    private final Object fragment; // Có thể là HomeFragment hoặc ExploreFragment

    public interface OnItemClickListener {
        void onItemClick(Recipe recipe, int position);
    }

    public RecipeAdapter(List<Recipe> recipes, OnItemClickListener onLikeClickListener, OnItemClickListener onDetailClickListener) {
        this.recipes = recipes;
        this.onLikeClickListener = onLikeClickListener;
        this.onDetailClickListener = onDetailClickListener;
        this.fragment = null;
    }

    public RecipeAdapter(List<Recipe> recipes, OnItemClickListener onLikeClickListener, OnItemClickListener onDetailClickListener, Object fragment) {
        this.recipes = recipes;
        this.onLikeClickListener = onLikeClickListener;
        this.onDetailClickListener = onDetailClickListener;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.textViewTitle.setText(recipe.getTitle());
        holder.textViewDifficulty.setText("Độ khó: " + recipe.getDifficulty());
        holder.textViewCookingTime.setText("Thời gian: " + recipe.getCookingTime() + " phút");
        holder.textViewCost.setText("Chi phí: " + recipe.getCost() + " VNĐ");

        if (fragment != null) {
            boolean isLiked = false;
            if (fragment instanceof HomeFragment) {
                isLiked = ((HomeFragment) fragment).isRecipeLiked(recipe.getRecipeId());
            } else if (fragment instanceof ExploreFragment) {
                isLiked = ((ExploreFragment) fragment).isRecipeLiked(recipe.getRecipeId());
            }
            holder.buttonLike.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        }

        holder.buttonLike.setOnClickListener(v -> onLikeClickListener.onItemClick(recipe, position));
        holder.buttonDetail.setOnClickListener(v -> onDetailClickListener.onItemClick(recipe, position));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDifficulty;
        TextView textViewCookingTime;
        TextView textViewCost;
        ImageView buttonLike;
        Button buttonDetail;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDifficulty = itemView.findViewById(R.id.textViewDifficulty);
            textViewCookingTime = itemView.findViewById(R.id.textViewCookingTime);
            textViewCost = itemView.findViewById(R.id.textViewCost);
            buttonLike = itemView.findViewById(R.id.imageViewLike);
            buttonDetail = itemView.findViewById(R.id.buttonViewRecipe);
        }
    }
}