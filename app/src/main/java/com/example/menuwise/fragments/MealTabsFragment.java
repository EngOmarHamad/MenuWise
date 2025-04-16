package com.example.menuwise.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.menuwise.R;
import com.example.menuwise.adapters.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MealTabsFragment extends Fragment {

    // Tab titles for the recipe categories
    private static final String[] TAB_TITLES = {
            "Breakfast",
            "Lunch",
            "Dinner",
            "Dessert",
            "Snack",
            "Beverage",
            "Appetizer",
            "Side Dish",
    };


    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_meal_tabs, container, false);

        TabLayout tabLayout = view.findViewById(R.id.mealTypesTabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.recipesViewPager);

        // Create fragments dynamically for each meal type
        List<Fragment> recipeFragments = new ArrayList<>();
        for (String type : TAB_TITLES) {
            recipeFragments.add(MealsFragment.newInstance(type));
        }

        // Set up the ViewPager adapter with the fragments
        ViewPagerAdapter adapter = new ViewPagerAdapter(
                getChildFragmentManager(),
                getLifecycle(),
                recipeFragments
        );
        viewPager.setAdapter(adapter);

        // Attach the TabLayout with ViewPager using the tab titles
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();

        return view;
    }
}
