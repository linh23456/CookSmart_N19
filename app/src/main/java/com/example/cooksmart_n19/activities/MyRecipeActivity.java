package com.example.cooksmart_n19.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cooksmart_n19.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MyRecipeActivity extends AppCompatActivity {
    private FloatingActionButton fabAddRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_recipe);

        // Find the root view
        View rootView = findViewById(R.id.mainMyRecipe);
        if (rootView != null) {
            // Handle system insets
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } else {
            // Log an error or handle the case where the view is not found
            // This is for debugging purposes; you can remove it after confirming the fix
            throw new IllegalStateException("Root view with ID 'main' not found in activity_my_recipe.xml");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Recipes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fabAddRecipe = findViewById(R.id.fabAddRecipe);
        fabAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(MyRecipeActivity.this, AddEditRecipeActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_OK);
        finish();
        return true;
    }
}