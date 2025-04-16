package com.example.menuwise.models;

import java.util.List;

public class Favorite {
    private int id;
    private String image;
    private String name;
    private List<String> mealType;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String>  getMealType() {
        return mealType;
    }

    public void setMealType(List<String> mealType) {
        this.mealType = mealType;
    }
}
