package com.example.cooksmart_n19.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.example.cooksmart_n19.adapters.StepInputAdapter;
import com.example.cooksmart_n19.models.Recipe;
import com.example.cooksmart_n19.repositories.MyRecipeRepository;
import com.example.cooksmart_n19.utils.CloudinaryManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddEditRecipeActivity extends AppCompatActivity {
    private TextInputEditText editTextRecipeName;
    private TextInputEditText editTextCost;
    private TextInputEditText editTextCookingTime;
    private ImageView imageViewRecipe;
    private AutoCompleteTextView editTextDifficulty;
    private Button buttonAddIngredient, buttonUploadImage, buttonAddStep;
    private MaterialButton buttonSave;
    private RecyclerView recyclerViewIngredients, recyclerViewSteps;
    private List<Recipe.IngredientItem> ingredientItemList;
    private List<Recipe.CookingStep> cookingStepList;
    private Recipe recipe;
    private IngredientAdapter ingredientAdapter;
    private StepInputAdapter stepInputAdapter;
    private Uri pendingImageUri, stepImageUri;
    private ActivityResultLauncher<Intent> recipeImagePickerLauncher, stepImagePickerLauncher;
    private MyRecipeRepository repository;
    private FirebaseAuth mAuth;
    private boolean isEditing = false;
    private String recipeId;

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

        recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId != null) {
            TextView textViewPageTitle = findViewById(R.id.textViewPageTitle);
            textViewPageTitle.setText("Sửa món ăn");
            isEditing = true;
            loadRecipeForEditing(recipeId);
        }
    }

    private void loadRecipeForEditing(String recipeId) {
        repository.getRecipeById(recipeId, new MyRecipeRepository.GetSingleRecipeCallback() {
            @Override
            public void onSuccess(Recipe loadedRecipe) {
                recipe = loadedRecipe;
                // Cập nhật danh sách nguyên liệu và bước nấu
                ingredientItemList.clear();
                cookingStepList.clear();
                if (recipe.getIngredients() != null) {
                    ingredientItemList.addAll(recipe.getIngredients());
                }
                if (recipe.getSteps() != null) {
                    cookingStepList.addAll(recipe.getSteps());
                }
                // Cập nhật các trường
                editTextRecipeName.setText(recipe.getTitle());
                editTextCost.setText(String.valueOf(recipe.getCost()));
                editTextCookingTime.setText(String.valueOf(recipe.getCookingTime()));
                editTextDifficulty.setText(recipe.getDifficulty());
                if (recipe.getImage() != null && !recipe.getImage().isEmpty()) {
                    Glide.with(AddEditRecipeActivity.this)
                            .load(recipe.getImage())
                            .placeholder(R.drawable.rice)
                            .error(R.drawable.rice)
                            .into(imageViewRecipe);
                }
                // Cập nhật RecyclerView
                ingredientAdapter.updateIngredients(new ArrayList<>(ingredientItemList));
                stepInputAdapter.updateSteps(new ArrayList<>(cookingStepList));
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(AddEditRecipeActivity.this, "Lỗi tải công thức: " + errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void init() {
        editTextRecipeName = findViewById(R.id.editTextRecipeName);
        editTextCost = findViewById(R.id.editTextCost);
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
        recipe.setIngredients(ingredientItemList);
        recipe.setSteps(cookingStepList);

        String[] difficulties = {"Khó", "Trung Bình", "Dễ"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(
                AddEditRecipeActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                difficulties
        );
        editTextDifficulty.setAdapter(difficultyAdapter);

        recipeImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        Glide.with(AddEditRecipeActivity.this)
                                .load(imageUri)
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
                        stepImageUri = result.getData().getData();
                    }
                }
        );

        ingredientAdapter = new IngredientAdapter(ingredientItemList, position -> showAddEditIngredientDialog(position));
        stepInputAdapter = new StepInputAdapter(cookingStepList, position -> showAddEditStepDialog(position));
        recyclerViewIngredients.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSteps.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewIngredients.setAdapter(ingredientAdapter);
        recyclerViewSteps.setAdapter(stepInputAdapter);

        buttonAddIngredient.setOnClickListener(v -> showAddEditIngredientDialog(-1));
        buttonAddStep.setOnClickListener(v -> showAddEditStepDialog(-1));
        buttonUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            recipeImagePickerLauncher.launch(intent);
        });
        buttonSave.setOnClickListener(v -> saveRecipe());
    }

    private void saveRecipe() {
//        FirebaseUser user = mAuth.getCurrentUser();
//        if (user == null) {
//            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
//            return;
//        }

        String name = editTextRecipeName.getText().toString().trim();
        String costStr = editTextCost.getText().toString().trim();
        String time = editTextCookingTime.getText().toString().trim();
        String difficulty = editTextDifficulty.getText().toString().trim();

        if (name.isEmpty() || costStr.isEmpty() || time.isEmpty() || difficulty.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double cost;
        try {
            cost = Double.parseDouble(costStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Chi phí phải là số", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ingredientItemList.isEmpty() || cookingStepList.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm nguyên liệu và bước nấu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gán thông tin cho recipe
        recipe.setTitle(name);
        recipe.setCost(cost);
        recipe.setCookingTime(Integer.parseInt(time));
        recipe.setDifficulty(difficulty);
        recipe.setIngredients(new ArrayList<>(ingredientItemList));
        recipe.setSteps(new ArrayList<>(cookingStepList));

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
                            repository.saveRecipe(recipe, new MyRecipeRepository.SaveMyRecipeCallback() {
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
            // Không có ảnh, lưu trực tiếp vào Firestore
            recipe.setImage(null);
            progressBar.setVisibility(View.VISIBLE);
            buttonSave.setEnabled(false);

            repository.saveRecipe(recipe, new MyRecipeRepository.SaveMyRecipeCallback() {
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

    private void showAddEditStepDialog(int position) {
        boolean isEdit = position != -1;
        Recipe.CookingStep step = isEdit ? cookingStepList.get(position) : new Recipe.CookingStep();
        stepImageUri = null;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_step, null);
        TextInputEditText editTextStepInstruction = dialogView.findViewById(R.id.editTextStepInstruction);
        ImageView imageViewStepPreview = dialogView.findViewById(R.id.imageViewStepPreview);
        MaterialButton buttonUploadStepImage = dialogView.findViewById(R.id.buttonUploadStepImage);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

        if (isEdit) {
            editTextStepInstruction.setText(step.getInstruction());
            if (step.getImages() != null && !step.getImages().isEmpty()) {
                imageViewStepPreview.setVisibility(View.VISIBLE);
                Glide.with(this).load(step.getImages()).into(imageViewStepPreview);
                Log.d("StepEdit", "Loading existing image: " + step.getImages());
            } else {
                imageViewStepPreview.setVisibility(View.GONE);
            }
        } else {
            imageViewStepPreview.setVisibility(View.GONE);
        }

        buttonUploadStepImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            stepImagePickerLauncher.launch(intent);
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa bước" : "Thêm bước")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Sửa" : "Thêm", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String instruction = editTextStepInstruction.getText().toString().trim();
                if (instruction.isEmpty()) {
                    Toast.makeText(AddEditRecipeActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (stepImageUri != null) {
                    try {
                        progressBar.setVisibility(View.VISIBLE);
                        positiveButton.setEnabled(false);
                        Uri compressedUri = compressImage(stepImageUri);
                        CloudinaryManager.uploadImage(compressedUri, "recipe", new CloudinaryManager.CloudinaryUploadCallback() {
                            @Override
                            public void onStart() {}
                            @Override
                            public void onProgress(long bytes, long totalBytes) {}
                            @Override
                            public void onSuccess(String imageUrl) {
                                runOnUiThread(() -> {
                                    Log.d("StepUpload", "Uploaded new image URL: " + imageUrl);
                                    step.setInstruction(instruction);
                                    step.setImages(imageUrl);
                                    updateStepList(step, position);
                                    progressBar.setVisibility(View.GONE);
                                    positiveButton.setEnabled(true);
                                    dialog.dismiss();
                                });
                            }
                            @Override
                            public void onError(String errorMessage) {
                                runOnUiThread(() -> {
                                    Log.e("StepUploadError", "Upload failed: " + errorMessage);
                                    Toast.makeText(AddEditRecipeActivity.this, "Lỗi tải ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    positiveButton.setEnabled(true);
                                });
                            }
                        });
                    } catch (IOException e) {
                        Log.e("CompressionError", "Failed to compress image", e);
                        Toast.makeText(AddEditRecipeActivity.this, "Lỗi nén ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        positiveButton.setEnabled(true);
                    }
                } else {
                    Log.d("StepEdit", "Keeping old image: " + step.getImages());
                    step.setInstruction(instruction);
                    updateStepList(step, position);
                    dialog.dismiss();
                }
            });

            if (stepImageUri != null) {
                Glide.with(this)
                        .load(stepImageUri)
                        .placeholder(R.drawable.rice)
                        .error(R.drawable.rice)
                        .into(imageViewStepPreview);
                imageViewStepPreview.setVisibility(View.VISIBLE);
            }
        });

        dialog.setOnDismissListener(d -> stepImageUri = null);
        dialog.show();
    }

    private void updateStepList(Recipe.CookingStep step, int position) {
        if (position == -1) {
            step.setStepNumber(cookingStepList.size() + 1);
            cookingStepList.add(step);
            Log.d("StepList", "Added new step, size: " + cookingStepList.size() + ", Image: " + step.getImages());
        } else {
            step.setStepNumber(position + 1);
            cookingStepList.set(position, step);
            Log.d("StepList", "Updated step at position " + position + ", Image: " + step.getImages());
        }
        stepInputAdapter.updateSteps(new ArrayList<>(cookingStepList));
    }

    private void showAddEditIngredientDialog(int position) {
        boolean isEdit = position != -1;
        Recipe.IngredientItem ingredientItem = isEdit ? ingredientItemList.get(position) : new Recipe.IngredientItem();

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_ingredient, null);
        TextInputEditText editTextIngredientName = dialogView.findViewById(R.id.editTextIngredientName);
        TextInputEditText editTextQuantity = dialogView.findViewById(R.id.editTextQuantity);
        TextInputEditText editTextUnit = dialogView.findViewById(R.id.editTextUnit);

        if (isEdit) {
            editTextIngredientName.setText(ingredientItem.getIngredientName());
            editTextQuantity.setText(String.valueOf(ingredientItem.getQuantity()));
            editTextUnit.setText(ingredientItem.getUnit());
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa thông tin nguyên liệu" : "Thêm nguyên liệu")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Lưu" : "Thêm", (dialog, which) -> {
                    String ingredientName = editTextIngredientName.getText().toString().trim();
                    String quantityIngredient = editTextQuantity.getText().toString().trim();
                    String unit = editTextUnit.getText().toString().trim();

                    if (ingredientName.isEmpty() || quantityIngredient.isEmpty() || unit.isEmpty()) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double quantity;
                    try {
                        quantity = Double.parseDouble(quantityIngredient);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Số lượng phải là số", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ingredientItem.setIngredientName(ingredientName);
                    ingredientItem.setQuantity(quantity);
                    ingredientItem.setUnit(unit);

                    if (isEdit) {
                        ingredientItemList.set(position, ingredientItem);
                    } else {
                        ingredientItemList.add(ingredientItem);
                    }

                    ingredientAdapter.updateIngredients(new ArrayList<>(ingredientItemList));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private Uri compressImage(Uri imageUri) throws IOException {
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