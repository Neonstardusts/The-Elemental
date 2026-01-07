package com.teamneon.theelemental.client.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

import static com.teamneon.theelemental.Theelemental.id;

public class InfoTooltipComponent implements ClientTooltipComponent {

    private static final int ICON_SIZE = 32;
    private static final int PADDING = 2;

    private final InfoTooltipComponentData data;

    public InfoTooltipComponent(InfoTooltipComponentData data) {
        this.data = data;
    }

    @Override
    public int getHeight(Font font) {
        int height = font.lineHeight + PADDING; // default top line
        if (Minecraft.getInstance().hasShiftDown()) {
            height += data.texturePaths().size() * (ICON_SIZE + PADDING); // icons + text
        }
        return height;
    }

    @Override
    public int getWidth(Font font) {
        int width = font.width(Minecraft.getInstance().hasShiftDown()
                ? Component.literal("ℹ Information")
                : Component.literal("ℹ [Shift for more information]"));

        if (Minecraft.getInstance().hasShiftDown()) {
            for (int i = 0; i < data.lines().size(); i++) {
                int lineWidth = font.width(data.lines().get(i)) + ICON_SIZE + PADDING;
                width = Math.max(width, lineWidth);
            }
        }

        return width;
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
        int offsetY = y;

        boolean shift = Minecraft.getInstance().hasShiftDown();
        Component topLine = shift
                ? Component.literal("ℹ Information").withStyle(ChatFormatting.GRAY)
                : Component.literal("ℹ [Shift for more information]").withStyle(ChatFormatting.DARK_GRAY);

        // Draw top line
        graphics.drawString(font, topLine, x, offsetY, 0xFFAAAAAA, false);
        offsetY += font.lineHeight + PADDING;

        // Draw icons + text only if Shift is held
        if (!shift) return;

        List<Component> lines = data.lines();
        List<String> textures = data.texturePaths();

        for (int i = 0; i < textures.size(); i++) {
            String texPath = textures.get(i);
            Identifier texture = id("textures/gui/help_icons/" + texPath);

            // Draw icon
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, offsetY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

            // Draw two lines of text per icon
            int line1Index = i * 2;
            int line2Index = line1Index + 1;

            Component line1 = lines.get(line1Index);
            Component line2 = line2Index < lines.size() ? lines.get(line2Index) : null;

            int numLines = line2 != null ? 2 : 1;
            int textHeight = font.lineHeight * numLines;
            int textStartY = offsetY + (ICON_SIZE - textHeight) / 2;

            graphics.drawString(font, line1, x + ICON_SIZE + PADDING, textStartY, 0xFFFFFFFF, false);
            if (line2 != null) {
                graphics.drawString(font, line2, x + ICON_SIZE + PADDING, textStartY + font.lineHeight, 0xFFFFFFFF, false);
            }

            offsetY += ICON_SIZE + PADDING;
        }
    }
}
