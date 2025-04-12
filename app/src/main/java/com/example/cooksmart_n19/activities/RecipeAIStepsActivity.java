package com.example.cooksmart_n19.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.adapters.StepAIAdapter;
import com.example.cooksmart_n19.R;

import java.util.ArrayList;

public class RecipeAIStepsActivity extends AppCompatActivity {
    private RecyclerView rvSteps;
    private StepAIAdapter stepAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_aisteps);

        // Nhận dữ liệu trực tiếp dạng String
        ArrayList<String> steps = getIntent().getStringArrayListExtra("steps");
        String imageUrl = getIntent().getStringExtra("image");

        // Thiết lập RecyclerView
        rvSteps = findViewById(R.id.rvSteps);
        rvSteps.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo Adapter với dữ liệu đơn giản
        stepAdapter = new StepAIAdapter(steps, imageUrl);
        rvSteps.setAdapter(stepAdapter);

        setupActionButtons();
    }

    private void setupActionButtons() {
        Button btnRestart = findViewById(R.id.btn_restart);
        Button btnNext = findViewById(R.id.btn_next);

        btnRestart.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> goToNextStep());
    }

    private void goToNextStep() {
        // Logic chuyển bước (nếu cần)
    }
}