package com.example.cooksmart_n19.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.adapters.CookingStepAdapter;
import com.example.cooksmart_n19.models.Recipe;
import com.example.cooksmart_n19.repositories.RecipeDetailsRepository;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class CookingStepsActivity extends AppCompatActivity {

    private ImageView closeButton;
    private ViewPager2 stepsViewPager;
    private LinearProgressIndicator stepProgress;
    private CookingStepAdapter adapter;
    private RecipeDetailsRepository repository;

    private List<Recipe.CookingStep> steps;
    private static final String TAG = "CookingStepsActivity";
    private boolean isActivityActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooking_steps);

        repository = new RecipeDetailsRepository();
        steps = new ArrayList<>();

        // Nhận recipe_id từ Intent
        String recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId == null) {
            Log.e(TAG, "recipeId is null");
            Toast.makeText(this, "Không tìm thấy ID công thức", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadRecipeDetails(recipeId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityActive = false;
        Log.d(TAG, "onDestroy called");
    }

    private void initViews() {
        closeButton = findViewById(R.id.close_button);
        stepsViewPager = findViewById(R.id.steps_view_pager);
        stepProgress = findViewById(R.id.step_progress);

        // Thiết lập ViewPager2
        adapter = new CookingStepAdapter(steps);
        stepsViewPager.setAdapter(adapter);
        stepsViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // Thiết lập thanh tiến trình
        stepProgress.setMax(steps.size());
        stepProgress.setProgress(1); // Bắt đầu từ bước 1

        // Cập nhật thanh tiến trình khi người dùng vuốt
        stepsViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                stepProgress.setProgress(position + 1);
                if (position == steps.size() - 1) {
                    Toast.makeText(CookingStepsActivity.this, "Bạn đã hoàn thành tất cả các bước!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Thiết lập sự kiện nút đóng
        closeButton.setOnClickListener(v -> finish());
    }

    private void loadRecipeDetails(String recipeId) {
        Log.d(TAG, "Loading recipe details for recipeId: " + recipeId);
        repository.getRecipeDetails(recipeId, new RecipeDetailsRepository.OnRecipeDetailsListener() {
            @Override
            public void onSuccess(Recipe loadedRecipe) {
                if (!isActivityActive) {
                    Log.w(TAG, "Activity is no longer active, skipping onSuccess");
                    return;
                }

                Log.d(TAG, "Recipe loaded successfully: " + (loadedRecipe != null ? loadedRecipe.toString() : "null"));
                if (loadedRecipe == null || loadedRecipe.getSteps() == null || loadedRecipe.getSteps().isEmpty()) {
                    Log.e(TAG, "No steps found for this recipe");
                    Toast.makeText(CookingStepsActivity.this, "Không có bước nấu ăn nào", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                steps.clear();
                steps.addAll(loadedRecipe.getSteps());
                adapter.notifyDataSetChanged();

                // Cập nhật thanh tiến trình
                stepProgress.setMax(steps.size());
                stepProgress.setProgress(1);
            }

            @Override
            public void onError(String error) {
                if (!isActivityActive) {
                    Log.w(TAG, "Activity is no longer active, skipping onError");
                    return;
                }

                Log.e(TAG, "Error loading recipe: " + error);
                Toast.makeText(CookingStepsActivity.this, "Không thể tải công thức: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}