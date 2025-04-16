package com.example.menuwise.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class APIResponse {
    @SerializedName("recipes")
    private List<Meal> meals;

    public List<Meal> getRecipes() {
        return meals;
    }
}
