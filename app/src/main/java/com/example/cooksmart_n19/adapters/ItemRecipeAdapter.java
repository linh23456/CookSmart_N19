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

import com.bumptech.glide.Glide;
import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.fragments.HomeFragment;
import com.example.cooksmart_n19.models.Recipe;

import java.util.List;

public class ItemRecipeAdapter extends RecyclerView.Adapter<ItemRecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;
    private final OnItemClickListener onLikeClickListener;
    private final OnItemClickListener onDetailClickListener;
    private final HomeFragment homeFragment;

    public interface OnItemClickListener {
        void onItemClick(Recipe recipe, int position);
    }

    public ItemRecipeAdapter(List<Recipe> recipes, OnItemClickListener onLikeClickListener, OnItemClickListener onDetailClickListener) {
        this.recipes = recipes;
        this.onLikeClickListener = onLikeClickListener;
        this.onDetailClickListener = onDetailClickListener;
        this.homeFragment = null;
    }

    public ItemRecipeAdapter(List<Recipe> recipes, OnItemClickListener onLikeClickListener, OnItemClickListener onDetailClickListener, HomeFragment homeFragment) {
        this.recipes = recipes;
        this.onLikeClickListener = onLikeClickListener;
        this.onDetailClickListener = onDetailClickListener;
        this.homeFragment = homeFragment;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.textViewTitle.setText(recipe.getTitle());
        holder.textViewDifficulty.setText("Độ khó: " + recipe.getDifficulty());
        holder.textViewCookingTime.setText("Thời gian: " + recipe.getCookingTime() + " phút");
        holder.textViewCost.setText("Chi phí: " + recipe.getCost() + " VNĐ");

        Glide.with(holder.itemView.getContext())
                .load(recipe.getImage())

                .thumbnail(0.25f)
                .placeholder(R.drawable.rice)
                .error(R.drawable.rice)
                .into(holder.imageViewRecipe);

        // Cập nhật trạng thái nút "Thích"
        if (homeFragment != null) {
            boolean isLiked = homeFragment.isRecipeLiked(recipe.getRecipeId());
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
        ImageButton buttonLike;
        Button buttonDetail;
        ImageView imageViewRecipe;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDifficulty = itemView.findViewById(R.id.textViewDifficulty);
            textViewCookingTime = itemView.findViewById(R.id.textViewCookingTime);
            textViewCost = itemView.findViewById(R.id.textViewCost);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            buttonDetail = itemView.findViewById(R.id.buttonViewDetail);
            imageViewRecipe = itemView.findViewById(R.id.imageViewRecipe);
        }
    }
}