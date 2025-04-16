package com.example.menuwise.network;

import com.example.menuwise.models.APIResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MealApiService {
    @GET("recipes/meal-type/{mealType}")
    Call<APIResponse> getRecipesByMealType(@Path("mealType") String mealType);
}
