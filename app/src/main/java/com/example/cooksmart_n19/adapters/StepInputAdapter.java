package com.example.cooksmart_n19.adapters;

import android.util.Log;
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

public class StepInputAdapter extends RecyclerView.Adapter<StepInputAdapter.StepInputViewHolder> {
    private List<Recipe.CookingStep> cookingStepList;
    private OnStepClickListener stepClickListener;

    public StepInputAdapter(List<Recipe.CookingStep> steps, OnStepClickListener stepClickListener) {
        this.cookingStepList = steps;
        this.stepClickListener = stepClickListener;
    }

    @NonNull
    @Override
    public StepInputViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_step, parent, false);
        return new StepInputViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepInputViewHolder holder, int position) {
        Recipe.CookingStep cookingStep = cookingStepList.get(position);
        holder.bind(cookingStep, position);
    }

    @Override
    public int getItemCount() {
        return cookingStepList.size();
    }

    public void updateSteps(List<Recipe.CookingStep> newStepList) {
        this.cookingStepList = newStepList;
        notifyDataSetChanged();
    }

    public class StepInputViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewStep;
        private TextView textViewStepNumber, textViewStepDescription;

        public StepInputViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewStep = itemView.findViewById(R.id.imageViewStep);
            textViewStepNumber = itemView.findViewById(R.id.textViewStepNumber);
            textViewStepDescription = itemView.findViewById(R.id.textViewStepDescription);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && stepClickListener != null) {
                    stepClickListener.onStepClick(position);
                }
            });
        }

        public void bind(Recipe.CookingStep step, int position) {
            textViewStepNumber.setText("Bước " + (position + 1));
            textViewStepDescription.setText(step.getInstruction());
            Log.d("StepImage", "Image URL: " + step.getImages());

            if (step.getImages() != null && !step.getImages().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(step.getImages())
                        .placeholder(R.drawable.rice)
                        .error(R.drawable.rice)
                        .into(imageViewStep);
                imageViewStep.setVisibility(View.VISIBLE);
            } else {
                imageViewStep.setImageResource(R.drawable.rice); // Ảnh mặc định
                imageViewStep.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnStepClickListener {
        void onStepClick(int position);
    }
}