package com.example.cooksmart_n19.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.adapters.PrepareIngredientAdapter;
import com.example.cooksmart_n19.models.Recipe;
import com.example.cooksmart_n19.repositories.RecipeDetailsRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị danh sách nguyên liệu của một công thức nấu ăn.
 * Người dùng có thể đánh dấu các nguyên liệu đã chuẩn bị và nhấn "Tiếp tục" để chuyển sang bước nấu ăn.
 */
public class PrepareIngredientsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPrepareIngredients;
    private Button continueButton;
    private PrepareIngredientAdapter adapter;
    private RecipeDetailsRepository repository;
    private List<Recipe.IngredientItem> ingredientItemList;
    private Recipe recipe;
    private static final String TAG = "PrepareIngredients";
    private boolean isActivityActive = true; // Kiểm tra trạng thái activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepare_ingredients);
        repository = new RecipeDetailsRepository();
        ingredientItemList = new ArrayList<>();
        Log.d(TAG, "onCreate called");

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityActive = false; // Đánh dấu activity không còn hoạt động
        Log.d(TAG, "onDestroy called");
    }

    /**
     * Khởi tạo giao diện và các thành phần cần thiết.
     */
    private void init() {
        Log.d(TAG, "init started");

        // Ánh xạ các thành phần giao diện
        recyclerViewPrepareIngredients = findViewById(R.id.recyclerViewPrepareIngredients);
        continueButton = findViewById(R.id.continue_button);
        Log.d(TAG, "Views initialized");

        // Khởi tạo RecyclerView và adapter
        recyclerViewPrepareIngredients.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PrepareIngredientAdapter(ingredientItemList);
        recyclerViewPrepareIngredients.setAdapter(adapter);
        Log.d(TAG, "RecyclerView and adapter initialized");

        // Xử lý sự kiện nút "Tiếp tục"
        continueButton.setOnClickListener(v -> startCooking());

        // Nhận recipeId từ Intent
        String recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId == null) {
            Log.e(TAG, "recipeId is null");
            Toast.makeText(this, "Không tìm thấy ID công thức", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tải chi tiết công thức
        Log.d(TAG, "Calling loadIngredients with recipeId: " + recipeId);
        loadIngredients(recipeId);

        Log.d(TAG, "init completed");
    }

    /**
     * Tải danh sách nguyên liệu từ Firestore dựa trên recipeId.
     *
     * @param recipeId ID của công thức cần tải
     */
    private void loadIngredients(String recipeId) {
        Log.d(TAG, "loadIngredients started for recipeId: " + recipeId);

        repository.getRecipeDetails(recipeId, new RecipeDetailsRepository.OnRecipeDetailsListener() {
            @Override
            public void onSuccess(Recipe loadedRecipe) {
                // Kiểm tra xem activity còn hoạt động không trước khi xử lý
                if (!isActivityActive) {
                    Log.w(TAG, "Activity is no longer active, skipping onSuccess");
                    return;
                }

                Log.d(TAG, "onSuccess called with recipe: " + (loadedRecipe != null ? loadedRecipe.toString() : "null"));
                recipe = loadedRecipe;
                displayRecipeDetails();
            }

            @Override
            public void onError(String error) {
                // Kiểm tra xem activity còn hoạt động không trước khi xử lý
                if (!isActivityActive) {
                    Log.w(TAG, "Activity is no longer active, skipping onError");
                    return;
                }

                Log.e(TAG, "Error loading recipe: " + error);
                Toast.makeText(PrepareIngredientsActivity.this, "Công thức không tồn tại hoặc đã bị xóa. Vui lòng quay lại và thử công thức khác.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        Log.d(TAG, "loadIngredients called, waiting for Firestore response...");
    }

    /**
     * Hiển thị danh sách nguyên liệu trong RecyclerView.
     */
    private void displayRecipeDetails() {
        if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            Log.d(TAG, "No ingredients found for this recipe");
            Toast.makeText(this, "Không có nguyên liệu cho công thức này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cập nhật adapter với danh sách nguyên liệu mới
        Log.d(TAG, "Ingredients loaded: " + recipe.getIngredients().size() + " items");
        adapter.updateIngredients(recipe.getIngredients());
    }

    /**
     * Xử lý sự kiện khi người dùng nhấn nút "Tiếp tục".
     * Kiểm tra xem tất cả nguyên liệu đã được đánh dấu chưa trước khi chuyển sang bước nấu ăn.
     */
    private void startCooking() {
        if (adapter == null) {
            Log.e(TAG, "Adapter is null, cannot proceed");
            Toast.makeText(this, "Không thể kiểm tra nguyên liệu. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (adapter.areAllIngredientsChecked()) {
            Intent intent = new Intent(this, CookingStepsActivity.class);
            intent.putExtra("recipe_id", recipe.getRecipeId());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Vui lòng kiểm tra tất cả nguyên liệu trước khi tiếp tục.", Toast.LENGTH_SHORT).show();
        }
    }
}