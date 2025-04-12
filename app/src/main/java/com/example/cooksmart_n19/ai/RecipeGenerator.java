package com.example.cooksmart_n19.ai;

import android.content.Context;
import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RecipeGenerator {
    private static final String TAG = "RecipeGenerator";
    private final GenerativeModelFutures model;
    private final Context context;

    public interface RecipeCallback {
        void onSuccess(Map<String, Object> recipeData);
        void onError(String errorMessage);
    }

    public RecipeGenerator(Context context, String apiKey) {
        this.context = context;
        GenerativeModel gm = new GenerativeModel("gemini-2.0-flash", apiKey);
        model = GenerativeModelFutures.from(gm);
    }

    public void generateRecipe(List<String> ingredients, RecipeCallback callback) {
        String prompt = buildSmartPrompt(ingredients);
        Content content = new Content.Builder().addText(prompt).build();

        Futures.addCallback(
                model.generateContent(content),
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        try {
                            String responseText = result.getText();
                            Log.d(TAG, "API Response: " + responseText);

                            Map<String, Object> recipeData = parseStructuredResponse(responseText);
                            if (validateRecipe(recipeData)) {
                                callback.onSuccess(recipeData);
                            } else {
                                callback.onError("Invalid recipe format");
                            }
                        } catch (Exception e) {
                            callback.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        callback.onError(t.getMessage());
                    }
                },
                MoreExecutors.directExecutor()
        );
    }

    private String buildSmartPrompt(List<String> ingredients) {
        return "Hãy trả về công thức nấu ăn theo định dạng sau:\n\n"
                + "TÊN MÓN: [Tên món ăn]\n"
                + "THỜI GIAN: [Thời gian nấu]\n"
                + "NGUYÊN LIỆU:\n- [Nguyên liệu 1]\n- [Nguyên liệu 2]\n"
                + "CÁC BƯỚC:\n1. [Bước 1]\n2. [Bước 2]\n\n"
                + "Nguyên liệu có sẵn: " + String.join(", ", ingredients);
    }

    private Map<String, Object> parseStructuredResponse(String response) {
        Map<String, Object> data = new HashMap<>();
        try {
            // Chuẩn hóa phản hồi
            String normalizedResponse = response.replace("：", ":") // Chuẩn hóa dấu hai chấm
                    .replaceAll("[*#•]", "") // Loại bỏ ký tự đặc biệt
                    .trim();

            // Trích xuất các thành phần
            data.put("title", extractValue(normalizedResponse, "TÊN MÓN", "Tên món"));
            data.put("cooking_time", extractValue(normalizedResponse, "THỜI GIAN", "Thời gian"));
            data.put("ingredients", extractList(normalizedResponse, "NGUYÊN LIỆU", "Nguyên liệu"));
            data.put("steps", extractList(normalizedResponse, "CÁC BƯỚC", "Hướng dẫn"));

            // Kiểm tra dữ liệu trống
            if (data.get("title").toString().isEmpty()) {
                throw new Exception("Thiếu tên món ăn");
            }
            if (((List<?>) data.get("ingredients")).isEmpty()) {
                data.put("ingredients", Collections.singletonList("Nguyên liệu đang cập nhật"));
            }
            if (((List<?>) data.get("steps")).isEmpty()) {
                data.put("steps", Collections.singletonList("Hướng dẫn sẽ được bổ sung"));
            }

        } catch (Exception e) {
            Log.e(TAG, "Lỗi xử lý dữ liệu: " + e.getMessage());
            data.put("error", "Dữ liệu không hợp lệ: " + e.getMessage());
        }
        List<String> steps = extractSteps(response);
        data.put("steps", steps != null ? steps : new ArrayList<>());
        return data;
    }

    private String extractValue(String text, String... keywords) {
        for (String keyword : keywords) {
            Pattern pattern = Pattern.compile(
                    "(?i)" + keyword + "[\\s:]*([^\\n]+)",
                    Pattern.DOTALL
            );
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }
        return "";
    }

    private List<String> extractList(String text, String... sectionNames) {
        for (String section : sectionNames) {
            Pattern pattern = Pattern.compile(
                    "(?i)" + section + "[\\s:]*([\\s\\S]+?)(?=\\n\\s*" + String.join("|", sectionNames) + "|$)",
                    Pattern.DOTALL
            );
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String content = matcher.group(1).trim();
                return Arrays.stream(content.split("\\n"))
                        .map(line -> line.replaceAll("^[-*\\d+]\\s*", "").trim())
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    private String extractTitle(String text) {
        // Tìm kiếm theo nhiều định dạng khác nhau
        Pattern[] patterns = {
                Pattern.compile("TÊN MÓN:?\\s*(.+?)(\\n|$)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("Tên món:?\\s*(.+?)(\\n|$)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("^(.+?)\\n", Pattern.MULTILINE)
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String title = matcher.group(1).trim();
                return title.replaceAll("[*#]", ""); // Loại bỏ ký tự đặc biệt
            }
        }
        return "Tên món không xác định";
    }

    private String extractCookingTime(String text) {
        Pattern pattern = Pattern.compile(
                "(Thời gian nấu|Thời gian|Time):?\\s*(.+?)(\\n|$)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(2).trim() : "Không xác định";
    }

    private List<String> extractIngredients(String text) {
        return extractListSection(text,
                Arrays.asList("NGUYÊN LIỆU", "Nguyên liệu", "Ingredients"),
                "-\\s*", "\\d+\\.\\s*"
        );
    }

    private List<String> extractSteps(String text) {
        return extractListSection(text,
                Arrays.asList("CÁC BƯỚC", "Hướng dẫn", "Steps"),
                "\\d+\\.\\s*", "-\\s*"
        );
    }

    private List<String> extractListSection(String text, List<String> sectionNames,
                                            String... itemPrefixes) {
        for (String sectionName : sectionNames) {
            Pattern sectionPattern = Pattern.compile(
                    sectionName + "[\\s:]*([\\s\\S]+?)(?=\\n\\s*" + String.join("|", sectionNames) + "|$)",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher sectionMatcher = sectionPattern.matcher(text);
            if (sectionMatcher.find()) {
                String sectionContent = sectionMatcher.group(1).trim();
                List<String> items = new ArrayList<>();

                // Thử nhiều cách tách items
                String[] lines = sectionContent.split("\\n");
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        // Loại bỏ các prefix khác nhau
                        for (String prefix : itemPrefixes) {
                            line = line.replaceFirst("^" + prefix, "");
                        }
                        items.add(line);
                    }
                }

                if (!items.isEmpty()) {
                    return items;
                }
            }
        }
        return Collections.singletonList("Thông tin đang được cập nhật");
    }
    private String extractSection(String text, String sectionName) {
        Pattern pattern = Pattern.compile(sectionName + ":\\s*(.+?)(?=\\n\\n|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private List<String> extractList(String text, String sectionName) {
        List<String> items = new ArrayList<>();
        Pattern pattern = Pattern.compile(sectionName + ":\\s*((?:\\n- .+?)+)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String[] lines = matcher.group(1).split("\\n- ");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    items.add(line.replaceFirst("^- ", "").trim());
                }
            }
        }
        return items.isEmpty() ? Collections.singletonList("Đang cập nhật") : items;
    }

    private boolean validateRecipe(Map<String, Object> recipeData) {
        // Kiểm tra nâng cao
        boolean isValid = true;

        if (recipeData.get("title").toString().trim().isEmpty()) {
            Log.w(TAG, "Invalid title");
            isValid = false;
        }

        List<String> ingredients = (List<String>) recipeData.get("ingredients");
        if (ingredients == null || ingredients.isEmpty() ||
                ingredients.stream().anyMatch(String::isEmpty)) {
            Log.w(TAG, "Invalid ingredients");
            isValid = false;
        }

        List<String> steps = (List<String>) recipeData.get("steps");
        if (steps == null || steps.isEmpty() ||
                steps.stream().anyMatch(String::isEmpty)) {
            Log.w(TAG, "Invalid steps");
            isValid = false;
        }

        return isValid;
    }
}
