package com.example.cooksmart_n19.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cooksmart_n19.R;

public class IntroActivity extends AppCompatActivity {
    private ProgressBar loadingProgressBar;
    private TextView titleText;
    private TextView progressText;
    private ImageView forkImage;
    private ImageView knifeImage;
    private Handler handler;
    private Runnable loadingRunnable;
    private boolean isLoadingCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_intro);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Khởi tạo các view
        try {
            loadingProgressBar = findViewById(R.id.loadingProgressBar);
            titleText = findViewById(R.id.titleText);
            progressText = findViewById(R.id.progressText);
            forkImage = findViewById(R.id.forkImage);
            knifeImage = findViewById(R.id.knifeImage);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Khởi tạo Handler để điều khiển thanh loading
        handler = new Handler();

        // Tạo hiệu ứng dĩa và dao chập lại
        startForkKnifeAnimation();

        // Tạo hiệu ứng thanh loading chạy từ 0% đến 100% trong 5 giây
        startLoadingAnimation();
    }

    // Tạo hiệu ứng cho dĩa và dao
    private void startForkKnifeAnimation() {
        try {
            // Hiệu ứng cho dĩa: Di chuyển từ ngoài bên trái vào giữa
            ObjectAnimator forkAnimator = ObjectAnimator.ofFloat(forkImage, "translationX", -300f, 10f);
            forkAnimator.setDuration(1000); // 1 giây

            // Hiệu ứng cho dao: Di chuyển từ ngoài bên phải vào giữa
            ObjectAnimator knifeAnimator = ObjectAnimator.ofFloat(knifeImage, "translationX", 300f, 10f);
            knifeAnimator.setDuration(1000); // 1 giây

            // Kết hợp hai hiệu ứng để chạy cùng lúc
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(forkAnimator, knifeAnimator);
            animatorSet.start();

            // Tạo hiệu ứng "chập" (rung nhẹ khi chạm nhau)
            forkAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // Hiệu ứng rung nhẹ cho dĩa
                    ObjectAnimator forkShake = ObjectAnimator.ofFloat(forkImage, "translationX", 0f, 10f, -10f, 0f);
                    forkShake.setDuration(300); // 0.3 giây

                    // Hiệu ứng rung nhẹ cho dao
                    ObjectAnimator knifeShake = ObjectAnimator.ofFloat(knifeImage, "translationX", 0f, -10f, 10f, 0f);
                    knifeShake.setDuration(300); // 0.3 giây

                    AnimatorSet shakeSet = new AnimatorSet();
                    shakeSet.playTogether(forkShake, knifeShake);
                    shakeSet.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Tạo hiệu ứng thanh loading
    private void startLoadingAnimation() {
        try {
            final int totalDuration = 5000; // 5 giây
            final int updateInterval = 50; // Cập nhật mỗi 50ms
            final int totalSteps = totalDuration / updateInterval; // Số bước
            final int progressPerStep = 100 / totalSteps; // Tiến trình mỗi bước

            loadingProgressBar.setProgress(0); // Khởi tạo tiến trình là 0

            loadingRunnable = new Runnable() {
                int currentProgress = 0;

                @Override
                public void run() {
                    if (currentProgress < 100 && !isLoadingCompleted) {
                        currentProgress += progressPerStep;
                        if (currentProgress > 100) {
                            currentProgress = 100;
                        }
                        loadingProgressBar.setProgress(currentProgress);
                        progressText.setText(currentProgress + "%"); // Cập nhật phần trăm
                        handler.postDelayed(this, updateInterval);
                    } else {
                        // Thanh loading hoàn thành, chuyển sang MainActivity
                        if (!isLoadingCompleted) {
                            isLoadingCompleted = true;
                            startMainActivity();
                        }
                    }
                }
            };

            // Bắt đầu animation
            handler.post(loadingRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Dừng animation nếu Activity bị hủy
    private void stopLoadingAnimation() {
        if (loadingRunnable != null) {
            handler.removeCallbacks(loadingRunnable);
        }
    }

    // Chuyển đến MainActivity
    private void startMainActivity() {
        try {
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Đảm bảo dừng animation khi Activity bị hủy
        stopLoadingAnimation();
    }
}