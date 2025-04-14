package com.example.cooksmart_n19.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.activities.auth.LoginActivity;
import com.example.cooksmart_n19.adapters.IngredientAdapter;
import com.example.cooksmart_n19.models.IngredientItem;
import com.example.cooksmart_n19.models.Recipe;
import com.example.cooksmart_n19.repositories.MyRecipeRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView recipeImage;
    private TextView recipeTitle;
    private TextView recipeDifficulty;
    private TextView recipeCookingTime;
    private TextView recipeCost;
    private TextView recipeRating;
    private ImageView shareButton;
    private ImageView likeButton;
    private TextView recipeDescription;
    private Button startCookingButton;
    private IngredientAdapter ingredientAdapter;
    private RecyclerView recyclerView;
    private Recipe recipe;
    private List<IngredientItem> ingredientItemList;
    private boolean isLiked = false; // Trạng thái "thích" (giả định)
    private MyRecipeRepository repository;
    private FirebaseAuth mAuth;
    private static final String TAG = "RecipeDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo repository
        repository = new MyRecipeRepository();

        ingredientItemList = new ArrayList<>();
        Log.d(TAG, "onCreate called");
        init();
    }

    private void init() {
        // Ánh xạ các thành phần giao diện
        recipeImage = findViewById(R.id.recipe_image);
        recipeTitle = findViewById(R.id.recipe_title);
        recipeDifficulty = findViewById(R.id.recipe_difficulty);
        recipeCookingTime = findViewById(R.id.recipe_cooking_time);
        recipeCost = findViewById(R.id.recipe_cost);
        recipeRating = findViewById(R.id.recipe_rating);
        shareButton = findViewById(R.id.share_button);
        likeButton = findViewById(R.id.like_button);
        recipeDescription = findViewById(R.id.recipe_description);
        startCookingButton = findViewById(R.id.start_cooking_button);
        recyclerView = findViewById(R.id.ingredients_recycler_view);

        // Nhận recipeId từ Intent
        String recipeId = getIntent().getStringExtra("recipe_id");
        Log.d(TAG, "Received recipeId: " + recipeId);
        if (recipeId != null) {
            loadRecipeDetails(recipeId);
        } else {
            Log.e(TAG, "recipeId is null");
            Toast.makeText(this, "Không tìm thấy ID công thức", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Xử lý sự kiện nút "Thích"
        likeButton.setOnClickListener(v -> toggleLike());

        // Xử lý sự kiện nút "Chia sẻ"
        shareButton.setOnClickListener(v -> shareRecipe());

        // Xử lý sự kiện nút "Bắt đầu"
        startCookingButton.setOnClickListener(v -> startCooking());
    }

    private void loadRecipeDetails(String recipeId) {
        Log.d(TAG, "Loading recipe details for recipeId: " + recipeId);

        // Tải thông tin cơ bản của công thức
        repository.getRecipeById(recipeId, new MyRecipeRepository.GetSingleRecipeCallback() {
            @Override
            public void onSuccess(Recipe loadedRecipe) {
                Log.d(TAG, "Recipe loaded successfully: " + (loadedRecipe != null ? loadedRecipe.toString() : "null"));
                recipe = loadedRecipe;
                displayRecipeDetails();

                // Tải danh sách nguyên liệu từ subcollection
                repository.loadIngredients(recipeId, new MyRecipeRepository.OnIngredientsLoadedListener() {
                    @Override
                    public void onIngredientsLoaded(List<IngredientItem> ingredients) {
                        Log.d(TAG, "Ingredients loaded successfully: " + ingredients.size() + " items");
                        ingredientItemList.clear();
                        ingredientItemList.addAll(ingredients);
                        ingredientAdapter = new IngredientAdapter(ingredientItemList, position -> {});
                        recyclerView.setLayoutManager(new LinearLayoutManager(RecipeDetailActivity.this));
                        recyclerView.setAdapter(ingredientAdapter);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error loading ingredients: " + e.getMessage());
                        Toast.makeText(RecipeDetailActivity.this, "Không thể tải nguyên liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Error loading recipe: " + errorMessage);
                Toast.makeText(RecipeDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayRecipeDetails() {
        if (recipe == null) {
            Log.e(TAG, "Recipe is null");
            Toast.makeText(this, "Không thể hiển thị công thức", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Displaying recipe details for recipeId: " + recipe.getRecipeId());
        // Hiển thị thông tin công thức
        recipeTitle.setText(recipe.getTitle());
        recipeDifficulty.setText("Độ khó: " + recipe.getDifficulty());
        recipeCookingTime.setText("Thời gian: " + recipe.getCookingTime() + " phút");
        recipeCost.setText("Chi phí: " + recipe.getCost() + " VNĐ");
        recipeRating.setText(String.format("%.1f (%d đánh giá)", recipe.getAverageRating(), recipe.getRatingCount())); // Giả định đánh giá

        // Hiển thị mô tả
        recipeDescription.setText(recipe.getDescription() != null ? recipe.getDescription() : "Không có mô tả.");

        // Hiển thị hình ảnh
        Glide.with(this)
                .load(recipe.getImage())
                .placeholder(R.drawable.recipe_placeholder)
                .error(R.drawable.recipe_placeholder)
                .into(recipeImage);
    }

    private void toggleLike() {
        isLiked = !isLiked;
        likeButton.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        Toast.makeText(this, isLiked ? "Đã thích công thức" : "Đã bỏ thích công thức", Toast.LENGTH_SHORT).show();
    }

    private void shareRecipe() {
        if (recipe == null) return;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ công thức: " + recipe.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hãy thử công thức " + recipe.getTitle() + " này nhé!");
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ công thức"));
    }

    private void startCooking() {
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, PrepareIngredientsActivity.class);
            intent.putExtra("recipe_id", recipe.getRecipeId());
            startActivity(intent);
            recipeRating.setText(String.format("%.1f (%d đánh giá)", recipe.getAverageRating(), recipe.getRatingCount()));
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập trước khi bắt đầu nấu ăn", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            displayRecipeDetails();
        }
    }
}