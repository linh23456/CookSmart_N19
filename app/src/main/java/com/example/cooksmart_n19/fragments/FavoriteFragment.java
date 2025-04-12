package com.example.cooksmart_n19.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.activities.RecipeDetailActivity;
import com.example.cooksmart_n19.adapters.FavoriteRecipeAdapter;
import com.example.cooksmart_n19.models.Recipe;
import com.example.cooksmart_n19.repositories.FavoriteRecipeRepository;
import com.example.cooksmart_n19.repositories.RecipeRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerViewFavorites;
    private FavoriteRecipeAdapter recipeAdapter;
    private RecipeRepository recipeRepository;
    private FavoriteRecipeRepository favouriteRecipeRepository;
    private List<Recipe> favoriteRecipes;
    private FirebaseAuth mAuth;
    private Map<String, Boolean> likeStatusMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        recyclerViewFavorites = view.findViewById(R.id.recyclerViewFavorites);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        recipeRepository = new RecipeRepository();
        favouriteRecipeRepository = new FavoriteRecipeRepository(); // Khởi tạo favouriteRecipeRepository
        favoriteRecipes = new ArrayList<>();
        likeStatusMap = new HashMap<>();

        recipeAdapter = new FavoriteRecipeAdapter(
                favoriteRecipes,
                this::toggleLike,
                this::navigateToRecipeDetail,
                this
        );
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFavorites.setAdapter(recipeAdapter);

        loadFavoriteRecipes();
    }

    private void loadFavoriteRecipes() {
        favouriteRecipeRepository.getFavoriteRecipes(new FavoriteRecipeRepository.RecipeCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                favoriteRecipes.clear();
                favoriteRecipes.addAll(recipes);
                for (Recipe recipe : recipes) {
                    likeStatusMap.put(recipe.getRecipeId(), true); // Đánh dấu tất cả là đã thích
                }
                recipeAdapter.updateRecipes(favoriteRecipes);
                // Kiểm tra isAdded() trước khi dùng getContext()
                if (recipes.isEmpty() && isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Bạn chưa có công thức yêu thích nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (isAdded() && getContext() != null) {
                    if (error.contains("PERMISSION_DENIED")) {
                        Toast.makeText(getContext(), "Không có quyền truy cập danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void toggleLike(Recipe recipe, int position) {
        if (mAuth.getCurrentUser() == null) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để thích công thức", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        boolean isCurrentlyLiked = likeStatusMap.getOrDefault(recipe.getRecipeId(), false);
        recipeRepository.toggleLike(recipe.getRecipeId(), isCurrentlyLiked, new RecipeRepository.OnToggleLikeListener() {
            @Override
            public void onSuccess(boolean newLikeStatus) {
                likeStatusMap.put(recipe.getRecipeId(), newLikeStatus);
                if (!newLikeStatus) {
                    // Nếu bỏ thích, xóa công thức khỏi danh sách yêu thích
                    favoriteRecipes.removeIf(r -> r.getRecipeId().equals(recipe.getRecipeId()));
                }
                recipeAdapter.updateRecipes(favoriteRecipes);
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Đã cập nhật trạng thái Thích", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (isAdded() && getContext() != null) {
                    if (error.contains("PERMISSION_DENIED")) {
                        Toast.makeText(getContext(), "Không có quyền cập nhật trạng thái thích", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi cập nhật trạng thái Thích: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void navigateToRecipeDetail(Recipe recipe, int position) {
        if (mAuth.getCurrentUser() == null) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để xem chi tiết công thức", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        recipeRepository.getRecipeDetails(recipe.getRecipeId(), new RecipeRepository.OnRecipeDetailsListener() {
            @Override
            public void onSuccess(Recipe fullRecipe) {
                Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
                intent.putExtra("recipe", fullRecipe.toString()); // Truyền trực tiếp đối tượng Recipe
                startActivity(intent);
            }

            @Override
            public void onFailure(String error) {
                if (isAdded() && getContext() != null) {
                    if (error.contains("PERMISSION_DENIED")) {
                        Toast.makeText(getContext(), "Không có quyền xem chi tiết công thức", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public boolean isRecipeLiked(String recipeId) {
        return likeStatusMap.getOrDefault(recipeId, false);
    }
}