package com.teamneon.theelemental.client.tooltip;

import com.teamneon.theelemental.Theelemental;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class SpellIconTooltipComponent implements ClientTooltipComponent {
    private final SpellIconTooltipComponentData data;
    private static final Identifier TEXTURE_FOLDER = Theelemental.id("textures/gui/spell_icons/");

    public SpellIconTooltipComponent(SpellIconTooltipComponentData data) {
        this.data = data;
    }

    @Override
    public int getHeight(Font font) {
        return 0;
    }

    @Override
    public int getWidth(Font font) {
        return 32;
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
        // Construct the path: textures/gui/spell_icons/1.png
        Identifier texture = TEXTURE_FOLDER.withPath(path -> path + data.spellId() + ".png");

        // We use 'x' and 'y' provided by the method.
        // width and height parameters tell us how much space the tooltip allocated for us.
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y + 10, 0, 0, 32, 32, 32, 32);
    }
}