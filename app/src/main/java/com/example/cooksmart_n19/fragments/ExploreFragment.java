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
import com.example.cooksmart_n19.adapters.ItemRecipeAdapter;
import com.example.cooksmart_n19.adapters.RecipeAdapter;
import com.example.cooksmart_n19.models.Recipe;
import com.example.cooksmart_n19.repositories.RecipeDetailsRepository;
import com.example.cooksmart_n19.repositories.RecipeRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExploreFragment extends Fragment {

    private EditText editTextSearch;
    private ImageButton buttonFilter;
    private RecyclerView recyclerViewSearchResults;
    private RecipeAdapter recipeAdapter; // Đổi tên để nhất quán với tên class
    private RecipeRepository recipeRepository;
    private RecipeDetailsRepository detailsRepository;
    private List<Recipe> allRecipes;
    private String currentQuery = "";
    private String currentDifficultyFilter = "Tất cả";
    private String currentCookingTimeSort = "Mặc định";
    private String currentCostSort = "Mặc định";
    private boolean isSearching = false;
    private FirebaseAuth mAuth;
    private Map<String, Boolean> likeStatusMap; // Thêm để quản lý trạng thái "thích"

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

        mAuth = FirebaseAuth.getInstance();
        recipeRepository = new RecipeRepository();
        detailsRepository = new RecipeDetailsRepository();
        allRecipes = new ArrayList<>();
        likeStatusMap = new HashMap<>(); // Khởi tạo likeStatusMap

        recipeAdapter = new RecipeAdapter(
                allRecipes,
                this::toggleLike,
                this::navigateToRecipeDetail,
                this // Truyền ExploreFragment để ItemRecipeAdapter có thể gọi isRecipeLiked()
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

        buttonFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        Dialog filterDialog = new Dialog(requireContext());
        filterDialog.setContentView(R.layout.dialog_filter);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(filterDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        filterDialog.getWindow().setAttributes(params);

        Spinner spinnerCookingTime = filterDialog.findViewById(R.id.spinnerCookingTime);
        Spinner spinnerDifficulty = filterDialog.findViewById(R.id.spinnerDifficulty);
        Spinner spinnerCost = filterDialog.findViewById(R.id.spinnerCost);
        Button buttonApply = filterDialog.findViewById(R.id.buttonApply);
        Button buttonCancel = filterDialog.findViewById(R.id.buttonCancel);

        ArrayAdapter<CharSequence> cookingTimeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.cooking_time_options,
                android.R.layout.simple_spinner_item
        );
        cookingTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCookingTime.setAdapter(cookingTimeAdapter);
        int cookingTimePosition = cookingTimeAdapter.getPosition(currentCookingTimeSort);
        spinnerCookingTime.setSelection(cookingTimePosition);

        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.difficulty_options,
                android.R.layout.simple_spinner_item
        );
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);
        int difficultyPosition = difficultyAdapter.getPosition(currentDifficultyFilter);
        spinnerDifficulty.setSelection(difficultyPosition);

        ArrayAdapter<CharSequence> costAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.cost_options,
                android.R.layout.simple_spinner_item
        );
        costAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCost.setAdapter(costAdapter);
        int costPosition = costAdapter.getPosition(currentCostSort);
        spinnerCost.setSelection(costPosition);

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

        buttonCancel.setOnClickListener(v -> filterDialog.dismiss());

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
        recipeRepository.searchRecipesByKeyword(normalizedQuery, currentCookingTimeSort, currentDifficultyFilter, currentCostSort, recipes -> {
            Log.d("ExploreFragment", "Recipes received: " + recipes.size());
            allRecipes.clear();
            allRecipes.addAll(recipes);
            Log.d("ExploreFragment", "allRecipes updated: " + allRecipes.size());
            recipeAdapter.updateRecipes(allRecipes);
            if (recipes.isEmpty() && getContext() != null) {
                Toast.makeText(getContext(), "Không tìm thấy công thức nào cho từ khóa: " + query, Toast.LENGTH_SHORT).show();
            }
            if (mAuth.getCurrentUser() != null) {
                checkLikeStatus(recipes); // Kiểm tra trạng thái "thích" sau khi tải danh sách
            }
            isSearching = false;
        });
    }

    private void checkLikeStatus(List<Recipe> recipes) {
        if (recipes.isEmpty()) return;

        String userId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user_likes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    likeStatusMap.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String recipeId = document.getString("recipeId");
                        if (recipeId != null) {
                            likeStatusMap.put(recipeId, true);
                        }
                    }
                    recipeAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể kiểm tra trạng thái thích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleLike(Recipe recipe, int position) {
        if (mAuth.getCurrentUser() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để thích công thức", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        boolean isCurrentlyLiked = likeStatusMap.getOrDefault(recipe.getRecipeId(), false);
        recipeRepository.toggleLike(recipe.getRecipeId(), isCurrentlyLiked, new RecipeRepository.OnToggleLikeListener() {
            @Override
            public void onSuccess(boolean newLikeStatus) {
                likeStatusMap.put(recipe.getRecipeId(), newLikeStatus);
                recipeAdapter.notifyItemChanged(position);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã cập nhật trạng thái Thích", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi khi cập nhật trạng thái Thích: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToRecipeDetail(Recipe recipe, int position) {
        if (mAuth.getCurrentUser() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để xem chi tiết công thức", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        detailsRepository.getRecipeDetails(recipe.getRecipeId(), new RecipeDetailsRepository.OnRecipeDetailsListener() {
            @Override
            public void onSuccess(Recipe fullRecipe) {
                Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
                intent.putExtra("recipe_id", recipe.getRecipeId());
                startActivity(intent);
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean isRecipeLiked(String recipeId) {
        return likeStatusMap.getOrDefault(recipeId, false);
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