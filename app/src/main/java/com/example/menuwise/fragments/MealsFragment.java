package com.example.menuwise.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menuwise.R;
import com.example.menuwise.adapters.RecipeAdapter;
import com.example.menuwise.models.Meal;
import com.example.menuwise.models.APIResponse;
import com.example.menuwise.network.MealApiService;
import com.example.menuwise.network.MealsClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MealsFragment extends Fragment {

    private static final String TAG = MealsFragment.class.getSimpleName();
    private String mealType;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private ProgressBar progressBar;
    private List<Meal> meals = new ArrayList<>();
    private Call<APIResponse> call;
    private final List<String> favoriteIds = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public MealsFragment() {}

    public static MealsFragment newInstance(String mealType) {
        MealsFragment fragment = new MealsFragment();
        Bundle args = new Bundle();
        args.putString("mealType", mealType);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meals, container, false);
        recyclerView = view.findViewById(R.id.recipeRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(requireContext(), meals);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            mealType = getArguments().getString("mealType");
        }
        if (auth.getCurrentUser() != null) {
            fetchFavoriteIdsAndRecipes();
        } else {
            Snackbar.make(requireView(), "Please log in first", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }

    private void fetchFavoriteIdsAndRecipes() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        db.collection("users").document(userId).collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteIds.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        favoriteIds.add(doc.getId());
                    }
                    fetchRecipesForMealType();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve favorites", e);
                    fetchRecipesForMealType();
                });
    }

    private void fetchRecipesForMealType() {
        MealApiService apiService = MealsClient.getApiClient();
        call = apiService.getRecipesByMealType(mealType);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse> call, @NonNull Response<APIResponse> response) {
                if (!isAdded() || getContext() == null) return;

                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    meals = response.body().getRecipes();
                    adapter.updateData(meals, favoriteIds);
                } else {
                    Snackbar.make(requireView(), "Failed to load meals", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<APIResponse> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Log.e(TAG, "API Failure: ", t);
                Snackbar.make(requireView(), "An error occurred while loading data: " + t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
