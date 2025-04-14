package com.example.cooksmart_n19.adapters;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.models.CookingStep;

import java.util.List;
import java.util.Locale;

public class CookingStepAdapter extends RecyclerView.Adapter<CookingStepAdapter.StepViewHolder> {

    private List<CookingStep> steps;
    private TextToSpeech textToSpeech;

    public CookingStepAdapter(Context context, List<CookingStep> steps) {
        this.steps = steps;

        // Khởi tạo TextToSpeech
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("vi", "VN")); // Ngôn ngữ Tiếng Việt
            } else {
                Toast.makeText(context, "Không thể khởi tạo TextToSpeech!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cooking_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        CookingStep step = steps.get(position);

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

        // Xử lý nút phát TTS
        holder.playTtsButton.setOnClickListener(v -> {
                String textToRead = "Bước " + step.getStepNumber() + ". " + step.getInstruction();
                textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null);
        });

        // Xử lý nút dừng TTS
        holder.stopTtsButton.setOnClickListener(v -> {
                textToSpeech.stop();
        });
    }

    @Override
    public int getItemCount() {
        return steps != null ? steps.size() : 0;
    }

    // Dọn dẹp TextToSpeech khi adapter bị hủy
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        ImageView stepImage;
        TextView stepTitle;
        TextView stepInstruction;
        ImageButton playTtsButton;
        ImageButton stopTtsButton;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepImage = itemView.findViewById(R.id.step_image);
            stepTitle = itemView.findViewById(R.id.step_title);
            stepInstruction = itemView.findViewById(R.id.step_instruction);
            playTtsButton = itemView.findViewById(R.id.playTtsButton);
            stopTtsButton = itemView.findViewById(R.id.stopTtsButton);
        }
    }
}