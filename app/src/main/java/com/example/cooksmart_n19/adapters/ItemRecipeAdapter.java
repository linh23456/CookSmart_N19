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
import com.example.cooksmart_n19.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public class ItemRecipeAdapter extends RecyclerView.Adapter<ItemRecipeAdapter.ViewHolder> {
    private List<Recipe> recipes;
    private OnLikeClickListener onLikeClickListener;
    private OnViewDetailClickListener onViewDetailClickListener;

    public interface OnLikeClickListener {
        void onLikeClick(Recipe recipe, int position);
    }

    public interface OnViewDetailClickListener {
        void onViewDetailClick(Recipe recipe);
    }

    public ItemRecipeAdapter(List<Recipe> recipes, OnLikeClickListener onLikeClickListener, OnViewDetailClickListener onViewDetailClickListener) {
        this.recipes = recipes != null ? recipes : new ArrayList<>();
        this.onLikeClickListener = onLikeClickListener;
        this.onViewDetailClickListener = onViewDetailClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
        holder.buttonLike.setOnClickListener(v -> {
            if (onLikeClickListener != null) {
                onLikeClickListener.onLikeClick(recipe, position);
            }
        });
        holder.buttonViewDetail.setOnClickListener(v -> {
            if (onViewDetailClickListener != null) {
                onViewDetailClickListener.onViewDetailClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes.clear();
        if (newRecipes != null) {
            this.recipes.addAll(newRecipes);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewRecipe;
        TextView textViewTitle;
        TextView textViewCookingTime;
        ImageButton buttonLike;
        Button buttonViewDetail;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewRecipe = itemView.findViewById(R.id.imageViewRecipe);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewCookingTime = itemView.findViewById(R.id.textViewCookingTime);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            buttonViewDetail = itemView.findViewById(R.id.buttonViewDetail);
        }

        void bind(Recipe recipe) {
            textViewTitle.setText(recipe.getTitle());
            textViewCookingTime.setText("Thời gian: " + recipe.getCookingTime());
            Glide.with(itemView.getContext())
                    .load(recipe.getImage())
                    .placeholder(R.drawable.rice)
                    .error(R.drawable.rice)
                    .into(imageViewRecipe);
            buttonLike.setImageResource(recipe.isLiked() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        }
    }
}