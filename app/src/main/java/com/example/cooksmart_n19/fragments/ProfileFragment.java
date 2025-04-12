package com.example.cooksmart_n19.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.activities.EditProfileActivity;
import com.example.cooksmart_n19.activities.MyRecipeActivity;
import com.example.cooksmart_n19.activities.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView profileName;
    private Button editProfileButton;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        editProfileButton = view.findViewById(R.id.edit_profile_button);

        // Các LinearLayout cho các mục menu
        view.findViewById(R.id.personal_page_layout).setOnClickListener(v -> onPersonalPageClicked());
        view.findViewById(R.id.notifications_layout).setOnClickListener(v -> onNotificationsClicked());
        view.findViewById(R.id.rate_app_layout).setOnClickListener(v -> onRateAppClicked());
        view.findViewById(R.id.app_info_layout).setOnClickListener(v -> onAppInfoClicked());
        view.findViewById(R.id.logout).setOnClickListener(v -> onLogoutClicked());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() != null) {
            String displayName = mAuth.getCurrentUser().getDisplayName();
            profileName.setText(displayName != null ? displayName : "Tên tài khoản");
            editProfileButton.setOnClickListener(v -> {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(getContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Chỉ khởi chạy EditProfileActivity, không cần đính kèm data
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            });
        } else {
            profileName.setText("Chưa đăng nhập");
            editProfileButton.setText("Đăng nhập");
            editProfileButton.setOnClickListener(v->{
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            });
        }
    }

    private void onPersonalPageClicked() {
       if(mAuth.getCurrentUser() != null){
           Intent intent = new Intent(getActivity(), MyRecipeActivity.class);
           startActivity(intent);
       }else{
           Toast.makeText(getContext(), "Vui lòng đăng nhập trước khi bắt đầu nấu ăn", Toast.LENGTH_SHORT).show();
           Intent intent = new Intent(getContext(), LoginActivity.class);
           startActivity(intent);
       }
    }

    private void onNotificationsClicked() {
        Toast.makeText(getContext(), "Mở thông báo", Toast.LENGTH_SHORT).show();
    }

    private void onRateAppClicked() {
        Toast.makeText(getContext(), "Mở đánh giá ứng dụng", Toast.LENGTH_SHORT).show();
    }

    private void onAppInfoClicked() {
        Toast.makeText(getContext(), "Mở thông tin ứng dụng", Toast.LENGTH_SHORT).show();
    }

    private void onLogoutClicked() {
        mAuth.signOut();
        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }
}