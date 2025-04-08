package com.example.cooksmart_n19.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.activities.RecipeDetailActivity;
import com.example.cooksmart_n19.adapters.RecipeAdapter;
import com.example.cooksmart_n19.models.Recipe;
import com.example.cooksmart_n19.repositories.RecipeRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private EditText editTextSearch;
    private ImageButton buttonFilter;
    private RecyclerView recyclerViewSearchResults;
    private RecipeAdapter recipeAdapter;
    private RecipeRepository recipeRepository;
    private List<Recipe> allRecipes;
    private String currentQuery = "";
    private String currentDifficultyFilter = "Tất cả";
    private String currentCookingTimeSort = "Mặc định";
    private String currentCostSort = "Mặc định";
    private boolean isSearching = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        editTextSearch = view.findViewById(R.id.editTextSearch);
        buttonFilter = view.findViewById(R.id.buttonFilter);
        recyclerViewSearchResults = view.findViewById(R.id.recyclerViewSearchResults);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recipeRepository = new RecipeRepository();
        allRecipes = new ArrayList<>();

        recipeAdapter = new RecipeAdapter(
                allRecipes,
                this::toggleLike,
                this::navigateToRecipeDetail
        );
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSearchResults.setAdapter(recipeAdapter);

        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                String query = editTextSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    Log.d("ExploreFragment", "Search triggered with query: " + query);
                    currentQuery = query;
                    searchRecipes(query);
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
                    }
                    currentQuery = "";
                    allRecipes.clear();
                    recipeAdapter.updateRecipes(allRecipes);
                }
                return true;
            }
            return false;
        });

        // Sự kiện cho nút Lọc
        buttonFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        // Tạo dialog
        Dialog filterDialog = new Dialog(requireContext());
        filterDialog.setContentView(R.layout.dialog_filter);

        // Đặt kích thước cho dialog
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(filterDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        filterDialog.getWindow().setAttributes(params);

        // Khởi tạo các Spinner trong dialog
        Spinner spinnerCookingTime = filterDialog.findViewById(R.id.spinnerCookingTime);
        Spinner spinnerDifficulty = filterDialog.findViewById(R.id.spinnerDifficulty);
        Spinner spinnerCost = filterDialog.findViewById(R.id.spinnerCost);
        Button buttonApply = filterDialog.findViewById(R.id.buttonApply);
        Button buttonCancel = filterDialog.findViewById(R.id.buttonCancel);

        // Thiết lập Spinner cho thời gian nấu
        ArrayAdapter<CharSequence> cookingTimeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.cooking_time_options,
                android.R.layout.simple_spinner_item
        );
        cookingTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCookingTime.setAdapter(cookingTimeAdapter);
        // Đặt giá trị hiện tại
        int cookingTimePosition = cookingTimeAdapter.getPosition(currentCookingTimeSort);
        spinnerCookingTime.setSelection(cookingTimePosition);

        // Thiết lập Spinner cho mức độ khó
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.difficulty_options,
                android.R.layout.simple_spinner_item
        );
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);
        int difficultyPosition = difficultyAdapter.getPosition(currentDifficultyFilter);
        spinnerDifficulty.setSelection(difficultyPosition);

        // Thiết lập Spinner cho giá
        ArrayAdapter<CharSequence> costAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.cost_options,
                android.R.layout.simple_spinner_item
        );
        costAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCost.setAdapter(costAdapter);
        int costPosition = costAdapter.getPosition(currentCostSort);
        spinnerCost.setSelection(costPosition);

        // Sự kiện cho nút Áp dụng
        buttonApply.setOnClickListener(v -> {
            currentCookingTimeSort = spinnerCookingTime.getSelectedItem().toString();
            currentDifficultyFilter = spinnerDifficulty.getSelectedItem().toString();
            currentCostSort = spinnerCost.getSelectedItem().toString();

            if (!currentQuery.isEmpty()) {
                searchRecipes(currentQuery);
            } else if (getContext() != null) {
                Toast.makeText(getContext(), "Vui lòng nhập từ khóa tìm kiếm trước khi lọc", Toast.LENGTH_SHORT).show();
            }

            filterDialog.dismiss();
        });

        // Sự kiện cho nút Hủy
        buttonCancel.setOnClickListener(v -> filterDialog.dismiss());

        // Hiển thị dialog
        filterDialog.show();
    }

    private void searchRecipes(String query) {
        if (isSearching) {
            Log.d("ExploreFragment", "Search already in progress, ignoring query: " + query);
            return;
        }
        isSearching = true;

        new android.os.Handler().postDelayed(() -> {
            if (isSearching) {
                Log.d("ExploreFragment", "Search timed out, resetting isSearching");
                isSearching = false;
            }
        }, 5000);

        String normalizedQuery = query.toLowerCase();
        Log.d("ExploreFragment", "Searching for normalized query: " + normalizedQuery);
        recipeRepository.searchRecipesByKeyword(normalizedQuery, currentCookingTimeSort, currentDifficultyFilter, currentCostSort, new RecipeRepository.RecipeCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                Log.d("ExploreFragment", "Recipes received: " + recipes.size());
                allRecipes.clear();
                allRecipes.addAll(recipes);
                Log.d("ExploreFragment", "allRecipes updated: " + allRecipes.size());
                recipeAdapter.updateRecipes(allRecipes);
                if (recipes.isEmpty() && getContext() != null) {
                    Toast.makeText(getContext(), "Không tìm thấy công thức nào cho từ khóa: " + query, Toast.LENGTH_SHORT).show();
                }
                isSearching = false;
            }
        });
    }
    private void toggleLike(Recipe recipe, int position) {
        recipe.setLiked(!recipe.isLiked());
        if (recipe.getRecipeId() != null) {
            FirebaseFirestore.getInstance()
                    .collection("recipes")
                    .document(recipe.getRecipeId())
                    .update("isLiked", recipe.isLiked());
        }
        recipeAdapter.notifyItemChanged(position);
    }
    private void navigateToRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getRecipeId());
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        isSearching = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isSearching = false;
        editTextSearch = null;
        buttonFilter = null;
        recyclerViewSearchResults = null;
        recipeRepository.removeListener();
    }
}