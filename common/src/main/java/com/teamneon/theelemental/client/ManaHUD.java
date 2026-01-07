package com.teamneon.theelemental.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.teamneon.theelemental.helpers.ElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import static com.teamneon.theelemental.Theelemental.id;

public class ManaHUD {

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        float currentMana = ClientElementalData.get().getCurrentMana();
        int maxMana = ClientElementalData.get().getMaxMana();
        float percent = currentMana / maxMana;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();



        ElementRegistry.getColor(ClientElementalData.get().getElement());

        int x = 10 + 40;
        int y = height - 42;


        Identifier manaFront = id("textures/gui/mana_frame.png");
        Identifier manaBar = id("textures/gui/mana_full.png");


        int barWidth = 9;  // texture width
        int barHeight = 32; // texture height

        //Make the manaBar
        int filledHeight = (int) (barHeight * percent); // how much of the bar to show
        int yOffset = barHeight - filledHeight; // shift down so it fills from bottom

        int elementColor = ElementRegistry.getColor(ClientElementalData.get().getElement()); // 0xRRGGBB
        float alphaF = 1f; // 0.0 - 1.0
        int alpha = (int)(alphaF * 255.0f) & 0xFF;
        int colorWithAlpha = (alpha << 24) | elementColor;


        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, manaFront, x, y-4, 0, 0, 40, 40, 40, 40);


        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                manaBar,
                x+4, y + yOffset,   // position on screen
                0, yOffset,       // starting point in the texture
                barWidth, filledHeight, // portion of texture to draw
                barWidth, barHeight, // full texture size
                colorWithAlpha
        );




    }
}


