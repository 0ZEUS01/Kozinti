package com.example.kozinti.tables;

import java.util.Date;

public class Recipes {
    String RecipeId, RecipeName, RecipeIngredients, RecipePreparationTime, RecipeCuisine, RecipeCategory, RecipeImageURL, CreatorId;
    Date CreationTime;
    public Recipes() {
    }

    public Recipes(String recipeId, String recipeName, String recipeIngredients, String recipePreparationTime, String recipeCuisine, String recipeCategory, String recipeImageURL, String creatorId,Date creationTime) {
        RecipeId = recipeId;
        RecipeName = recipeName;
        RecipeIngredients = recipeIngredients;
        RecipePreparationTime = recipePreparationTime;
        RecipeCuisine = recipeCuisine;
        RecipeCategory = recipeCategory;
        RecipeImageURL = recipeImageURL;
        CreatorId = creatorId;
        CreationTime = creationTime;
    }

    public String getRecipeId() {
        return RecipeId;
    }

    public void setRecipeId(String recipeId) {
        RecipeId = recipeId;
    }

    public String getRecipeName() {
        return RecipeName;
    }

    public void setRecipeName(String recipeName) {
        RecipeName = recipeName;
    }

    public String getRecipeIngredients() {
        return RecipeIngredients;
    }

    public void setRecipeIngredients(String recipeIngredients) {
        RecipeIngredients = recipeIngredients;
    }

    public String getRecipePreparationTime() {
        return RecipePreparationTime;
    }

    public void setRecipePreparationTime(String recipePreparationTime) {
        RecipePreparationTime = recipePreparationTime;
    }

    public String getRecipeCuisine() {
        return RecipeCuisine;
    }

    public void setRecipeCuisine(String recipeCuisine) {
        RecipeCuisine = recipeCuisine;
    }

    public String getRecipeCategory() {
        return RecipeCategory;
    }

    public void setRecipeCategory(String recipeCategory) {
        RecipeCategory = recipeCategory;
    }

    public String getRecipeImageURL() {
        return RecipeImageURL;
    }

    public void setRecipeImageURL(String recipeImageURL) {
        RecipeImageURL = recipeImageURL;
    }

    public String getCreatorId() {
        return CreatorId;
    }

    public void setCreatorId(String creatorId) {
        CreatorId = creatorId;
    }

    public Date getCreationTime() {
        return CreationTime;
    }

    public void setCreationTime(Date creationTime) {
        CreationTime = creationTime;
    }
}
