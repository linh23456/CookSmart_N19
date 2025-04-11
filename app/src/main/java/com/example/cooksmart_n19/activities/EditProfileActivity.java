package com.example.cooksmart_n19.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.interfaces.FirebaseCallback;
import com.example.cooksmart_n19.models.User;
import com.example.cooksmart_n19.repositories.UserRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageButton backButton;
    private ImageView avatarImageView;
    private TextView changeImageButton;
    private TextInputEditText nameEditText, phoneEditText, emailEditText;
    private Button saveButton;

    private UserRepository userRepository;
    private User currentUser;
    private Uri newImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Ánh xạ view
        backButton        = findViewById(R.id.btn_back);
        avatarImageView   = findViewById(R.id.img_avatar);
        changeImageButton = findViewById(R.id.changeImageButton);
        nameEditText      = findViewById(R.id.edt_name);
        phoneEditText     = findViewById(R.id.edt_phone);
        emailEditText     = findViewById(R.id.edt_email);
        saveButton        = findViewById(R.id.btn_save);

        userRepository = new UserRepository(this);

        backButton.setOnClickListener(v -> finish());
        changeImageButton.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> saveProfileChanges());

        fetchUserFromFirebase();
    }

    private void fetchUserFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Disable nút Lưu cho đến khi dữ liệu về
        saveButton.setEnabled(false);

        userRepository.refreshUserData(new FirebaseCallback() {
            @Override
            public void onUserDataReceived(User user) {
                currentUser = user;
                populateUserData();
                saveButton.setEnabled(true);
            }

            @Override
            public void onSuccess(String message) { /* không dùng */ }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(EditProfileActivity.this,
                        "Lỗi tải dữ liệu: " + errorMessage,
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void populateUserData() {
        if (currentUser == null) return;

        nameEditText.setText(currentUser.getName());
        phoneEditText.setText(currentUser.getPhoneNumber());
        emailEditText.setText(currentUser.getEmail());
        emailEditText.setEnabled(false);

        String url = currentUser.getProfileImageUrl();
        if (url != null && !url.isEmpty()) {
            Glide.with(this)
                    .load(url)
                    .into(avatarImageView);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            newImageUri = data.getData();
            Glide.with(this)
                    .load(newImageUri)
                    .into(avatarImageView);
        }
    }

    private void saveProfileChanges() {
        String name  = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (name.isEmpty()) {
            nameEditText.setError("Tên không được để trống");
            return;
        }
        if (phone.isEmpty()) {
            phoneEditText.setError("Số điện thoại không được để trống");
            return;
        }

        // Tạo User mới để cập nhật
        User updated = new User();
        updated.setUserId(currentUser.getUserId());
        updated.setName(name);
        updated.setPhoneNumber(phone);
        updated.setEmail(currentUser.getEmail());
        updated.setProfileImageUrl(currentUser.getProfileImageUrl());
        updated.setFavoriteRecipeIds(currentUser.getFavoriteRecipeIds());
        updated.setCreatedAt(currentUser.getCreatedAt());
        updated.setUpdatedAt(currentUser.getUpdatedAt());

        // Disable nút Lưu khi đang update
        saveButton.setEnabled(false);

        userRepository.updateUserProfile(updated, newImageUri, new FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true);
            }
            @Override
            public void onUserDataReceived(User user) { /* không dùng */ }
        });
    }
}
