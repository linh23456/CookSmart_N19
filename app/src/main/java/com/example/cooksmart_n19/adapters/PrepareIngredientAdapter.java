package com.example.cooksmart_n19.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public class PrepareIngredientAdapter extends RecyclerView.Adapter<PrepareIngredientAdapter.ViewHolder> {

    private List<Recipe.IngredientItem> ingredientList;
    private List<Boolean> checkedStates; // Lưu trạng thái checkbox

    public PrepareIngredientAdapter(List<Recipe.IngredientItem> ingredientList) {
        this.ingredientList = ingredientList != null ? ingredientList : new ArrayList<>();
        this.checkedStates = new ArrayList<>();
        updateCheckedStates(); // Khởi tạo checkedStates
    }

    /**
     * Cập nhật danh sách nguyên liệu và trạng thái checkbox.
     *
     * @param newIngredients Danh sách nguyên liệu mới
     */
    public void updateIngredients(List<Recipe.IngredientItem> newIngredients) {
        this.ingredientList.clear();
        if (newIngredients != null) {
            this.ingredientList.addAll(newIngredients);
        }
        updateCheckedStates();
        notifyDataSetChanged();
    }

    /**
     * Đồng bộ danh sách checkedStates với ingredientList.
     */
    private void updateCheckedStates() {
        checkedStates.clear();
        for (int i = 0; i < ingredientList.size(); i++) {
            checkedStates.add(false); // Khởi tạo trạng thái checkbox là false
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prepare_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= ingredientList.size()) {
            return; // Bỏ qua nếu position không hợp lệ
        }

        Recipe.IngredientItem ingredient = ingredientList.get(position);
        holder.ingredientName.setText(ingredient.getIngredientName());
        holder.quantity.setText(String.valueOf(ingredient.getQuantity()));

        // Cập nhật trạng thái checkbox
        holder.checkBox.setChecked(checkedStates.get(position));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedStates.set(position, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return ingredientList != null ? ingredientList.size() : 0;
    }

    public boolean areAllIngredientsChecked() {
        if (checkedStates.isEmpty()) {
            return false;
        }
        for (Boolean isChecked : checkedStates) {
            if (!isChecked) {
                return false;
            }
        }
        return true;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientName;
        TextView quantity;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.ingredient_name);
            quantity = itemView.findViewById(R.id.quantity);
            checkBox = itemView.findViewById(R.id.checkbox_ingredient);
        }
    }
}