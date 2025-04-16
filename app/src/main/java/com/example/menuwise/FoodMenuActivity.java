package com.example.menuwise;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.menuwise.databinding.ActivityMealListBinding;
import com.example.menuwise.fragments.FavoritesFragment;
import com.example.menuwise.fragments.MealTabsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class FoodMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMealListBinding binding = ActivityMealListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        setupToolbar();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.recipe_fragment_container, new MealTabsFragment())
                    .commit();
        }

        setupBottomNavigation();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Drawable overflowIcon = toolbar.getOverflowIcon();
        if (overflowIcon != null) {
            int color = ContextCompat.getColor(this, R.color.appbar_icon_color);
            overflowIcon.setTint(color);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.item_home) {
                loadFragment(new FavoritesFragment());
                return true;
            } else if (id == R.id.item_favorite) {
                loadFragment(new FavoritesFragment());
                return true;
            } else if (id == R.id.item_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, SignInActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.recipe_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

}
