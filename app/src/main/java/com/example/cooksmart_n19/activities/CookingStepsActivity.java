package com.example.cooksmart_n19.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.adapters.CookingStepAdapter;
import com.example.cooksmart_n19.dialogs.RatingDialogManager;
import com.example.cooksmart_n19.models.CookingStep;
import com.example.cooksmart_n19.models.Recipe;
import com.example.cooksmart_n19.repositories.MyRecipeRepository;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class CookingStepsActivity extends AppCompatActivity {

    private ImageView closeButton;
    private ViewPager2 stepsViewPager;
    private LinearProgressIndicator stepProgress;
    private TextView textViewAverageRating, textViewRatingCount;
    private CookingStepAdapter adapter;
    private MyRecipeRepository repository;
    private RatingDialogManager ratingDialogManager;
    private Recipe recipe;

    private List<CookingStep> steps;
    private String recipeId;
    private static final String TAG = "CookingStepsActivity";
    private boolean isActivityActive = true;
    private boolean hasShownRatingDialog = false;

    public static final int REQUEST_CODE_COOKING_STEPS = 1001;
    public static final String EXTRA_AVERAGE_RATING = "extra_average_rating";
    public static final String EXTRA_RATING_COUNT = "extra_rating_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooking_steps);

        repository = new MyRecipeRepository();
        steps = new ArrayList<>();

        recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId == null) {
            Log.e(TAG, "recipeId is null");
            Toast.makeText(this, "Không tìm thấy ID công thức", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ratingDialogManager = new RatingDialogManager(this, recipeId, new RatingDialogManager.OnRatingSubmittedCallback() {
            @Override
            public void onRatingSubmitted(double averageRating, int ratingCount) {
                if (recipe != null) {
                    recipe.setAverageRating(averageRating);
                    recipe.setRatingCount(ratingCount);
                }
                // Trả về kết quả cho RecipeDetailActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_AVERAGE_RATING, averageRating);
                resultIntent.putExtra(EXTRA_RATING_COUNT, ratingCount);
                setResult(RESULT_OK, resultIntent);
            }

            @Override
            public void onError(String errorMessage) {
                // Xử lý lỗi nếu cần
            }
        });

        initViews();
        loadSteps(recipeId);
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

        adapter = new CookingStepAdapter(steps);
        stepsViewPager.setAdapter(adapter);
        stepsViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        stepProgress.setMax(steps.size());
        stepProgress.setProgress(1);

        stepsViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                stepProgress.setProgress(position + 1);
                if (position == steps.size() - 1 && !hasShownRatingDialog) {
                    Toast.makeText(CookingStepsActivity.this, "Bạn đã hoàn thành tất cả các bước!", Toast.LENGTH_SHORT).show();
                    ratingDialogManager.showRatingDialog();
                    hasShownRatingDialog = true;
                }
            }
        });

        closeButton.setOnClickListener(v -> finish());
    }

    private void loadRecipeDetails() {
        repository.getRecipeById(recipeId, new MyRecipeRepository.GetSingleRecipeCallback() {
            @Override
            public void onSuccess(Recipe loadedRecipe) {
                if (!isActivityActive) {
                    Log.w(TAG, "Activity is no longer active, skipping onSuccess");
                    return;
                }

                recipe = loadedRecipe;
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isActivityActive) {
                    Log.w(TAG, "Activity is no longer active, skipping onFailure");
                    return;
                }

                Log.e(TAG, "Error loading recipe: " + errorMessage);
                Toast.makeText(CookingStepsActivity.this, "Lỗi tải công thức: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSteps(String recipeId) {
        Log.d(TAG, "Loading steps for recipeId: " + recipeId);
        repository.loadSteps(recipeId, new MyRecipeRepository.OnStepsLoadedListener() {
            @Override
            public void onStepsLoaded(List<CookingStep> loadedSteps) {
                if (!isActivityActive) {
                    Log.w(TAG, "Activity is no longer active, skipping onStepsLoaded");
                    return;
                }

                Log.d(TAG, "Steps loaded successfully: " + loadedSteps.size() + " steps");
                if (loadedSteps == null || loadedSteps.isEmpty()) {
                    Log.e(TAG, "No steps found for this recipe");
                    Toast.makeText(CookingStepsActivity.this, "Không có bước nấu ăn nào", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                steps.clear();
                steps.addAll(loadedSteps);
                adapter.notifyDataSetChanged();

                stepProgress.setMax(steps.size());
                stepProgress.setProgress(1);
            }

            @Override
            public void onError(Exception e) {
                if (!isActivityActive) {
                    Log.w(TAG, "Activity is no longer active, skipping onError");
                    return;
                }

                Log.e(TAG, "Error loading steps: " + e.getMessage());
                Toast.makeText(CookingStepsActivity.this, "Không thể tải các bước: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}