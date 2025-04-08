package com.example.cooksmart_n19.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    private List<Recipe> recipes;
    private OnLikeClickListener onLikeClickListener;
    private OnViewRecipeClickListener onViewRecipeClickListener;

    public interface OnLikeClickListener {
        void onLikeClick(Recipe recipe, int position);
    }

    public interface OnViewRecipeClickListener {
        void onViewRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(List<Recipe> recipes, OnLikeClickListener onLikeClickListener, OnViewRecipeClickListener onViewRecipeClickListener) {
        this.recipes = recipes != null ? recipes : new ArrayList<>();
        this.onLikeClickListener = onLikeClickListener;
        this.onViewRecipeClickListener = onViewRecipeClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);

        // Sự kiện nhấn nút Thích
        holder.imageViewLike.setOnClickListener(v -> {
            if (onLikeClickListener != null) {
                onLikeClickListener.onLikeClick(recipe, position);
            }
        });

        // Sự kiện nhấn nút Xem công thức
        holder.buttonViewRecipe.setOnClickListener(v -> {
            if (onViewRecipeClickListener != null) {
                onViewRecipeClickListener.onViewRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewRecipe;
        TextView textViewTitle;
        TextView textViewCookingTime;
        TextView textViewDifficulty;
        TextView textViewCost;
        ImageView imageViewLike;
        Button buttonViewRecipe;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewRecipe = itemView.findViewById(R.id.imageViewRecipe);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewCookingTime = itemView.findViewById(R.id.textViewCookingTime);
            textViewDifficulty = itemView.findViewById(R.id.textViewDifficulty);
            textViewCost = itemView.findViewById(R.id.textViewCost);
            imageViewLike = itemView.findViewById(R.id.imageViewLike);
            buttonViewRecipe = itemView.findViewById(R.id.buttonViewRecipe);
        }

        void bind(Recipe recipe) {
            textViewTitle.setText(recipe.getTitle());
            textViewCookingTime.setText("Thời gian: " + recipe.getCookingTime());
            textViewDifficulty.setText("Mức độ: " + recipe.getDifficulty());
            textViewCost.setText("Giá: " + String.format("%,.0f", recipe.getCost()) + " VNĐ");
            imageViewLike.setImageResource(recipe.isLiked() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);

            Glide.with(itemView.getContext())
                    .load(recipe.getImage())
                    .placeholder(R.drawable.banh_mi)
                    .error(R.drawable.banh_mi)
                    .into(imageViewRecipe);
        }
    }
}