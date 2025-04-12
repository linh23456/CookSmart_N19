package com.example.cooksmart_n19.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.models.IngredientItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrepareIngredientAdapter extends RecyclerView.Adapter<PrepareIngredientAdapter.IngredientViewHolder> {

    private List<IngredientItem> ingredientList;
    private Map<String, Boolean> checkedStates; // Lưu trạng thái của CheckBox
    private static final String TAG = "PrepareIngredientAdapter";

    public PrepareIngredientAdapter(List<IngredientItem> ingredientList) {
        this.ingredientList = ingredientList != null ? ingredientList : new ArrayList<>();
        this.checkedStates = new HashMap<>();
        // Khởi tạo trạng thái ban đầu cho tất cả nguyên liệu là chưa đánh dấu
        for (IngredientItem ingredient : this.ingredientList) {
            checkedStates.put(ingredient.getIngredientId(), false);
        }
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prepare_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        IngredientItem ingredient = ingredientList.get(position);
        Log.d(TAG, "Binding ingredient at position " + position + ": name=" + ingredient.getIngredientName() + ", quantity=" + ingredient.getQuantity());
        holder.bind(ingredient);
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    public void updateIngredients(List<IngredientItem> newIngredients) {
        this.ingredientList = newIngredients;
        // Khởi tạo trạng thái ban đầu cho các nguyên liệu mới
        for (IngredientItem ingredient : this.ingredientList) {
            checkedStates.put(ingredient.getIngredientId(), false);
        }
        Log.d(TAG, "Updated ingredient list: " + ingredientList.toString());
        notifyDataSetChanged(); // Thông báo cho RecyclerView rằng dữ liệu đã thay đổi
    }

    public boolean areAllIngredientsChecked() {
        for (IngredientItem ingredient : ingredientList) {
            Boolean isChecked = checkedStates.get(ingredient.getIngredientId());
            if (isChecked == null || !isChecked) {
                return false;
            }
        }
        return true;
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder {
        private TextView ingredientName;
        private TextView quantity;
        private CheckBox checkBox;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_ingredient);
            ingredientName = itemView.findViewById(R.id.ingredient_name);
            quantity = itemView.findViewById(R.id.quantity);
        }

        public void bind(IngredientItem ingredient) {
            // Hiển thị tên nguyên liệu, nếu null thì hiển thị mặc định
            ingredientName.setText(ingredient.getIngredientName() != null ? ingredient.getIngredientName() : "Không có tên");
            // Hiển thị số lượng, nếu null thì hiển thị mặc định
            quantity.setText(String.valueOf(ingredient.getQuantity()) != null ? String.valueOf(ingredient.getQuantity()) + " " + ingredient.getUnit() : "N/A");
            // Thiết lập trạng thái checkbox từ checkedStates
            Boolean isChecked = checkedStates.get(ingredient.getIngredientId());
            checkBox.setChecked(isChecked != null && isChecked);
            checkBox.setOnCheckedChangeListener((buttonView, isCheckedState) -> {
                // Cập nhật trạng thái trong checkedStates
                checkedStates.put(ingredient.getIngredientId(), isCheckedState);
            });
        }
    }
}