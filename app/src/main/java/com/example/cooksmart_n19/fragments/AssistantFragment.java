package com.example.cooksmart_n19.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.ai.RecipeGenerator;
import com.example.cooksmart_n19.activities.RecipeAIOverviewActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AssistantFragment extends Fragment {
    private EditText edtIngredients;
    private Spinner spDiet;
    private RecipeGenerator generator;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assistant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        initRecipeGenerator();
    }

    private void setupViews(View rootView) {
        edtIngredients = rootView.findViewById(R.id.edt_ingredients);
        spDiet = rootView.findViewById(R.id.sp_diet);
        Button btnGenerate = rootView.findViewById(R.id.btn_generate);

        btnGenerate.setOnClickListener(v -> handleGenerateRecipe());
    }

    private void initRecipeGenerator() {
        String apiKey = getString(R.string.gemini_api_key);
        generator = new RecipeGenerator(requireContext(), apiKey);
    }

    private void handleGenerateRecipe() {
        String ingredients = edtIngredients.getText().toString().trim();
        if (ingredients.isEmpty()) {
            showToast("Vui lòng nhập nguyên liệu!");
            return;
        }

        showLoadingDialog();
        List<String> ingredientList = Arrays.asList(ingredients.split(",\\s*"));

        generator.generateRecipe(ingredientList, new RecipeGenerator.RecipeCallback() {
            @Override
            public void onSuccess(Map<String, Object> recipeData) {
                requireActivity().runOnUiThread(() -> {
                    dismissLoadingDialog();
                    processRecipeData(recipeData);
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    dismissLoadingDialog();
                    handleError("Lỗi: " + error);
                });
            }
        });
    }

    private void processRecipeData(Map<String, Object> recipeData) {
        if (isValidRecipe(recipeData)) {
            navigateToRecipeOverview(recipeData);
        } else {
            handleError("Dữ liệu không hợp lệ");
        }
    }

    private boolean isValidRecipe(Map<String, Object> data) {
        return data.containsKey("title") &&
                data.containsKey("ingredients") &&
                data.containsKey("steps");
    }

    private void navigateToRecipeOverview(Map<String, Object> data) {
        Intent intent = new Intent(requireContext(), RecipeAIOverviewActivity.class);
        intent.putExtra("title", (String) data.get("title"));
        intent.putStringArrayListExtra("ingredients", new ArrayList<>((List<String>) data.get("ingredients")));
        intent.putStringArrayListExtra("steps", new ArrayList<>((List<String>) data.get("steps")));

        if (data.containsKey("image")) {
            intent.putExtra("image", (String) data.get("image"));
        }

        startActivity(intent);
    }

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Đang tạo công thức...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void handleError(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        dismissLoadingDialog();
        super.onDestroyView();
    }
}