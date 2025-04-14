package com.example.cooksmart_n19.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.activities.RecipeAIListActivity;

import java.util.List;

public class RecipeAIAdapter extends RecyclerView.Adapter<RecipeAIAdapter.ViewHolder> {
    private final List<RecipeAIListActivity.Recipe> recipes;
    private final OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onStartCookingClick(RecipeAIListActivity.Recipe recipe);
    }

    public RecipeAIAdapter(List<RecipeAIListActivity.Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipeai, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeAIListActivity.Recipe recipe = recipes.get(position);

        // Hiển thị số thứ tự
        holder.tvRecipeNumber.setText("Món " + (position + 1));

        // Load hình ảnh
        if (!recipe.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(recipe.getImageUrl())
                    .into(holder.ivRecipe);
        }

        // Animation
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(
                holder.itemView.getContext(),
                R.anim.slide_in
        ));

        // Xử lý click
        holder.btnStartCooking.setOnClickListener(v -> {
            // Thêm hiệu ứng click
            v.startAnimation(AnimationUtils.loadAnimation(
                    v.getContext(),
                    R.anim.button_click
            ));
            listener.onStartCookingClick(recipe);
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRecipe;
        TextView tvRecipeNumber, tvRecipeName, tvCookingTime, tvStepsCount;
        Button btnStartCooking;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipe = itemView.findViewById(R.id.iv_recipe);
            tvRecipeNumber = itemView.findViewById(R.id.tv_recipe_number); // Thêm dòng này
            tvRecipeName = itemView.findViewById(R.id.tv_recipe_name);
            tvCookingTime = itemView.findViewById(R.id.tv_cooking_time);
            tvStepsCount = itemView.findViewById(R.id.tv_steps_count);
            btnStartCooking = itemView.findViewById(R.id.btn_start_cooking);
        }
    }

    public static class Recipe {
        private String name;
        private String cookingTime;
        private List<String> steps;
        private String imageUrl;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCookingTime() {
            return cookingTime;
        }

        public void setCookingTime(String cookingTime) {
            this.cookingTime = cookingTime;
        }

        public List<String> getSteps() {
            return steps;
        }

        public void setSteps(List<String> steps) {
            this.steps = steps;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
