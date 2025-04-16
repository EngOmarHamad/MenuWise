package com.example.menuwise.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class Meal implements Parcelable {
    private int id;
    private String name;
    private List<String> ingredients;
    private List<String> instructions;
    private int prepTimeMinutes;
    private int cookTimeMinutes;
    private int servings;
    private String difficulty;
    private String cuisine;
    private int caloriesPerServing;
    private List<String> tags;
    private String image;
    private double rating;
    private int reviewCount;
    private List<String> mealType;
    private int userId;

    // Required for Firebase
    public Meal() {}

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public List<String> getIngredients() { return ingredients; }
    public List<String> getInstructions() { return instructions; }
    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public int getCookTimeMinutes() { return cookTimeMinutes; }
    public int getServings() { return servings; }
    public String getDifficulty() { return difficulty; }
    public String getCuisine() { return cuisine; }
    public int getCaloriesPerServing() { return caloriesPerServing; }
    public List<String> getTags() { return tags; }
    public String getImage() { return image; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public List<String> getMealType() { return mealType; }
    public int getUserId() { return userId; }

    public boolean isOfMealType(String type) {
        return mealType != null && mealType.contains(type);
    }

    // Parcelable implementation
    protected Meal(Parcel in) {
        id = in.readInt();
        name = in.readString();
        ingredients = in.createStringArrayList();
        instructions = in.createStringArrayList();
        prepTimeMinutes = in.readInt();
        cookTimeMinutes = in.readInt();
        servings = in.readInt();
        difficulty = in.readString();
        cuisine = in.readString();
        caloriesPerServing = in.readInt();
        tags = in.createStringArrayList();
        image = in.readString();
        rating = in.readDouble();
        reviewCount = in.readInt();
        mealType = in.createStringArrayList();
        userId = in.readInt();
    }

    public static final Creator<Meal> CREATOR = new Creator<Meal>() {
        @Override
        public Meal createFromParcel(Parcel in) {
            return new Meal(in);
        }

        @Override
        public Meal[] newArray(int size) {
            return new Meal[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeStringList(ingredients);
        dest.writeStringList(instructions);
        dest.writeInt(prepTimeMinutes);
        dest.writeInt(cookTimeMinutes);
        dest.writeInt(servings);
        dest.writeString(difficulty);
        dest.writeString(cuisine);
        dest.writeInt(caloriesPerServing);
        dest.writeStringList(tags);
        dest.writeString(image);
        dest.writeDouble(rating);
        dest.writeInt(reviewCount);
        dest.writeStringList(mealType);
        dest.writeInt(userId);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
