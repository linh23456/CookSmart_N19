package com.example.cooksmart_n19.activities;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.adapters.RecipeAIAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RecipeAIListActivity extends AppCompatActivity {
    private List<Recipe> recipes = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_ailist);

        showLoadingDialog();
        parseIntentData();
        setupRecyclerView();
    }

    private void parseIntentData() {
        if (getIntent().hasExtra("recipes")) {
            String jsonString = getIntent().getStringExtra("recipes");
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Recipe recipe = new Recipe(
                            jsonObject.optString("title", "Tên món"),
                            jsonObject.optString("cooking_time", "Thời gian chưa xác định"),
                            jsonObject.optString("image_url", ""),
                            convertJsonArrayToList(jsonObject.getJSONArray("ingredients")),
                            convertJsonArrayToList(jsonObject.getJSONArray("steps"))
                    );
                    recipes.add(recipe);
                }
            } catch (JSONException e) {
                Log.e("RecipeList", "Error parsing JSON", e);
            }
        }
    }

    private List<String> convertJsonArrayToList(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }


    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải công thức...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public class Recipe implements Parcelable {
        private String name;
        private String cookingTime;
        private String imageUrl;
        private List<String> ingredients;
        private List<String> steps;

        public Recipe(String name, String cookingTime, String imageUrl,
                      List<String> ingredients, List<String> steps) {
            this.name = name;
            this.cookingTime = cookingTime;
            this.imageUrl = imageUrl;
            this.ingredients = ingredients;
            this.steps = steps != null ? steps : new ArrayList<>();
        }

        // Parcelable implementation
        protected Recipe(Parcel in) {
            name = in.readString();
            cookingTime = in.readString();
            imageUrl = in.readString();
            ingredients = in.createStringArrayList();
            steps = in.createStringArrayList();
        }

        public final Creator<Recipe> CREATOR = new Creator<Recipe>() {
            @Override
            public Recipe createFromParcel(Parcel in) {
                return new Recipe(in);
            }

            @Override
            public Recipe[] newArray(int size) {
                return new Recipe[size];
            }
        };

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(cookingTime);
            dest.writeString(imageUrl);
            dest.writeStringList(ingredients);
            dest.writeStringList(steps);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        // Getters
        public String getName() { return name; }
        public String getCookingTime() { return cookingTime; }
        public String getImageUrl() { return imageUrl; }
        public List<String> getIngredients() { return ingredients; }
        public List<String> getSteps() { return steps; }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_recipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Thêm divider giữa các item
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        RecipeAIAdapter adapter = new RecipeAIAdapter(recipes, recipe -> {
            Intent intent = new Intent(this, CookingAIActivity.class);
            intent.putExtra("selected_recipe", recipe);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }
}