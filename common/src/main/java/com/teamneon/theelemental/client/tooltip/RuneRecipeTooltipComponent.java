package com.teamneon.theelemental.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class RuneRecipeTooltipComponent implements ClientTooltipComponent {
    private final RuneRecipeTooltipComponentData data;

    public RuneRecipeTooltipComponent(RuneRecipeTooltipComponentData data) {
        this.data = data;
    }

    @Override
    public int getHeight(Font font) { return 0; }

    @Override
    public int getWidth(Font font) { return 18 * data.recipeItems().size(); }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
        int i = 0;
        for (ItemStack stack : data.recipeItems()) {
            int slotX = x + (i * 18);
            int slotY = y + 62;
            graphics.renderItem(stack, slotX, slotY);
            graphics.renderItemDecorations(font, stack, slotX, slotY);
            i++;
        }
    }
}