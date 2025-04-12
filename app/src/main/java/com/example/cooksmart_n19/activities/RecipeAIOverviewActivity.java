package com.example.cooksmart_n19.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cooksmart_n19.R;

import java.util.ArrayList;

public class RecipeAIOverviewActivity extends AppCompatActivity {
    private ListView lvIngredients, lvSteps;
    private Button btnStartCooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_aioverview);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String dishName = intent.getStringExtra("dishName");
        ArrayList<String> ingredients = intent.getStringArrayListExtra("ingredients");
        ArrayList<String> steps = intent.getStringArrayListExtra("steps");

        // Hiển thị thông tin
        setupViews(dishName, ingredients, steps);
    }

    private void setupViews(String dishName, ArrayList<String> ingredients, ArrayList<String> steps) {
        // Hiển thị tên món
        TextView tvDishName = findViewById(R.id.tvDishName);
        tvDishName.setText(dishName);

        // Hiển thị nguyên liệu
        lvIngredients = findViewById(R.id.lvIngredients);
        ArrayAdapter<String> ingredientsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                ingredients
        );
        lvIngredients.setAdapter(ingredientsAdapter);

        // Hiển thị các bước
        lvSteps = findViewById(R.id.lvSteps);
        ArrayAdapter<String> stepsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                steps
        );
        lvSteps.setAdapter(stepsAdapter);

        // Xử lý nút Bắt đầu làm
        btnStartCooking = findViewById(R.id.btnStartCooking);
        btnStartCooking.setOnClickListener(v -> {
            // Lấy dữ liệu từ Intent
            ArrayList<String> imageUrls = getIntent().getStringArrayListExtra("imageUrls");

            Intent intent = new Intent(this, CookingAIStepsActivity.class);
            intent.putStringArrayListExtra("steps", steps); // Dùng biến steps từ tham số
            intent.putStringArrayListExtra("imageUrls", imageUrls);
            startActivity(intent);
        });
    }
}