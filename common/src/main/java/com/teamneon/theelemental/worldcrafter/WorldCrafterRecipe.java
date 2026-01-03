package com.teamneon.theelemental.worldcrafter;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorldCrafterRecipe {

    private final List<ItemStack> inputs;
    private final ItemStack output;
    private final Identifier id;

    public WorldCrafterRecipe(Identifier id, List<ItemStack> inputs, ItemStack output) {
        this.id = id;
        this.inputs = inputs;
        this.output = output;
    }

    public List<ItemStack> getInputs() {
        return inputs;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    public Identifier getId() {
        return id;
    }

    /**
     * Checks if the provided items match this recipe.
     * Simple version: exact items, ignoring order.
     */
    /**
     * Checks if the provided items match this recipe.
     * Assumes the first input is always the central item.
     * Remaining inputs are shapeless (pillars).
     */
    public boolean matches(List<ItemStack> items) {
        if (items.isEmpty()) return false;

        // Check central item first
        ItemStack centralInput = inputs.get(0);
        ItemStack centralProvided = items.get(0);

        if (!ItemStack.isSameItem(centralInput, centralProvided) ||
                centralProvided.getCount() < centralInput.getCount()) {
            return false;
        }

        // Gather remaining items for shapeless check
        List<ItemStack> required = new ArrayList<>(inputs.subList(1, inputs.size())); // skip central
        List<ItemStack> provided = new ArrayList<>(items.subList(1, items.size()));

        for (ItemStack reqStack : required) {
            boolean found = false;
            for (ItemStack provStack : provided) {
                if (ItemStack.isSameItem(reqStack, provStack) && provStack.getCount() >= reqStack.getCount()) {
                    provided.remove(provStack);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        // shapeless: extra items on pillars are okay
        return true;
    }


    /**
     * Finds a matching recipe from a list of all recipes.
     */
    public static Optional<ItemStack> findMatchingRecipe(List<ItemStack> items) {
        for (WorldCrafterRecipe recipe : RecipeRegistry.getRecipes()) {
            if (recipe.matches(items)) {
                return Optional.of(recipe.getOutput());
            }
        }
        return Optional.empty();
    }
}
