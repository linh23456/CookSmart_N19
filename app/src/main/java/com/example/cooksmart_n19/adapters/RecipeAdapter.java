package com.example.cooksmart_n19.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.models.Recipe;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder>{

    private List<Recipe> recipeList;
    private Context context;

    public RecipeAdapter(List<Recipe> recipeList, Context context) {
        this.recipeList = recipeList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_recipe_card, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe currentRecipe = recipeList.get(position);
        holder.txtTimeCook.setText(String.valueOf(currentRecipe.getTimeCook()) + " Min.");
        holder.txtQuantityLove.setText(currentRecipe.getQuantityLove());
        holder.txtTitleRecipe.setText(currentRecipe.getTitle());

        Picasso.get().load(currentRecipe.getImageRecipeURL()).into(holder.imageRecipe);
    }


    @Override
    public int getItemCount() {
        return recipeList != null ? recipeList.size() : 0; // Dynamically return size of the list
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageRecipe;
        private TextView txtTimeCook, txtTitleRecipe, txtQuantityLove;

        public ViewHolder(@NonNull View view) {
            super(view);
            // Khởi tạo các view
            this.imageRecipe =(ImageView) view.findViewById(R.id.imageViewRecipe);
            this.txtTimeCook =(TextView) view.findViewById(R.id.timeRecipe);
            this.txtTitleRecipe =(TextView) view.findViewById(R.id.titleRecipe);
            this.txtQuantityLove =(TextView) view.findViewById(R.id.likesRecipe);
        }
    }
}
