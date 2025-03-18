package com.example.cooksmart_n19.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.adapters.MainRecycleViewAdapter;
import com.example.cooksmart_n19.models.Category;
import com.example.cooksmart_n19.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private MainRecycleViewAdapter mainRecycleViewAdapter;
    private List<Category> categoryList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.mainRecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tạo danh sách Category
        categoryList = new ArrayList<>();

        // Tạo danh sách Recipe mẫu
        List<Recipe> recipeList = new ArrayList<>();
        recipeList.add(new Recipe("https://upload.wikimedia.org/wikipedia/commons/a/a3/Eq_it-na_pizza-margherita_sep2005_sml.jpg", "Pizza Italian", 15, "3.1K"));
        recipeList.add(new Recipe("https://upload.wikimedia.org/wikipedia/commons/a/a3/Eq_it-na_pizza-margherita_sep2005_sml.jpg", "Pizza Italian", 15, "3.1K"));
        recipeList.add(new Recipe("https://upload.wikimedia.org/wikipedia/commons/a/a3/Eq_it-na_pizza-margherita_sep2005_sml.jpg", "Pizza Italian", 15, "3.1K"));
        recipeList.add(new Recipe("https://upload.wikimedia.org/wikipedia/commons/a/a3/Eq_it-na_pizza-margherita_sep2005_sml.jpg", "Pizza Italian", 15, "3.1K"));

        // Tạo một Category và thêm vào danh sách categoryList
        categoryList.add(new Category(recipeList, "The latest"));
        categoryList.add(new Category(recipeList, "The latest"));
        categoryList.add(new Category(recipeList, "The latest"));

        // Tạo Adapter và thiết lập cho RecyclerView
        mainRecycleViewAdapter = new MainRecycleViewAdapter(categoryList, getContext());
        recyclerView.setAdapter(mainRecycleViewAdapter);

        return view;
    }
}
