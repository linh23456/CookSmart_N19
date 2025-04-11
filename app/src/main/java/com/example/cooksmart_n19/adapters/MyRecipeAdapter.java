package com.example.cooksmart_n19.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.models.Recipe;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MyRecipeAdapter extends RecyclerView.Adapter<MyRecipeAdapter.MyRecipeViewHolder> {
    private List<Recipe> recipes;
    private OnRecipeClickListener onRecipeClickListener;
    private OnRecipeActionListener onRecipeActionListener;
    private final String currentUserId;

    public MyRecipeAdapter(List<Recipe> recipes, String currentUserId,
                           OnRecipeClickListener onRecipeClickListener,
                           OnRecipeActionListener onRecipeActionListener) {
        this.recipes = recipes;
        this.currentUserId = currentUserId;
        this.onRecipeClickListener = onRecipeClickListener;
        this.onRecipeActionListener = onRecipeActionListener;
    }

    @NonNull
    @Override
    public MyRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_recipe, parent, false);
        return new MyRecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public class MyRecipeViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewRecipe;
        private TextView textViewRecipeName, textViewRecipeDescription, textViewCookingTime, textViewDifficulty;
        private MaterialButton buttonEditRecipe, buttonDeleteRecipe;
        private LinearLayout layoutStaffActions;

        public MyRecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewRecipe = itemView.findViewById(R.id.imageViewRecipe);
            textViewRecipeName = itemView.findViewById(R.id.textViewRecipeName);
            textViewRecipeDescription = itemView.findViewById(R.id.textViewRecipeDescription);
            textViewCookingTime = itemView.findViewById(R.id.textViewCookingTime);
            textViewDifficulty = itemView.findViewById(R.id.textViewDifficulty);
            buttonEditRecipe = itemView.findViewById(R.id.buttonEditRecipe);
            buttonDeleteRecipe = itemView.findViewById(R.id.buttonDeleteRecipe);
            layoutStaffActions = itemView.findViewById(R.id.layoutStaffActions);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onRecipeClickListener.onRecipeClick(position);
                }
            });
        }

        public void bind(Recipe recipe) {
            Glide.with(itemView.getContext())
                    .load(recipe.getImage())
                    .placeholder(R.drawable.rice)
                    .error(R.drawable.rice)
                    .into(imageViewRecipe);

            textViewRecipeName.setText(recipe.getTitle());
            textViewRecipeDescription.setText(recipe.getDescription() != null ? recipe.getDescription() : "No description");
            textViewCookingTime.setText(String.valueOf(recipe.getCookingTime()) + " phút");
            textViewDifficulty.setText(recipe.getDifficulty());

                Log.d("MyRecipeAdapter", "Showing actions for recipe: " + recipe.getTitle());
                layoutStaffActions.setVisibility(View.VISIBLE);
                buttonEditRecipe.setOnClickListener(v -> {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        Log.d("MyRecipeAdapter", "Edit button clicked for position: " + adapterPosition);
                        onRecipeActionListener.onEditClick(adapterPosition);
                    }
                });
                buttonDeleteRecipe.setOnClickListener(v -> {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        Log.d("MyRecipeAdapter", "Delete button clicked for position: " + adapterPosition);
                        onRecipeActionListener.onDeleteClick(adapterPosition);
                    }
                });

        }
    }

    public interface OnRecipeClickListener {
        void onRecipeClick(int position);
    }

    public interface OnRecipeActionListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }
}