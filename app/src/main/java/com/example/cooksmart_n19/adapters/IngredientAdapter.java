package com.example.cooksmart_n19.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.models.Recipe;

import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {
    private List<Recipe.IngredientItem> ingredientItemList;
    private OnIngredientListener ingredientListener;
    public IngredientAdapter(List<Recipe.IngredientItem> list, OnIngredientListener listener){
        this.ingredientListener = listener;
        this.ingredientItemList = list;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Recipe.IngredientItem ingredient = ingredientItemList.get(position);
        holder.bind(ingredient);
    }

    @Override
    public int getItemCount() {
        return ingredientItemList.size();
    }

    public void updateIngredients(List<Recipe.IngredientItem> newIngredientList) {
        this.ingredientItemList = newIngredientList;
        notifyDataSetChanged();
    }

    public class IngredientViewHolder extends RecyclerView.ViewHolder{
        private TextView textViewIngredientName, textViewQuantity;
        public IngredientViewHolder(@NonNull View itemView){
            super(itemView);
            textViewIngredientName = itemView.findViewById(R.id.textViewIngredientName);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION && ingredientListener != null){
                    ingredientListener.onIngredientListener(position);
                }
            });
        }
        public void bind(Recipe.IngredientItem ingredient) {
            textViewIngredientName.setText(ingredient.getIngredientName());
            textViewQuantity.setText(String.format("%s %s", ingredient.getQuantity(), ingredient.getUnit()));
        }
    }
    public interface OnIngredientListener{
        void onIngredientListener(int position);
    }
}
