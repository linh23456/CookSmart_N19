package com.example.cooksmart_n19.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.adapters.PrepareIngredientAdapter;
import com.example.cooksmart_n19.models.IngredientItem;
import com.example.cooksmart_n19.repositories.MyRecipeRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class PrepareIngredientsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPrepareIngredients;
    private Button continueButton;
    private ProgressBar progressBar;
    private PrepareIngredientAdapter adapter;
    private MyRecipeRepository repository;
    private List<IngredientItem> ingredientItemList;
    private String recipeId;
    private static final String TAG = "PrepareIngredients";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepare_ingredients);
        repository = new MyRecipeRepository();
        mAuth = FirebaseAuth.getInstance();
        ingredientItemList = new ArrayList<>();
        Log.d(TAG, "onCreate called");

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        progressBar = findViewById(R.id.progressBar); // Thêm ProgressBar vào layout
        Log.d(TAG, "Views initialized");

        // Khởi tạo RecyclerView và adapter
        recyclerViewPrepareIngredients.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PrepareIngredientAdapter(ingredientItemList);
        recyclerViewPrepareIngredients.setAdapter(adapter);
        Log.d(TAG, "RecyclerView and adapter initialized");

        // Kiểm tra chiều cao của RecyclerView
        recyclerViewPrepareIngredients.post(() -> {
            Log.d(TAG, "RecyclerView height: " + recyclerViewPrepareIngredients.getHeight());
        });

        // Xử lý sự kiện nút "Tiếp tục"
        continueButton.setOnClickListener(v -> startCooking());

        // Nhận recipeId từ Intent
        recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId == null) {
            Log.e(TAG, "recipeId is null");
            Toast.makeText(this, "Không tìm thấy ID công thức", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Kiểm tra đăng nhập (nếu cần)
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "User is not logged in");
            Toast.makeText(this, "Vui lòng đăng nhập để xem chi tiết công thức", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tải danh sách nguyên liệu
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

        // Hiển thị ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        continueButton.setEnabled(false); // Vô hiệu hóa nút "Tiếp tục" trong khi tải

        repository.loadIngredients(recipeId, new MyRecipeRepository.OnIngredientsLoadedListener() {
            @Override
            public void onIngredientsLoaded(List<IngredientItem> ingredients) {

                // Ẩn ProgressBar
                progressBar.setVisibility(View.GONE);
                continueButton.setEnabled(true);

                Log.d(TAG, "Ingredients loaded successfully: " + ingredients.size() + " items");
                if (ingredients == null || ingredients.isEmpty()) {
                    Log.e(TAG, "No ingredients found for this recipe");
                    Toast.makeText(PrepareIngredientsActivity.this, "Không có nguyên liệu cho công thức này", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                ingredientItemList.clear();
                ingredientItemList.addAll(ingredients);
                Log.d(TAG, "Updated ingredientItemList: " + ingredientItemList.toString());
                adapter.updateIngredients(ingredientItemList);
                Log.d(TAG, "Adapter item count after update: " + adapter.getItemCount());
            }

            @Override
            public void onError(Exception e) {

                // Ẩn ProgressBar
                progressBar.setVisibility(View.GONE);
                continueButton.setEnabled(true);

                Log.e(TAG, "Error loading ingredients: " + e.getMessage());
                Toast.makeText(PrepareIngredientsActivity.this, "Không thể tải nguyên liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });

        Log.d(TAG, "loadIngredients called, waiting for Firestore response...");
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
            intent.putExtra("recipe_id", recipeId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Vui lòng kiểm tra tất cả nguyên liệu trước khi tiếp tục.", Toast.LENGTH_SHORT).show();
        }
    }
}