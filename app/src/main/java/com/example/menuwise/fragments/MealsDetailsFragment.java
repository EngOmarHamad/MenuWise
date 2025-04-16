package com.example.menuwise.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.menuwise.R;
import com.example.menuwise.databinding.FragmentMealDetailsBinding;
import com.example.menuwise.models.Meal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MealsDetailsFragment extends Fragment {

    private FragmentMealDetailsBinding binding;
    private Meal meal;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private boolean isFavorite;

    public MealsDetailsFragment() {
    }

    public static MealsDetailsFragment newInstance(Meal meal) {
        MealsDetailsFragment fragment = new MealsDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("meal", meal);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMealDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private AlertDialog loadingDialog;

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.loading, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            meal = (Meal) getArguments().getParcelable("meal");
            isFavorite = getArguments().getBoolean("isFavorite", false);
        }

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (meal != null) {
            // Displaying the meal image
            Glide.with(requireContext()).load(meal.getImage()).into(binding.imageView);

            // Setting the meal name, price, and time
            binding.nameTextView.setText(meal.getName());
            binding.priceTextView.setText(meal.getCuisine());
            binding.preparationTimeTextView.setText(String.format("Preparation Time: %s", meal.getDifficulty()));  // Adjust as needed

            // Adding ingredients and steps
            binding.ingredientsTextView.setText(String.format("Ingredients: %s", String.join(", ", meal.getIngredients())));
            List<String> instructionsList = meal.getInstructions();
            StringBuilder instructionsText = new StringBuilder();

            for (int i = 0; i < instructionsList.size(); i++)
                instructionsText.append(String.format(Locale.US, "%d. %s\n", i + 1, instructionsList.get(i)));

            binding.instructionsTextView.setText(instructionsText.toString());
            // Add meal types to ChipGroup
            ChipGroup chipGroup = binding.mealTypeChipGroup;
            for (String mealType : meal.getMealType()) {
                Chip chip = new Chip(requireContext());
                chip.setText(mealType);
                chip.setChipBackgroundColorResource(R.color.colorPrimary); // Define in colors.xml
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                chipGroup.addView(chip);
            }

            // Update the favorite button
            if (isFavorite) {
                binding.favoriteButton.setText(R.string.remove_from_favorites);
                binding.favoriteButton.setIconResource(R.drawable.ic_favorite_filled);
                binding.favoriteButton.setOnClickListener(v -> removeFromFavorites());
            } else {
                binding.favoriteButton.setText(R.string.add_to_favorites);
                binding.favoriteButton.setIconResource(R.drawable.ic_favorite_border);
                binding.favoriteButton.setOnClickListener(v -> addToFavorites());
            }
        }
    }

    private void removeFromFavorites() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId != null) {
            showLoadingDialog();

            firestore.collection("users").document(userId)
                    .collection("favorites").document(String.valueOf(meal.getId()))
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        hideLoadingDialog();
                        Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                        binding.favoriteButton.setText(R.string.add_to_favorites);
                        binding.favoriteButton.setIconResource(R.drawable.ic_favorite_border);
                        isFavorite = false;
                        binding.favoriteButton.setOnClickListener(v -> addToFavorites());
                    })
                    .addOnFailureListener(e -> {
                        hideLoadingDialog();
                        Log.e("MealsDetailsFragment", "Error removing from favorites", e);
                        Toast.makeText(requireContext(), "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireContext(), "Please login to manage favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private void addToFavorites() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId != null) {
            showLoadingDialog();

            firestore.collection("users").document(userId)
                    .collection("favorites").document(String.valueOf(meal.getId()))
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            hideLoadingDialog();
                            Toast.makeText(requireContext(), "This meal is already in your favorites", Toast.LENGTH_SHORT).show();
                            binding.favoriteButton.setText(R.string.remove_from_favorites);
                            binding.favoriteButton.setIconResource(R.drawable.ic_favorite_filled);
                            isFavorite = true;
                            binding.favoriteButton.setOnClickListener(v -> removeFromFavorites());
                        } else {
                            Map<String, Object> favoriteData = new HashMap<>();
                            favoriteData.put("id", meal.getId());
                            favoriteData.put("name", meal.getName());
                            favoriteData.put("image", meal.getImage());
                            favoriteData.put("mealType", meal.getMealType());

                            firestore.collection("users").document(userId)
                                    .collection("favorites").document(String.valueOf(meal.getId()))
                                    .set(favoriteData)
                                    .addOnSuccessListener(aVoid -> {
                                        hideLoadingDialog();
                                        binding.favoriteButton.setText(R.string.remove_from_favorites);
                                        binding.favoriteButton.setIconResource(R.drawable.ic_favorite_filled);
                                        isFavorite = true;
                                        binding.favoriteButton.setOnClickListener(v -> removeFromFavorites());
                                        Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        hideLoadingDialog();
                                        Log.e("MealsDetailsFragment", "Error adding to favorites", e);
                                        Toast.makeText(requireContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        hideLoadingDialog();
                        Toast.makeText(requireContext(), "Error checking favorites", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireContext(), "Please login to add to favorites", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
