package com.example.cooksmart_n19.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;


import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.adapters.StepPagerAdapter;
import com.example.cooksmart_n19.fragments.AssistantFragment;

import java.util.List;

public class CookingAIStepsActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private List<String> steps;
    private Button btnPrevious , btnNext , btnExit;
    private List<String> imageUrls; // Thêm danh sách ảnh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooking_aisteps);

        // Nhận dữ liệu từ Intent
        steps = getIntent().getStringArrayListExtra("steps");
        imageUrls = getIntent().getStringArrayListExtra("imageUrls"); // Thêm dòng này

        setupViewPager();
        setupNavigationButtons();
        btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> {
            finish();
        });
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.viewPagerSteps);
        StepPagerAdapter adapter = new StepPagerAdapter(steps); // Truyền cả 2 tham số
        viewPager.setAdapter(adapter);
    }

    private void setupNavigationButtons() {
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);

        // Xử lý nút điều hướng
        btnPrevious.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < steps.size() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        // Cập nhật trạng thái nút
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateButtonState(position);
            }
        });
    }

    private void updateButtonState(int position) {
        btnPrevious.setEnabled(position > 0);
        btnNext.setEnabled(position < steps.size() - 1);
    }
}