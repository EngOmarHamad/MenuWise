package com.example.menuwise.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.menuwise.R;
import com.example.menuwise.models.Favorite;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private final Context context;
    private ArrayList<Favorite> favoriteRecipes;

    public FavoritesAdapter(Context context, ArrayList<Favorite> favoriteRecipes) {
        this.context = context;
        this.favoriteRecipes = favoriteRecipes;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.favorite, parent, false);
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        Favorite recipe = favoriteRecipes.get(position);

        holder.recipeName.setText(recipe.getName());
        List<String> mealTypes =recipe.getMealType();
        holder.mealTypeChipGroup.removeAllViews();
        for (String type : mealTypes) {
            Chip chip = new Chip(context);
            chip.setText(type);
            chip.setChipBackgroundColorResource(R.color.colorPrimary);
            chip.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            chip.setChipStrokeWidth(2f);
            chip.setChipStrokeColor(ContextCompat.getColorStateList(context, R.color.colorPrimary));
            chip.setClickable(false);
            chip.setCheckable(false);
            holder.mealTypeChipGroup.addView(chip);
        }

        Glide.with(context)
                .load(recipe.getImage())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.recipeImage);

        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Favorite favorite = favoriteRecipes.get(adapterPosition);

                // عرض مربع تأكيد الحذف
                new AlertDialog.Builder(context)
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to remove this recipe from your favorites?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            String userId = holder.auth.getCurrentUser() != null ? holder.auth.getCurrentUser().getUid() : null;

                            // حذف من Firestore
                            assert userId != null;
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userId)
                                    .collection("favorites")
                                    .document(String.valueOf(favorite.getId()))
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();

                                        // حذف من القائمة المحلية
                                        favoriteRecipes.remove(adapterPosition);
                                        notifyItemRemoved(adapterPosition);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to remove favorite", Toast.LENGTH_SHORT).show();
                                        Log.e("FavoritesAdapter", "Error deleting favorite", e);
                                    });
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteRecipes.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<Favorite> newFavorites) {
        ArrayList<Favorite> copy = new ArrayList<>(newFavorites);
        this.favoriteRecipes.clear();
        this.favoriteRecipes.addAll(copy);
        notifyDataSetChanged();
    }

    public static class FavoritesViewHolder extends RecyclerView.ViewHolder {

        private final TextView recipeName;
        private final ChipGroup mealTypeChipGroup;
        private final ImageView recipeImage;
        private final ImageButton deleteButton;
        private final FirebaseAuth auth;

        public FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.nameTextView);
            mealTypeChipGroup = itemView.findViewById(R.id.mealTypeChipGroup);
            recipeImage = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            auth = FirebaseAuth.getInstance();
        }
    }
}
