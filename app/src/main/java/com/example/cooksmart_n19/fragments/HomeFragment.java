package com.example.cooksmart_n19.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.cooksmart_n19.adapters.ItemRecipeAdapter;
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

public class HomeFragment extends Fragment {
    private RecyclerView recyclerViewFeaturedRecipes;
    private RecyclerView recyclerViewRecentRecipes;
    private ItemRecipeAdapter featuredAdapter;
    private ItemRecipeAdapter recentAdapter;
    private RecipeRepository repository;
    private RecipeDetailsRepository detailsRepository;
    private FirebaseAuth mAuth;
    private Map<String, Boolean> likeStatusMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewFeaturedRecipes = view.findViewById(R.id.recyclerViewFeaturedRecipes);
        recyclerViewRecentRecipes = view.findViewById(R.id.recyclerViewRecentRecipes);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        repository = new RecipeRepository();
        detailsRepository = new RecipeDetailsRepository();
        likeStatusMap = new HashMap<>();
        setupRecyclerViews();
        loadRecipes();
    }

    private void setupRecyclerViews() {
        featuredAdapter = new ItemRecipeAdapter(
                new ArrayList<>(),
                (recipe, position) -> {
                    toggleLike(recipe, position, true);
                    featuredAdapter.notifyItemChanged(position);
                },
                this::navigateToRecipeDetail,
                this
        );
        recyclerViewFeaturedRecipes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewFeaturedRecipes.setAdapter(featuredAdapter);

        recentAdapter = new ItemRecipeAdapter(
                new ArrayList<>(),
                (recipe, position) -> {
                    toggleLike(recipe, position, false);
                    recentAdapter.notifyItemChanged(position);
                },
                this::navigateToRecipeDetail,
                this
        );
        recyclerViewRecentRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecentRecipes.setAdapter(recentAdapter);
    }

    private void loadRecipes() {
        repository.getFeaturedRecipes(recipes -> {
            featuredAdapter.updateRecipes(recipes);
            if (mAuth.getCurrentUser() != null) {
                checkLikeStatus(recipes, true);
            }
        });

        repository.getRecentRecipes(recipes -> {
            recentAdapter.updateRecipes(recipes);
            if (mAuth.getCurrentUser() != null) {
                checkLikeStatus(recipes, false);
            }
        });
    }

    private void checkLikeStatus(List<Recipe> recipes, boolean isFeatured) {
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
                    if (isFeatured) {
                        featuredAdapter.notifyDataSetChanged();
                    } else {
                        recentAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể kiểm tra trạng thái thích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleLike(Recipe recipe, int position, boolean isFeatured) {
        if (mAuth.getCurrentUser() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để thích công thức", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        boolean isCurrentlyLiked = likeStatusMap.getOrDefault(recipe.getRecipeId(), false);
        repository.toggleLike(recipe.getRecipeId(), isCurrentlyLiked, new RecipeRepository.OnToggleLikeListener() {
            @Override
            public void onSuccess(boolean newLikeStatus) {
                likeStatusMap.put(recipe.getRecipeId(), newLikeStatus);
                if (isFeatured) {
                    featuredAdapter.notifyItemChanged(position);
                } else {
                    recentAdapter.notifyItemChanged(position);
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã cập nhật trạng thái Thích", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi khi cập nhật trạng thái Thích: " + error, Toast.LENGTH_SHORT).show();
                    Log.d("My App", error);
                }
            }
        });
    }

    private void navigateToRecipeDetail(Recipe recipe, int position) {
        Log.d("My App", recipe.getRecipeId());
        detailsRepository.getRecipeDetails(recipe.getRecipeId(), new RecipeDetailsRepository.OnRecipeDetailsListener() {
            @Override
            public void onSuccess(Recipe fullRecipe) {
                fullRecipe = recipe;
                Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
                intent.putExtra("recipe_id", fullRecipe.getRecipeId());
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
    public void onResume() {
        super.onResume();
        loadRecipes();
    }

    private void navigateToRecipeDetail(Recipe recipe) {
        // Sử dụng Intent để mở RecipeDetailActivity
        Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getRecipeId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerViewFeaturedRecipes = null;
        recyclerViewRecentRecipes = null;
        repository.removeListener();
    }
}