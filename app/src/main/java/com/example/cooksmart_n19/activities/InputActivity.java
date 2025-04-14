package com.example.cooksmart_n19.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.ai.RecipeGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class InputActivity extends AppCompatActivity {
    private EditText edtIngredients;
    private Spinner spDiet;
    private RecipeGenerator generator;
    private ProgressDialog progressDialog;

    // Constants for intent extras
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_INGREDIENTS = "ingredients";
    private static final String EXTRA_STEPS = "steps";
    private static final String EXTRA_IMAGE = "image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        initializeViews();
        setupRecipeGenerator();
    }

    private void initializeViews() {
        edtIngredients = findViewById(R.id.edt_ingredients);
        spDiet = findViewById(R.id.sp_diet);
        Button btnGenerate = findViewById(R.id.btn_generate);
        btnGenerate.setOnClickListener(v -> validateAndGenerate());
    }

    private void setupRecipeGenerator() {
        String apiKey = getString(R.string.gemini_api_key); // Sửa lỗi chính tả "gemini"
        generator = new RecipeGenerator(this, apiKey);
    }

    private boolean isSpecialResponse(Map<String, Object> data) {
        return data.containsKey("raw_response") && data.get("raw_response") instanceof String;
    }

    private void handleSpecialResponse(Map<String, Object> data) {
        String rawResponse = (String) data.get("raw_response");
        switch (rawResponse.toUpperCase()) {
            case "OK":
                showToast("Thao tác thành công!");
                break;
            case "TUYỆT":
                new AlertDialog.Builder(this)
                        .setTitle("Thông báo đặc biệt")
                        .setMessage("Phản hồi không mong muốn: Tuyệt")
                        .setPositiveButton("OK", null)
                        .show();
                break;
            default:
                handleError("Phản hồi không xác định: " + rawResponse);
        }
    }
    private void validateAndGenerate() {
        String ingredientsStr = edtIngredients.getText().toString().trim();
        if (ingredientsStr.isEmpty()) {
            showToast("Vui lòng nhập nguyên liệu!");
            return;
        }

        List<String> ingredients = Arrays.asList(ingredientsStr.split(",\\s*"));
        showLoadingDialog();

        generator.generateRecipe(ingredients, new RecipeGenerator.RecipeCallback() {
            @Override
            public void onSuccess(Map<String, Object> recipeData) {
                runOnUiThread(() -> {
                    dismissLoadingDialog();
                    if (recipeData.containsKey("error")) {
                        handleError(recipeData.get("error").toString());
                    } else {
                        processRecipeResult(recipeData);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    dismissLoadingDialog();
                    handleError("Lỗi hệ thống: " + errorMessage);
                });
            }
        });
    }

    private void processRecipeResult(Map<String, Object> recipeData) {
        try {
            if (recipeData != null && isValidRecipe(recipeData)) {
                navigateToRecipeOverview(recipeData);
            } else {
                handleError("Dữ liệu công thức không hợp lệ");
            }
        } catch (Exception e) {
            handleError("Lỗi xử lý dữ liệu: " + e.getMessage());
        }
    }

    private boolean isValidRecipe(Map<String, Object> recipeData) {
        try {
            return validateList(recipeData.get("ingredients")) &&
                    validateList(recipeData.get("steps")) &&
                    recipeData.containsKey("title");
        } catch (ClassCastException e) {
            return false;
        }
    }

    private boolean validateList(Object obj) {
        return obj instanceof List && !((List<?>) obj).isEmpty();
    }

    private void navigateToRecipeOverview(Map<String, Object> recipeData) {
        Intent intent = new Intent(this, RecipeAIOverviewActivity.class);
        intent.putExtra(EXTRA_TITLE, (String) recipeData.get("title"));
        intent.putStringArrayListExtra(EXTRA_INGREDIENTS, new ArrayList<>((List<String>) recipeData.get("ingredients")));
        intent.putStringArrayListExtra(EXTRA_STEPS, new ArrayList<>((List<String>) recipeData.get("steps")));

        if (recipeData.containsKey("image") && recipeData.get("image") instanceof String) {
            intent.putExtra(EXTRA_IMAGE, (String) recipeData.get("image"));
        }

        startActivity(intent);
    }

    private void showLoadingDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Đang tạo công thức...");
            progressDialog.setCancelable(false);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void handleError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Lỗi")
                .setMessage(message + "\n\nVui lòng thử lại với nguyên liệu khác!")
                .setPositiveButton("Thử lại", (dialog, which) -> edtIngredients.requestFocus())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        dismissLoadingDialog();
        super.onDestroy();
    }
}