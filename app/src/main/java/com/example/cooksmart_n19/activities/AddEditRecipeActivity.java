package com.example.cooksmart_n19.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.adapters.IngredientAdapter;
import com.example.cooksmart_n19.adapters.StepAdapter;
import com.example.cooksmart_n19.dialogs.IngredientDialogManager;
import com.example.cooksmart_n19.dialogs.StepDialogManager;
import com.example.cooksmart_n19.models.CookingStep;
import com.example.cooksmart_n19.models.IngredientItem;
import com.example.cooksmart_n19.models.Recipe;
import com.example.cooksmart_n19.repositories.MyRecipeRepository;
import com.example.cooksmart_n19.utils.CloudinaryManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddEditRecipeActivity extends AppCompatActivity {
    private TextInputEditText editTextRecipeName;
    private TextInputEditText editTextCost;
    private TextInputEditText editTextCookingTime;
    private TextInputEditText editTextRecipeIntro;
    private AutoCompleteTextView  editTextDifficulty;
    private ImageView imageViewRecipe;
    private Button buttonAddIngredient, buttonUploadImage, buttonAddStep;
    private MaterialButton buttonSave;
    private RecyclerView recyclerViewIngredients, recyclerViewSteps;
    private List<IngredientItem> ingredientItemList;
    private List<CookingStep> cookingStepList;
    private Recipe recipe;
    private IngredientAdapter ingredientAdapter;
    private StepAdapter stepInputAdapter;
    private Uri pendingImageUri;
    private ActivityResultLauncher<Intent> recipeImagePickerLauncher, stepImagePickerLauncher;
    private MyRecipeRepository repository;
    private FirebaseAuth mAuth;
    private boolean isEditing = false;
    private String recipeId;
    private IngredientDialogManager ingredientDialogManager;
    private StepDialogManager stepDialogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_recipe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        CloudinaryManager.init(this);
        init();

        //
        recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId != null) {
            TextView textViewPageTitle = findViewById(R.id.textViewPageTitle);
            textViewPageTitle.setText("Sửa món ăn");
            isEditing = true;
            loadRecipeForEditing(recipeId);
        }
    }

    private void init() {
        // Ánh xạ các thành phần giao diện
        editTextRecipeName = findViewById(R.id.editTextRecipeName);
        editTextCost = findViewById(R.id.editTextCost);
        editTextRecipeIntro = findViewById(R.id.editTextRecipeIntro);
        editTextCookingTime = findViewById(R.id.editTextCookingTime);
        editTextDifficulty = findViewById(R.id.editTextDifficulty);
        imageViewRecipe = findViewById(R.id.imageViewRecipe);
        buttonAddIngredient = findViewById(R.id.buttonAddIngredient);
        buttonUploadImage = findViewById(R.id.buttonUploadImage);
        buttonAddStep = findViewById(R.id.buttonAddStep);
        buttonSave = findViewById(R.id.buttonSave);
        recyclerViewIngredients = findViewById(R.id.recyclerViewIngredients);
        recyclerViewSteps = findViewById(R.id.recyclerViewSteps);

        repository = new MyRecipeRepository();

        recipe = new Recipe();
        ingredientItemList = new ArrayList<>();
        cookingStepList = new ArrayList<>();

        // Thiết lập AutoCompleteTextView cho độ khó
        String[] difficulties = {"Khó", "Trung Bình", "Dễ"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(
                AddEditRecipeActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                difficulties
        );
        editTextDifficulty.setAdapter(difficultyAdapter);

        // Thiết lập launcher để chọn ảnh
        recipeImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        Glide.with(AddEditRecipeActivity.this)
                                .load(imageUri)
                                .thumbnail(0.25f)
                                .placeholder(R.drawable.rice)
                                .error(R.drawable.rice)
                                .into(imageViewRecipe);
                        pendingImageUri = imageUri;
                    }
                }
        );

        stepImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        stepDialogManager.setStepImageUri(imageUri);
                    }
                }
        );

        // Khởi tạo adapter và RecyclerView
        ingredientAdapter = new IngredientAdapter(ingredientItemList, this::showAddEditIngredientDialog);
        stepInputAdapter = new StepAdapter(cookingStepList, this::showAddEditStepDialog);
        recyclerViewIngredients.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSteps.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewIngredients.setAdapter(ingredientAdapter);
        recyclerViewSteps.setAdapter(stepInputAdapter);

        // Khởi tạo các dialog manager
        ingredientDialogManager = new IngredientDialogManager(this, ingredientItemList,
                () -> ingredientAdapter.updateIngredients(new ArrayList<>(ingredientItemList)));
        stepDialogManager = new StepDialogManager(this, cookingStepList, stepImagePickerLauncher,
                () -> stepInputAdapter.updateSteps(new ArrayList<>(cookingStepList)));

        // Thiết lập sự kiện click
        buttonAddIngredient.setOnClickListener(v -> showAddEditIngredientDialog(-1));
        buttonAddStep.setOnClickListener(v -> showAddEditStepDialog(-1));
        buttonUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            recipeImagePickerLauncher.launch(intent);
        });
        buttonSave.setOnClickListener(v -> saveRecipe());
    }
    private void loadRecipeForEditing(String recipeId) {
        repository.getRecipeById(recipeId, new MyRecipeRepository.GetSingleRecipeCallback() {
            @Override
            public void onSuccess(Recipe loadedRecipe) {
                recipe = loadedRecipe;
                // Cập nhật các trường giao diện
                editTextRecipeIntro.setText(recipe.getDescription());
                editTextRecipeName.setText(recipe.getTitle());
                editTextCost.setText(String.valueOf(recipe.getCost()));
                editTextCookingTime.setText(String.valueOf(recipe.getCookingTime()));
                editTextDifficulty.setText(recipe.getDifficulty());
                if (recipe.getImage() != null && !recipe.getImage().isEmpty()) {
                    Glide.with(AddEditRecipeActivity.this)
                            .load(recipe.getImage())
                            .thumbnail(0.25f)
                            .placeholder(R.drawable.rice)
                            .error(R.drawable.rice)
                            .into(imageViewRecipe);
                }

                // Tải danh sách nguyên liệu từ subcollection
                repository.loadIngredients(recipeId, new MyRecipeRepository.OnIngredientsLoadedListener() {
                    @Override
                    public void onIngredientsLoaded(List<IngredientItem> ingredients) {
                        ingredientItemList.clear();
                        ingredientItemList.addAll(ingredients);
                        ingredientAdapter.updateIngredients(new ArrayList<>(ingredientItemList));
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(AddEditRecipeActivity.this, "Lỗi tải nguyên liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                // Tải danh sách bước thực hiện từ subcollection
                repository.loadSteps(recipeId, new MyRecipeRepository.OnStepsLoadedListener() {
                    @Override
                    public void onStepsLoaded(List<CookingStep> steps) {
                        cookingStepList.clear();
                        cookingStepList.addAll(steps);
                        stepInputAdapter.updateSteps(new ArrayList<>(cookingStepList));
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(AddEditRecipeActivity.this, "Lỗi tải bước thực hiện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(AddEditRecipeActivity.this, "Lỗi tải công thức: " + errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveRecipe() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        String authorId = user.getUid();
        String name = editTextRecipeName.getText().toString().trim();
        String costStr = editTextCost.getText().toString().trim();
        String time = editTextCookingTime.getText().toString().trim();
        String difficulty = editTextDifficulty.getText().toString().trim();
        String description = editTextRecipeIntro.getText().toString().trim();

        if (name.isEmpty() || costStr.isEmpty() || time.isEmpty() || difficulty.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        long cost;
        try {
            cost = Long.parseLong(costStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Chi phí phải là số", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ingredientItemList.isEmpty() || cookingStepList.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm nguyên liệu và bước nấu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gán thông tin cho recipe
        recipe.setAuthorId(authorId);
        recipe.setTitle(name);
        recipe.setCost(cost);
        recipe.setDescription(description);
        recipe.setCookingTime(Integer.parseInt(time));
        recipe.setDifficulty(difficulty);
        if (isEditing) {
            recipe.setRecipeId(recipeId);
            // Kiểm tra quyền tác giả trước khi cập nhật
            if (recipe.getAuthorId() != null && !recipe.getAuthorId().equals(authorId)) {
                Toast.makeText(this, "Bạn không có quyền chỉnh sửa công thức này", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ProgressBar progressBar = findViewById(R.id.saveProgressBar);

        if (pendingImageUri != null) {
            buttonSave.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);

            try {
                Uri compressedUri = compressImage(pendingImageUri);
                CloudinaryManager.uploadImage(compressedUri, "recipe", new CloudinaryManager.CloudinaryUploadCallback() {
                    @Override
                    public void onStart() {}
                    @Override
                    public void onProgress(long bytes, long totalBytes) {}
                    @Override
                    public void onSuccess(String imageUrl) {
                        runOnUiThread(() -> {
                            Log.d("RecipeUpload", "Uploaded recipe image URL: " + imageUrl);
                            recipe.setImage(imageUrl);

                            // Lưu vào Firestore qua RecipeRepository
                            repository.saveRecipe(authorId, recipe, ingredientItemList, cookingStepList, new MyRecipeRepository.SaveMyRecipeCallback() {
                                @Override
                                public void onSuccess(Recipe savedRecipe) {
                                    Log.d("MyApp", "Recipe saved successfully: " + savedRecipe.toString());
                                    Toast.makeText(AddEditRecipeActivity.this, "Đã lưu công thức", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    buttonSave.setEnabled(true);
                                    setResult(RESULT_OK);
                                    finish();
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    Log.e("RecipeSaveError", "Failed to save recipe: " + errorMessage);
                                    Toast.makeText(AddEditRecipeActivity.this, "Lỗi lưu công thức: " + errorMessage, Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    buttonSave.setEnabled(true);
                                }
                            });
                        });
                    }
                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            Log.e("RecipeUploadError", "Upload failed: " + errorMessage);
                            Toast.makeText(AddEditRecipeActivity.this, "Lỗi tải ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            buttonSave.setEnabled(true);
                        });
                    }
                });
            } catch (IOException e) {
                Log.e("CompressionError", "Failed to compress recipe image", e);
                Toast.makeText(this, "Lỗi nén ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                buttonSave.setEnabled(true);
            }
        } else {
            // Không có ảnh mới, giữ ảnh cũ (nếu có) hoặc đặt null
            if (!isEditing || recipe.getImage() == null) {
                recipe.setImage(null);
            }
            progressBar.setVisibility(View.VISIBLE);
            buttonSave.setEnabled(false);

            repository.saveRecipe(authorId, recipe, ingredientItemList, cookingStepList, new MyRecipeRepository.SaveMyRecipeCallback() {
                @Override
                public void onSuccess(Recipe savedRecipe) {
                    Log.d("MyApp", "Recipe saved successfully: " + savedRecipe.toString());
                    Toast.makeText(AddEditRecipeActivity.this, "Đã lưu công thức", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    buttonSave.setEnabled(true);
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("RecipeSaveError", "Failed to save recipe: " + errorMessage);
                    Toast.makeText(AddEditRecipeActivity.this, "Lỗi lưu công thức: " + errorMessage, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    buttonSave.setEnabled(true);
                }
            });
        }
    }

    private void showAddEditIngredientDialog(int position) {
        ingredientDialogManager.showAddEditIngredientDialog(position);
    }

    private void showAddEditStepDialog(int position) {
        stepDialogManager.showAddEditStepDialog(position);
    }

    public Uri compressImage(Uri imageUri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
        java.io.File file = new java.io.File(getCacheDir(), "compressed_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(stream.toByteArray());
        }
        return Uri.fromFile(file);
    }
}