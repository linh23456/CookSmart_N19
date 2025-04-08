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
import com.example.cooksmart_n19.adapters.ItemRecipeAdapter;
import com.example.cooksmart_n19.models.Recipe;
import com.example.cooksmart_n19.repositories.RecipeRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerViewFeaturedRecipes;
    private RecyclerView recyclerViewRecentRecipes;
    private ItemRecipeAdapter featuredAdapter;
    private ItemRecipeAdapter recentAdapter;
    private RecipeRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo các thành phần giao diện bằng findViewById
        recyclerViewFeaturedRecipes = view.findViewById(R.id.recyclerViewFeaturedRecipes);
        recyclerViewRecentRecipes = view.findViewById(R.id.recyclerViewRecentRecipes);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new RecipeRepository();
        setupRecyclerViews();
        loadRecipes();
    }

    private void setupRecyclerViews() {
        // RecyclerView cho công thức nổi bật
        featuredAdapter = new ItemRecipeAdapter(
                new ArrayList<>(),
                (recipe, position) -> {
                    toggleLike(recipe);
                    featuredAdapter.notifyItemChanged(position);
                },
                this::navigateToRecipeDetail // Thêm listener cho nút "Xem chi tiết"
        );
        recyclerViewFeaturedRecipes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewFeaturedRecipes.setAdapter(featuredAdapter);

        // RecyclerView cho công thức gần đây
        recentAdapter = new ItemRecipeAdapter(
                new ArrayList<>(),
                (recipe, position) -> {
                    toggleLike(recipe);
                    recentAdapter.notifyItemChanged(position);
                },
                this::navigateToRecipeDetail // Thêm listener cho nút "Xem chi tiết"
        );
        recyclerViewRecentRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecentRecipes.setAdapter(recentAdapter);
    }

    private void loadRecipes() {
        // Tải công thức nổi bật
        repository.getFeaturedRecipes(recipes -> featuredAdapter.updateRecipes(recipes));

        // Tải công thức gần đây
        repository.getRecentRecipes(recipes -> recentAdapter.updateRecipes(recipes));
    }

    private void toggleLike(Recipe recipe) {
        recipe.setLiked(!recipe.isLiked());
        // Cập nhật trạng thái "Thích" trên Firestore
        if (recipe.getRecipeId() != null) {
            FirebaseFirestore.getInstance()
                    .collection("recipes")
                    .document(recipe.getRecipeId())
                    .update("isLiked", recipe.isLiked())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Đã cập nhật trạng thái Thích", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi khi cập nhật trạng thái Thích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
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