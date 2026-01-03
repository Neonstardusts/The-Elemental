package com.teamneon.theelemental.worldcrafter;

import com.teamneon.theelemental.Theelemental;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ModRecipes {

    public static void register() {
        // Example: crafting a diamond from 1 gold ingot + 1 iron ingot + central item = stick
        WorldCrafterRecipe recipe = new WorldCrafterRecipe(
                Theelemental.id("diamond_from_stuff"), // proper Identifier usage
                List.of(
                        new ItemStack(Items.DIAMOND),        // central
                        new ItemStack(Items.GOLD_INGOT),   // pillar 1
                        new ItemStack(Items.IRON_INGOT),   // pillar 2
                        new ItemStack(Items.DIAMOND),
                        new ItemStack(Items.DIAMOND)
                        // remaining pillars can be empty
                ),
                new ItemStack(Items.DIAMOND_SWORD)   // output
        );

        RecipeRegistry.registerRecipe(recipe);
    }
}
