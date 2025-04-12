package com.example.cooksmart_n19.activities;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.adapters.StepPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class CookingAIActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private Button btnPrevious, btnNext;
    private StepPagerAdapter adapter;
    private List<String> steps;
    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooking_aiactivity);

        // Nhận dữ liệu
        Parcelable parcelable = getIntent().getParcelableExtra("selected_recipe");
        if (parcelable instanceof RecipeAIListActivity.Recipe) {
            RecipeAIListActivity.Recipe recipe = (RecipeAIListActivity.Recipe) parcelable;
            setupUI(recipe);
        } else {
            finish();
        }
    }

    private void setupUI(RecipeAIListActivity.Recipe recipe) {
        TextView tvRecipeName = findViewById(R.id.tv_recipe_name);
        TextView tvCookingTime = findViewById(R.id.tv_cooking_time);
        ImageView ivRecipe = findViewById(R.id.iv_recipe);
        viewPager = findViewById(R.id.view_pager);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);

        tvRecipeName.setText(recipe.getName());
        tvCookingTime.setText("Thời gian nấu: " + recipe.getCookingTime());

        Glide.with(this)
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(ivRecipe);

        steps = recipe.getSteps() != null ? recipe.getSteps() : new ArrayList<>();

        adapter = new StepPagerAdapter(steps);
        viewPager.setAdapter(adapter);

        updateButtonState();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                updateButtonState();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentPosition > 0) {
                viewPager.setCurrentItem(currentPosition - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPosition < steps.size() - 1) {
                viewPager.setCurrentItem(currentPosition + 1);
            } else {
                finishCooking();
            }
        });
    }

    private void updateButtonState() {
        btnPrevious.setVisibility(currentPosition > 0 ? View.VISIBLE : View.INVISIBLE);

        if (currentPosition == steps.size() - 1) {
            btnNext.setText("Hoàn thành");
        } else {
            btnNext.setText("Tiếp theo");
        }
    }

    private void finishCooking() {
        new AlertDialog.Builder(this)
                .setTitle("Hoàn thành")
                .setMessage("Bạn đã hoàn thành món ăn!")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    private static class CookingStepAdapter extends RecyclerView.Adapter<CookingStepAdapter.ViewHolder> {
        private final List<String> steps;

        public CookingStepAdapter(List<String> steps) {
            this.steps = steps;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cooking_stepai, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvStepNumber.setText("Bước " + (position + 1));
            holder.tvStepContent.setText(steps.get(position));
        }

        @Override
        public int getItemCount() {
            return steps.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStepNumber, tvStepContent;

            ViewHolder(View itemView) {
                super(itemView);
                tvStepNumber = itemView.findViewById(R.id.tv_step_number);
                tvStepContent = itemView.findViewById(R.id.tv_step_content);
            }
        }
    }
}