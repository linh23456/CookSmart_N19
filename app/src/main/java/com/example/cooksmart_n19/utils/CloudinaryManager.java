package com.example.cooksmart_n19.utils;

import android.content.Context;
import android.net.Uri;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {
    private static boolean isInitialized = false;

    // Khởi tạo Cloudinary
    public static void init(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dxctcszku");
            config.put("api_key", "726441944643878");
            config.put("api_secret", "m_y1seWpFjZkZbpC3WHcv4jrJAc");
            MediaManager.init(context.getApplicationContext(), config);
            isInitialized = true;
        }
    }

    // Tải ảnh lên Cloudinary với thư mục tùy chỉnh
    public static void uploadImage(Uri imageUri, String folder, CloudinaryUploadCallback callback) {
        try {
            MediaManager.get().upload(imageUri)
                    .option("folder", folder) // Thư mục trên Cloudinary: "users" hoặc "recipe"
                    .unsigned("cooksmart_upload")
                    .option("quality", "80") // Tối ưu hóa chất lượng
                    .option("fetch_format", "auto") // Tối ưu định dạng
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            callback.onStart();
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            callback.onProgress(bytes, totalBytes);
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = (String) resultData.get("secure_url");
                            callback.onSuccess(imageUrl);
                        }

                        @Override
                        public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                            callback.onError(error.getDescription());
                        }

                        @Override
                        public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                            callback.onError("Upload rescheduled: " + error.getDescription());
                        }
                    }).dispatch();
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    // Interface callback
    public interface CloudinaryUploadCallback {
        void onStart();
        void onProgress(long bytes, long totalBytes);
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }
}