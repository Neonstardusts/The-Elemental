package com.teamneon.theelemental.worldcrafter;

import java.util.ArrayList;
import java.util.List;

public class RecipeRegistry {

    private static final List<WorldCrafterRecipe> RECIPES = new ArrayList<>();

    public static void registerRecipe(WorldCrafterRecipe recipe) {
        RECIPES.add(recipe);
    }

    public static List<WorldCrafterRecipe> getRecipes() {
        return new ArrayList<>(RECIPES);
    }
}
