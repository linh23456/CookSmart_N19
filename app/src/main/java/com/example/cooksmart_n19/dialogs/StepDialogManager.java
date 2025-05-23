package com.example.cooksmart_n19.dialogs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.activities.AddEditRecipeActivity;
import com.example.cooksmart_n19.models.CookingStep;
import com.example.cooksmart_n19.utils.CloudinaryManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;

public class StepDialogManager {
    private final Context context;
    private final List<CookingStep> cookingStepList;
    private final ActivityResultLauncher<Intent> stepImagePickerLauncher;
    private final Runnable onUpdateCallback;
    private Uri stepImageUri;
    private ImageView imageViewStepPreview; // Di chuyển thành biến instance để truy cập dễ dàng

    public StepDialogManager(Context context, List<CookingStep> cookingStepList, ActivityResultLauncher<Intent> stepImagePickerLauncher, Runnable onUpdateCallback) {
        this.context = context;
        this.cookingStepList = cookingStepList;
        this.stepImagePickerLauncher = stepImagePickerLauncher;
        this.onUpdateCallback = onUpdateCallback;
    }

    public void showAddEditStepDialog(int position) {
        boolean isEdit = position != -1;
        CookingStep step = isEdit ? cookingStepList.get(position) : new CookingStep();
        stepImageUri = null;

        // Tạo dialog view
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_step, null);
        TextInputEditText editTextStepInstruction = dialogView.findViewById(R.id.editTextStepInstruction);
        imageViewStepPreview = dialogView.findViewById(R.id.imageViewStepPreview); // Ánh xạ ở đây
        MaterialButton buttonUploadStepImage = dialogView.findViewById(R.id.buttonUploadStepImage);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

        // Hiển thị nội dung ban đầu
        if (isEdit) {
            editTextStepInstruction.setText(step.getInstruction());
            displayStepImage(step.getImages()); // Hiển thị ảnh hiện có của bước
        } else {
            imageViewStepPreview.setVisibility(View.GONE); // Ẩn ImageView nếu là bước mới
        }

        // Xử lý sự kiện upload ảnh
        buttonUploadStepImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            stepImagePickerLauncher.launch(intent);
        });

        // Tạo và hiển thị dialog
        AlertDialog dialog = new AlertDialog.Builder(context)
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
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(context, "Lỗi tải ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    positiveButton.setEnabled(true);
                                });
                            }
                        });
                    } catch (IOException e) {
                        Log.e("CompressionError", "Failed to compress image", e);
                        Toast.makeText(context, "Lỗi nén ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        });

        dialog.setOnDismissListener(d -> stepImageUri = null);
        dialog.show();
    }

    // Phương thức hiển thị hình ảnh (hiện có hoặc mới)
    private void displayStepImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .thumbnail(0.25f)
                    .placeholder(R.drawable.rice)
                    .error(R.drawable.rice)
                    .into(imageViewStepPreview);
            imageViewStepPreview.setVisibility(View.VISIBLE);
            Log.d("StepImage", "Displaying existing image: " + imageUrl);
        } else {
            imageViewStepPreview.setVisibility(View.GONE);
            Log.d("StepImage", "No existing image to display");
        }
    }

    // Phương thức hiển thị hình ảnh mới được chọn (step.getImage())
    private void displayStepImage(Uri imageUri) {
        if (imageUri != null) {
            Glide.with(context)
                    .load(imageUri)
                    .thumbnail(0.25f)
                    .placeholder(R.drawable.rice)
                    .error(R.drawable.rice)
                    .into(imageViewStepPreview);
            imageViewStepPreview.setVisibility(View.VISIBLE);
            Log.d("StepImage", "Displaying new image from URI: " + imageUri.toString());
        } else {
            imageViewStepPreview.setVisibility(View.GONE);
            Log.d("StepImage", "No new image to display");
        }
    }

    // Cập nhật stepImageUri và hiển thị ảnh mới(Uri)
    public void setStepImageUri(Uri uri) {
        this.stepImageUri = uri;
        displayStepImage(stepImageUri); // Cập nhật giao diện ngay khi có ảnh mới
    }

    private void updateStepList(CookingStep step, int position) {
        if (position == -1) {
            step.setStepNumber(cookingStepList.size() + 1);
            cookingStepList.add(step);
            Log.d("StepList", "Added new step, size: " + cookingStepList.size() + ", Image: " + step.getImages());
        } else {
            step.setStepNumber(position + 1);
            cookingStepList.set(position, step);
            Log.d("StepList", "Updated step at position " + position + ", Image: " + step.getImages());
        }
        onUpdateCallback.run();
    }

    private void runOnUiThread(Runnable action) {
        if (context instanceof AddEditRecipeActivity) {
            ((AddEditRecipeActivity) context).runOnUiThread(action);
        }
    }

    private Uri compressImage(Uri imageUri) throws IOException {
        if (context instanceof AddEditRecipeActivity) {
            return ((AddEditRecipeActivity) context).compressImage(imageUri);
        }
        throw new IllegalStateException("Context must be an instance of AddEditRecipeActivity");
    }
}