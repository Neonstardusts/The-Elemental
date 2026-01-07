package com.teamneon.theelemental.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class RuneRecipeTooltipComponent implements ClientTooltipComponent {
    private final RuneRecipeTooltipComponentData data;

    public RuneRecipeTooltipComponent(RuneRecipeTooltipComponentData data) {
        this.data = data;
    }

    @Override
    public int getHeight(Font font) {
        int textHeight = data.ritualText().size() * font.lineHeight;
        int itemHeight = data.recipeItems().isEmpty() ? 0 : 18;
        return textHeight + itemHeight + 4; // small padding
    }

    @Override
    public int getWidth(Font font) {
        int textWidth = data.ritualText().stream()
                .mapToInt(font::width)
                .max().orElse(0);
        int itemWidth = data.recipeItems().size() * 18;
        return Math.max(textWidth, itemWidth);
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
        int offsetY = y;

        // Draw the ritual text first
        for (Component line : data.ritualText()) {
            graphics.drawString(font, line, x, offsetY, 0xFFFFFFFF, false);
            offsetY += font.lineHeight;
        }

        // Draw the items below the text
        int itemX = x;
        int itemY = offsetY + 2; // small padding
        for (int i = 0; i < data.recipeItems().size(); i++) {
            ItemStack stack = data.recipeItems().get(i);
            int slotX = itemX + (i * 18);
            graphics.renderItem(stack, slotX, itemY);
            graphics.renderItemDecorations(font, stack, slotX, itemY);
        }
    }
}
