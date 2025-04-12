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
import com.example.cooksmart_n19.models.Recipe;

import java.util.List;

public class CookingStepAdapter extends RecyclerView.Adapter<CookingStepAdapter.StepViewHolder> {

    private List<Recipe.CookingStep> steps;

    public CookingStepAdapter(List<Recipe.CookingStep> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cooking_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        Recipe.CookingStep step = steps.get(position);

        // Cập nhật tiêu đề bước
        holder.stepTitle.setText("Bước " + step.getStepNumber());

        // Cập nhật nội dung hướng dẫn
        holder.stepInstruction.setText(step.getInstruction());

        // Cập nhật hình ảnh
        Glide.with(holder.itemView.getContext())
                .load(step.getImages())
                .placeholder(R.drawable.recipe_placeholder)
                .error(R.drawable.recipe_placeholder)
                .into(holder.stepImage);
    }

    @Override
    public int getItemCount() {
        return steps != null ? steps.size() : 0;
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        ImageView stepImage;
        TextView stepTitle;
        TextView stepInstruction;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepImage = itemView.findViewById(R.id.step_image);
            stepTitle = itemView.findViewById(R.id.step_title);
            stepInstruction = itemView.findViewById(R.id.step_instruction);
        }
    }
}