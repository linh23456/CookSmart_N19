package com.example.cooksmart_n19.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cooksmart_n19.R;

import java.util.List;

public class StepAIAdapter extends RecyclerView.Adapter<StepAIAdapter.StepViewHolder> {
    private List<String> steps;
    private String imageUrl; // Giữ nguyên nếu cần hiển thị ảnh

    public StepAIAdapter(List<String> steps, String imageUrl) {
        this.steps = steps;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stepai, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        String step = steps.get(position);
        holder.bind(step, imageUrl);
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStep;
        private ImageView ivImage;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStep = itemView.findViewById(R.id.tv_step);
            ivImage = itemView.findViewById(R.id.iv_image);
        }

        public void bind(String step, String imageUrl) {
            tvStep.setText(step);

            // Xử lý ảnh nếu cần
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .into(ivImage);
            } else {
                ivImage.setVisibility(View.GONE);
            }
        }
    }
}

