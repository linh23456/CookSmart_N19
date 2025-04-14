package com.example.cooksmart_n19.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.adapters.MyRecipeAdapter;
import com.example.cooksmart_n19.models.Recipe;
import com.example.cooksmart_n19.repositories.MyRecipeRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MyRecipeActivity extends AppCompatActivity {
    private FloatingActionButton fabAddRecipe;
    private RecyclerView recyclerMyRecipe;
    private MyRecipeAdapter myRecipeAdapter;
    private List<Recipe> recipeList;
    private MyRecipeRepository repository;
    private String currentUserId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_recipe);
        mAuth = FirebaseAuth.getInstance();

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
            throw new IllegalStateException("Root view with ID 'mainMyRecipe' not found in activity_my_recipe.xml");
        }
        init();
        loadRecipes();
    }

    private void init() {
        // Lấy currentUserId từ FirebaseAuth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        } else {
            currentUserId = "guest";
            Toast.makeText(this, "Please log in to manage recipes", Toast.LENGTH_SHORT).show();
        }

        recyclerMyRecipe = findViewById(R.id.recyclerMyRecipe);
        recipeList = new ArrayList<>();
        myRecipeAdapter = new MyRecipeAdapter(recipeList, currentUserId,
                position -> {
                    Intent intent = new Intent(MyRecipeActivity.this, AddEditRecipeActivity.class);
                    intent.putExtra("recipe_id", recipeList.get(position).getRecipeId());
                    startActivityForResult(intent, 2);
                },
                new MyRecipeAdapter.OnRecipeActionListener() {
                    @Override
                    public void onEditClick(int position) {
                        Log.d("MyRecipeActivity", "Edit button clicked for position: " + position);
                        Intent intent = new Intent(MyRecipeActivity.this, AddEditRecipeActivity.class);
                        intent.putExtra("recipe_id", recipeList.get(position).getRecipeId());
                        startActivityForResult(intent, 2);
                    }

                    @Override
                    public void onDeleteClick(int position) {
                        Log.d("MyRecipeActivity", "Delete button clicked for position: " + position);
                        new AlertDialog.Builder(MyRecipeActivity.this)
                                .setTitle("Xóa công thức")
                                .setMessage("Bạn có chắc chắn muốn xóa công thức này?")
                                .setPositiveButton("Xóa", (dialog, which) -> {
                                    String recipeId = recipeList.get(position).getRecipeId();
                                    repository.deleteMyRecipe(recipeId, new MyRecipeRepository.DeleteMyRecipeCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(MyRecipeActivity.this, "Recipe deleted successfully", Toast.LENGTH_SHORT).show();
                                            loadRecipes();
                                        }

                                        @Override
                                        public void onError(String errorMessage) {
                                            Toast.makeText(MyRecipeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }
                });
        recyclerMyRecipe.setLayoutManager(new LinearLayoutManager(this));
        recyclerMyRecipe.setAdapter(myRecipeAdapter);
        repository = new MyRecipeRepository();
        fabAddRecipe = findViewById(R.id.fabAddRecipe);
        fabAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(MyRecipeActivity.this, AddEditRecipeActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    private void loadRecipes() {
        repository.getAllMyRecipe(mAuth.getCurrentUser().getUid(),new MyRecipeRepository.GetMRecipeCallback() {
            @Override
            public void onSuccess(List<Recipe> recipe) {
                recipeList.clear();
                recipeList.addAll(recipe);
                myRecipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MyRecipeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if ((requestCode == 1 || requestCode == 2) && resultCode == RESULT_OK) {
                loadRecipes();
            }
        }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_OK);
        finish();
        return true;
    }
}