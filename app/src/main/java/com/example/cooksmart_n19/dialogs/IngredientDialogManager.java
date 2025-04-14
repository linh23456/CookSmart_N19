package com.example.cooksmart_n19.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.models.IngredientItem;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class IngredientDialogManager {
    private final Context context;
    private final List<IngredientItem> ingredientItemList;
    private final Runnable onUpdateCallback;

    public IngredientDialogManager(Context context, List<IngredientItem> ingredientItemList, Runnable onUpdateCallback) {
        this.context = context;
        this.ingredientItemList = ingredientItemList;
        this.onUpdateCallback = onUpdateCallback;
    }

    public void showAddEditIngredientDialog(int position) {
        boolean isEdit = position != -1;
        IngredientItem ingredientItem = isEdit ? ingredientItemList.get(position) : new IngredientItem();

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_ingredient, null);
        TextInputEditText editTextIngredientName = dialogView.findViewById(R.id.editTextIngredientName);
        TextInputEditText editTextQuantity = dialogView.findViewById(R.id.editTextQuantity);
        TextInputEditText editTextUnit = dialogView.findViewById(R.id.editTextUnit);

        if (isEdit) {
            editTextIngredientName.setText(ingredientItem.getIngredientName());
            editTextQuantity.setText(String.valueOf(ingredientItem.getQuantity()));
            editTextUnit.setText(ingredientItem.getUnit());
        }

        new AlertDialog.Builder(context)
                .setTitle(isEdit ? "Sửa thông tin nguyên liệu" : "Thêm nguyên liệu")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Lưu" : "Thêm", (dialog, which) -> {
                    String ingredientName = editTextIngredientName.getText().toString().trim();
                    String quantityIngredient = editTextQuantity.getText().toString().trim();
                    String unit = editTextUnit.getText().toString().trim();

                    if (ingredientName.isEmpty() || quantityIngredient.isEmpty() || unit.isEmpty()) {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long quantity;
                    try {
                        quantity = Long.parseLong(quantityIngredient);
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Số lượng phải là số", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ingredientItem.setIngredientName(ingredientName);
                    ingredientItem.setQuantity(quantity);
                    ingredientItem.setUnit(unit);

                    if (isEdit) {
                        ingredientItemList.set(position, ingredientItem);
                    } else {
                        ingredientItemList.add(ingredientItem);
                    }

                    onUpdateCallback.run();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}