package com.example.cooksmart_n19.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.Manifest;
import android.app.Activity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.activities.RecipeDetailActivity;
import com.example.cooksmart_n19.adapters.ItemRecipeAdapter;
import com.example.cooksmart_n19.adapters.RecipeAdapter;
import com.example.cooksmart_n19.models.Recipe;
import com.example.cooksmart_n19.repositories.RecipeDetailsRepository;
import com.example.cooksmart_n19.repositories.RecipeRepository;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ExploreFragment extends Fragment {

    private EditText editTextSearch;
    private ImageButton buttonFilter;
    private RecyclerView recyclerViewSearchResults;
    private RecipeAdapter recipeAdapter; // Đổi tên để nhất quán với tên class
    private RecipeRepository recipeRepository;
    private RecipeDetailsRepository detailsRepository;
    private List<Recipe> allRecipes;
    private String currentQuery = "";
    private String currentDifficultyFilter = "Tất cả";
    private String currentCookingTimeSort = "Mặc định";
    private String currentCostSort = "Mặc định";
    private boolean isSearching = false;
    private FirebaseAuth mAuth;
    private Map<String, Boolean> likeStatusMap;
    private ImageButton buttonCamera;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> storagePermissionLauncher;
    private ImageCapture imageCapture;
    private ImageLabeler imageLabeler;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        showPermissionDeniedToast("Camera");
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        handleImage(imageUri);
                    }
                });

        storagePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        showPermissionDeniedToast("Storage");
                    }
                });

        // Khởi tạo ML Kit Image Labeler
        ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.65f)
                .build();
        imageLabeler = ImageLabeling.getClient(options);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang phân tích ảnh...");
        progressDialog.setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        // Initialize existing UI components
        editTextSearch = view.findViewById(R.id.editTextSearch);
        buttonFilter = view.findViewById(R.id.buttonFilter);
        recyclerViewSearchResults = view.findViewById(R.id.recyclerViewSearchResults);
        buttonCamera = view.findViewById(R.id.buttonCamera);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Existing initialization code
        mAuth = FirebaseAuth.getInstance();
        recipeRepository = new RecipeRepository();
        detailsRepository = new RecipeDetailsRepository();
        allRecipes = new ArrayList<>();
        likeStatusMap = new HashMap<>();

        recipeAdapter = new RecipeAdapter(
                allRecipes,
                this::toggleLike,
                this::navigateToRecipeDetail,
                this
        );
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSearchResults.setAdapter(recipeAdapter);

        // Existing search setup
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            handleSearchAction();
            return true;
        });

        buttonFilter.setOnClickListener(v -> showFilterDialog());

        // Camera button click listener
        buttonCamera.setOnClickListener(v -> showImageSourceDialog());
    }

    private void handleSearchAction() {
        String query = editTextSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            Log.d("ExploreFragment", "Search triggered with query: " + query);
            currentQuery = query;
            searchRecipes(query);
        } else {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
            }
            currentQuery = "";
            allRecipes.clear();
            recipeAdapter.updateRecipes(allRecipes);
        }
    }

    private void showImageSourceDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn nguồn ảnh")
                .setItems(new String[]{"Máy ảnh", "Thư viện"}, (dialog, which) -> {
                    switch (which) {
                        case 0: checkCameraPermission();
                            break;
                        case 1: openGallery();
                            break;
                    }
                })
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        Dialog cameraDialog = new Dialog(requireContext());
        cameraDialog.setContentView(R.layout.dialog_camera);

        PreviewView previewView = cameraDialog.findViewById(R.id.previewView);
        ImageButton captureButton = cameraDialog.findViewById(R.id.captureButton);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Initialize preview
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Select back camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Bind use cases
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                Camera camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture);

                // Setup capture button
                captureButton.setOnClickListener(v -> captureImage(cameraDialog));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Lỗi khởi động camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));

        cameraDialog.show();
    }

    private void captureImage(Dialog dialog) {
        File photoFile = createImageFile();
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri imageUri = Uri.fromFile(photoFile);
                        handleImage(imageUri);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(getContext(), "Lỗi chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, imageFileName + ".jpg");
    }

    private void handleImage(Uri imageUri) {
        progressDialog.show();

        try {
            InputImage image = InputImage.fromFilePath(requireContext(), imageUri);

            imageLabeler.process(image)
                    .addOnSuccessListener(labels -> processImageResults(labels))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Lỗi phân tích ảnh", Toast.LENGTH_SHORT).show();
                    });
        } catch (IOException e) {
            progressDialog.dismiss();
            e.printStackTrace();
        }
    }

    private void processImageResults(List<ImageLabel> labels) {
        List<String> keywords = new ArrayList<>();
        for (ImageLabel label : labels) {
            if (label.getConfidence() >= 0.7f) {
                keywords.add(label.getText().toLowerCase());
            }
        }

        if (!keywords.isEmpty()) {
            executeImageSearch(keywords);
        } else {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Không nhận diện được thành phần món ăn", Toast.LENGTH_SHORT).show();
        }
    }

    private void executeImageSearch(List<String> keywords) {
        // Tận dụng cơ chế tìm kiếm hiện có
        String searchQuery = String.join(" ", keywords);
        currentQuery = searchQuery;
        editTextSearch.setText(searchQuery);
        searchRecipes(searchQuery);
        progressDialog.dismiss();
    }
    private void showPermissionDeniedToast(String permission) {
        Toast.makeText(getContext(),
                "Cần cấp quyền " + permission + " để sử dụng tính năng này",
                Toast.LENGTH_SHORT).show();
    }
    private void showFilterDialog() {
        Dialog filterDialog = new Dialog(requireContext());
        filterDialog.setContentView(R.layout.dialog_filter);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(filterDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        filterDialog.getWindow().setAttributes(params);

        Spinner spinnerCookingTime = filterDialog.findViewById(R.id.spinnerCookingTime);
        Spinner spinnerDifficulty = filterDialog.findViewById(R.id.spinnerDifficulty);
        Spinner spinnerCost = filterDialog.findViewById(R.id.spinnerCost);
        Button buttonApply = filterDialog.findViewById(R.id.buttonApply);
        Button buttonCancel = filterDialog.findViewById(R.id.buttonCancel);

        ArrayAdapter<CharSequence> cookingTimeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.cooking_time_options,
                android.R.layout.simple_spinner_item
        );
        cookingTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCookingTime.setAdapter(cookingTimeAdapter);
        int cookingTimePosition = cookingTimeAdapter.getPosition(currentCookingTimeSort);
        spinnerCookingTime.setSelection(cookingTimePosition);

        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.difficulty_options,
                android.R.layout.simple_spinner_item
        );
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);
        int difficultyPosition = difficultyAdapter.getPosition(currentDifficultyFilter);
        spinnerDifficulty.setSelection(difficultyPosition);

        ArrayAdapter<CharSequence> costAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.cost_options,
                android.R.layout.simple_spinner_item
        );
        costAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCost.setAdapter(costAdapter);
        int costPosition = costAdapter.getPosition(currentCostSort);
        spinnerCost.setSelection(costPosition);

        buttonApply.setOnClickListener(v -> {
            currentCookingTimeSort = spinnerCookingTime.getSelectedItem().toString();
            currentDifficultyFilter = spinnerDifficulty.getSelectedItem().toString();
            currentCostSort = spinnerCost.getSelectedItem().toString();

            if (!currentQuery.isEmpty()) {
                searchRecipes(currentQuery);
            } else if (getContext() != null) {
                Toast.makeText(getContext(), "Vui lòng nhập từ khóa tìm kiếm trước khi lọc", Toast.LENGTH_SHORT).show();
            }

            filterDialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> filterDialog.dismiss());

        filterDialog.show();
    }

    private void searchRecipes(String query) {
        if (isSearching) {
            Log.d("ExploreFragment", "Search already in progress, ignoring query: " + query);
            return;
        }
        isSearching = true;

        new android.os.Handler().postDelayed(() -> {
            if (isSearching) {
                Log.d("ExploreFragment", "Search timed out, resetting isSearching");
                isSearching = false;
            }
        }, 5000);

        String normalizedQuery = query.toLowerCase();
        Log.d("ExploreFragment", "Searching for normalized query: " + normalizedQuery);
        recipeRepository.searchRecipesByKeyword(normalizedQuery, currentCookingTimeSort, currentDifficultyFilter, currentCostSort, recipes -> {
            Log.d("ExploreFragment", "Recipes received: " + recipes.size());
            allRecipes.clear();
            allRecipes.addAll(recipes);
            Log.d("ExploreFragment", "allRecipes updated: " + allRecipes.size());
            recipeAdapter.updateRecipes(allRecipes);
            if (recipes.isEmpty() && getContext() != null) {
                Toast.makeText(getContext(), "Không tìm thấy công thức nào cho từ khóa: " + query, Toast.LENGTH_SHORT).show();
            }
            if (mAuth.getCurrentUser() != null) {
                checkLikeStatus(recipes); // Kiểm tra trạng thái "thích" sau khi tải danh sách
            }
            isSearching = false;
        });
    }

    private void checkLikeStatus(List<Recipe> recipes) {
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
                    recipeAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể kiểm tra trạng thái thích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleLike(Recipe recipe, int position) {
        if (mAuth.getCurrentUser() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để thích công thức", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        boolean isCurrentlyLiked = likeStatusMap.getOrDefault(recipe.getRecipeId(), false);
        recipeRepository.toggleLike(recipe.getRecipeId(), isCurrentlyLiked, new RecipeRepository.OnToggleLikeListener() {
            @Override
            public void onSuccess(boolean newLikeStatus) {
                likeStatusMap.put(recipe.getRecipeId(), newLikeStatus);
                recipeAdapter.notifyItemChanged(position);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã cập nhật trạng thái Thích", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi khi cập nhật trạng thái Thích: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToRecipeDetail(Recipe recipe, int position) {
        if (mAuth.getCurrentUser() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để xem chi tiết công thức", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        detailsRepository.getRecipeDetails(recipe.getRecipeId(), new RecipeDetailsRepository.OnRecipeDetailsListener() {
            @Override
            public void onSuccess(Recipe fullRecipe) {
                Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
                intent.putExtra("recipe_id", recipe.getRecipeId());
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
    public void onPause() {
        super.onPause();
        isSearching = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isSearching = false;
        editTextSearch = null;
        buttonFilter = null;
        recyclerViewSearchResults = null;
        recipeRepository.removeListener();
    }
}