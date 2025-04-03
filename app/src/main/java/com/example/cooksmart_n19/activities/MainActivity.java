package com.example.cooksmart_n19.activities;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.fragments.AssistantFragment;
import com.example.cooksmart_n19.fragments.ExploreFragment;
import com.example.cooksmart_n19.fragments.FavoriteFragment;
import com.example.cooksmart_n19.fragments.HomeFragment;
import com.example.cooksmart_n19.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private static final String SELECTED_TAB_KEY = "selected_tab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_search) {
                loadFragment(new ExploreFragment());
                return true;
            } else if (itemId == R.id.nav_favorites) {
                loadFragment(new FavoriteFragment());
                return true;
            } else if (itemId == R.id.nav_assistant) {
                loadFragment(new AssistantFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else {
            int selectedTab = savedInstanceState.getInt(SELECTED_TAB_KEY, R.id.nav_home);
            bottomNavigation.setSelectedItemId(selectedTab);
        }
    }
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Add to back stack
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAB_KEY, bottomNavigation.getSelectedItemId());
    }
}