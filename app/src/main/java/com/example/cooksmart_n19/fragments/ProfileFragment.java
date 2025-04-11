package com.example.cooksmart_n19.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.activities.EditProfileActivity;
import com.example.cooksmart_n19.activities.MyRecipeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private ActivityResultLauncher<Intent> myRecipeLauncher;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Khởi tạo FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // 2. Đăng ký launcher để nhận kết quả từ MyRecipeActivity
        myRecipeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(getContext(), "Quay về từ MyRecipeActivity", Toast.LENGTH_SHORT).show();
                        // TODO: refresh UI nếu cần
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ nút "Chỉnh sửa hồ sơ"
        Button editProfileBtn = view.findViewById(R.id.edit_profile_button);
        editProfileBtn.setOnClickListener(v -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }
            // Chỉ khởi chạy EditProfileActivity, không cần đính kèm data
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // Ánh xạ layout "Trang cá nhân" để mở MyRecipeActivity
        LinearLayout personalLayout = view.findViewById(R.id.personal_page_layout);
        personalLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyRecipeActivity.class);
            myRecipeLauncher.launch(intent);
        });

        return view;
    }
}
