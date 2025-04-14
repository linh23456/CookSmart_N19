package com.example.cooksmart_n19.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.adapters.StepPagerAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class RecipeAIStepsActivity extends AppCompatActivity
        implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private ViewPager2 viewPager;
    private StepPagerAdapter adapter;
    private ImageButton btnPlayPause;
    private boolean isSpeaking = false;
    private ArrayList<String> steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_aisteps);

        // Nhận dữ liệu các bước
        steps = getIntent().getStringArrayListExtra("steps");
        if(steps == null || steps.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu các bước", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Khởi tạo TTS
        tts = new TextToSpeech(this, this);

        setupViewPager();
        setupControls();
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.viewPagerSteps);
        adapter = new StepPagerAdapter(steps);
        viewPager.setAdapter(adapter);

        // Xử lý chuyển trang
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(isSpeaking) {
                    speakCurrentStep();
                }
            }
        });
    }

    private void setupControls() {
        Button btnPrevious = findViewById(R.id.btnPrevious);
        Button btnNext = findViewById(R.id.btnNext);
        Button btnExit = findViewById(R.id.btnExit);
        btnPlayPause = findViewById(R.id.btnPlayPause);

        // Xử lý nút điều khiển
        btnPrevious.setOnClickListener(v -> navigateToPreviousStep());
        btnNext.setOnClickListener(v -> navigateToNextStep());
        btnExit.setOnClickListener(v -> finish());
        btnPlayPause.setOnClickListener(v -> toggleSpeech());
    }

    private void navigateToPreviousStep() {
        if(viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

//    private void navigateToMain(){
//        Intent intent = new Intent(AssistantFragment.class , RecipeAIStepsActivity.class);
//
//    }
    private void navigateToNextStep() {
        if(viewPager.getCurrentItem() < steps.size() - 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
    }

    private void toggleSpeech() {
        if(isSpeaking) {
            pauseSpeaking();
        } else {
            startSpeaking();
        }
        updatePlayButton();
    }

    private void startSpeaking() {
        if(!isSpeaking) {
            speakCurrentStep();
            isSpeaking = true;
        }
    }

    private void pauseSpeaking() {
        if(tts != null && tts.isSpeaking()) {
            tts.stop();
            isSpeaking = false;
        }
    }

    private void speakCurrentStep() {
        if(tts != null) {
            String text = steps.get(viewPager.getCurrentItem());
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "STEP_" + viewPager.getCurrentItem());
        }
    }

    private void updatePlayButton() {
        btnPlayPause.setImageResource(
                isSpeaking ? R.drawable.ic_pause : R.drawable.ic_play
        );
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {
            setupTTSLanguage();
        } else {
            handleTTSInitError();
        }
    }

    private void setupTTSLanguage() {
        int result = tts.setLanguage(Locale.getDefault());

        // Fallback to English if default language not supported
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            result = tts.setLanguage(Locale.ENGLISH);
        }

        if (result == TextToSpeech.LANG_MISSING_DATA) {
            handleMissingLanguageData();
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            showLanguageNotSupportedError();
        } else {
            setupSpeechListener();
        }
    }

    private void setupSpeechListener() {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                runOnUiThread(() -> {
                    isSpeaking = true;
                    updatePlayButton();
                });
            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(() -> {
                    isSpeaking = false;
                    updatePlayButton();
                });
            }

            @Override
            public void onError(String utteranceId) {
                runOnUiThread(() -> {
                    isSpeaking = false;
                    updatePlayButton();
                    Toast.makeText(RecipeAIStepsActivity.this,
                            "Lỗi phát âm thanh", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void handleMissingLanguageData() {
        new AlertDialog.Builder(this)
                .setTitle("Missing Language Data")
                .setMessage("Please install TTS language data")
                .setPositiveButton("Install", (d, w) -> {
                    Intent installIntent = new Intent();
                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLanguageNotSupportedError() {
        Toast.makeText(this,
                "Language not supported",
                Toast.LENGTH_LONG).show();
    }

    private void handleTTSInitError() {
        Toast.makeText(this,
                "Text-to-Speech initialization failed",
                Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(tts != null && tts.isSpeaking()) {
            tts.stop();
        }
    }
}