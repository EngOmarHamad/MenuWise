package com.example.menuwise.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.menuwise.R;
import com.example.menuwise.fragments.MealsDetailsFragment;
import com.example.menuwise.models.Favorite;
import com.example.menuwise.models.Meal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private final Context context;
    private  final List<Meal> meals;
    private List<String> favoriteIds = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

    public RecipeAdapter(Context context, List<Meal> meals) {
        this.context = context;
        this.meals = meals != null ? meals : new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Meal> newMeals, List<String> favoriteIds) {
        this.meals.clear();
        this.meals.addAll(newMeals);
        this.favoriteIds = favoriteIds != null ? favoriteIds : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.bind(meal);
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameTextView, cuisineTextView ,ratingTextView,timeTextView;
        private final ImageView favoriteIcon;
        private AlertDialog loadingDialog;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            cuisineTextView = itemView.findViewById(R.id.cuisineTextView);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
        private void showLoadingDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.loading, null);
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
        @SuppressLint("NotifyDataSetChanged")
        public void bind(Meal meal) {
            Glide.with(context)
                    .load(meal.getImage())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(imageView);

            nameTextView.setText(meal.getName());
            cuisineTextView.setText(meal.getCuisine());
            ratingTextView.setText(String.valueOf(meal.getRating()));
            timeTextView.setText(String.format("%smin", meal.getPrepTimeMinutes()));

            // تحقق إن كانت الوجبة من المفضلة
            boolean isFavorite = favoriteIds.contains(String.valueOf(meal.getId()));
            Log.e(" boolean isFavorite",String.valueOf(isFavorite));
            favoriteIcon.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

            itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                MealsDetailsFragment fragment = new MealsDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable("meal", meal);
                bundle.putBoolean("isFavorite", isFavorite);
                fragment.setArguments(bundle);
                if (context instanceof AppCompatActivity) {
                    AppCompatActivity activity = (AppCompatActivity) context;
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.recipe_fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
            favoriteIcon.setOnClickListener(v -> {
                if (userId == null) {
                    Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
                    return;
                }

                showLoadingDialog();

                DocumentReference favRef = db.collection("users").document(userId)
                        .collection("favorites").document(String.valueOf(meal.getId()));

                boolean isCurrentlyFavorite = favoriteIds.contains(String.valueOf(meal.getId()));

                if (!isCurrentlyFavorite) {
                    Favorite favorite = new Favorite();
                    favorite.setId(meal.getId());
                    favorite.setName(meal.getName());
                    favorite.setImage(meal.getImage());
                    favorite.setMealType(meal.getMealType());

                    favRef.set(favorite)
                            .addOnSuccessListener(unused -> {
                                favoriteIds.add(String.valueOf(meal.getId()));
                                favoriteIcon.setImageResource(R.drawable.ic_favorite_filled);
                                Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                                hideLoadingDialog();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Error adding to favorites", Toast.LENGTH_SHORT).show();
                                hideLoadingDialog();
                            });
                } else {
                    favRef.delete()
                            .addOnSuccessListener(unused -> {
                                favoriteIds.remove(String.valueOf(meal.getId()));
                                favoriteIcon.setImageResource(R.drawable.ic_favorite_border);
                                Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                                hideLoadingDialog();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Error removing from favorites", Toast.LENGTH_SHORT).show();
                                hideLoadingDialog();
                            });
                }
            });

        }
    }
}
