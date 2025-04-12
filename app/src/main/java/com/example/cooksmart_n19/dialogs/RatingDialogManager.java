package com.example.cooksmart_n19.dialogs;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.repositories.MyRecipeRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RatingDialogManager {
    private final Context context;
    private final MyRecipeRepository repository;
    private final String recipeId;
    private final OnRatingSubmittedCallback callback;

    public interface OnRatingSubmittedCallback {
        void onRatingSubmitted(double averageRating, int ratingCount);
        void onError(String errorMessage);
    }

    public RatingDialogManager(Context context, String recipeId, OnRatingSubmittedCallback callback) {
        this.context = context;
        this.repository = new MyRecipeRepository();
        this.recipeId = recipeId;
        this.callback = callback;
    }

    public void showRatingDialog() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        repository.checkIfUserRated(recipeId, userId, new MyRecipeRepository.OnCheckUserRatedListener() {
            @Override
            public void onUserRated(float rating) {
                Toast.makeText(context, "Bạn đã đánh giá món ăn này: " + rating + " sao", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUserNotRated() {
                View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rate_recipe, null);
                RatingBar ratingBarDialog = dialogView.findViewById(R.id.ratingBarDialog);
                Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
                Button buttonSubmit = dialogView.findViewById(R.id.buttonSubmit);

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setView(dialogView)
                        .setCancelable(false)
                        .create();

                buttonCancel.setOnClickListener(v -> dialog.dismiss());

                buttonSubmit.setOnClickListener(v -> {
                    float rating = ratingBarDialog.getRating();
                    if (rating == 0.0f) {
                        Toast.makeText(context, "Vui lòng chọn số sao để đánh giá", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    repository.submitRating(recipeId, userId, rating, new MyRecipeRepository.OnRatingSubmittedListener() {
                        @Override
                        public void onSuccess(double averageRating, int ratingCount) {
                            Toast.makeText(context, "Đánh giá của bạn đã được gửi", Toast.LENGTH_SHORT).show();
                            callback.onRatingSubmitted(averageRating, ratingCount);

                            dialog.dismiss();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(context, "Lỗi gửi đánh giá: " + errorMessage, Toast.LENGTH_SHORT).show();
                            Log.d("Rating Recipe", errorMessage);
                            callback.onError(errorMessage);
                        }
                    });
                });

                dialog.show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(context, "Lỗi kiểm tra đánh giá: " + errorMessage, Toast.LENGTH_SHORT).show();
                callback.onError(errorMessage);
            }
        });
    }
}