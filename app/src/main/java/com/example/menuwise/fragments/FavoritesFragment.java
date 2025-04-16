package com.example.menuwise.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.menuwise.R;
import com.example.menuwise.adapters.FavoritesAdapter;
import com.example.menuwise.models.Favorite;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;


public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private FavoritesAdapter adapter;
    private final ArrayList<Favorite> favoriteRecipes = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);


        recyclerView = view.findViewById(R.id.recyclerViewFavorites);
        progressBar = view.findViewById(R.id.progressBarFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoritesAdapter(requireContext(), favoriteRecipes);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadFavorites();

        return view;
    }

    private void loadFavorites() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textViewEmpty.setVisibility(View.GONE);

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        assert userId != null;
        Log.e("userId",userId);

        CollectionReference favoritesRef = db.collection("users").document(userId).collection("favorites");

        favoritesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteRecipes.clear(); // يجب أن يكون قبل الإضافة
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Favorite recipe = document.toObject(Favorite.class);
                            favoriteRecipes.add(recipe);
                            Log.e("Favorites Size", String.valueOf(favoriteRecipes.size()));
                        } catch (Exception e) {
                            Log.e("Exception", "Error loading document: " + e.getLocalizedMessage());
                        }
                    }
                    adapter.updateData(favoriteRecipes);
                    progressBar.setVisibility(View.GONE);
                    if (favoriteRecipes.isEmpty()) {
                        textViewEmpty.setText("لا توجد عناصر مفضلة حتى الآن");
                        textViewEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        textViewEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "فشل تحميل المفضلة", Toast.LENGTH_SHORT).show();
                });
    }
}
