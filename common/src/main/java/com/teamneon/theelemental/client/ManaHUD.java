package com.teamneon.theelemental.client;

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

        int barWidth = 10;
        int barHeight = 34;
        int x = 10 + 40;
        int y = height - 46;
        int barx = x+3;
        int bary = y+4+barHeight;


        Identifier manaFront = id("textures/gui/mana_frame.png");
        Identifier manaBackround = id("textures/gui/mana_backround.png");

        //FIX MANA BAR LATER
        // Draw fill
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, manaBackround, x, y, 0, 0, 40, 40, 40, 40);
        guiGraphics.fill(barx, bary, barx + barWidth, bary - (int)(barHeight * percent), 0xD000e5ff);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, manaFront, x, y, 0, 0, 40, 40, 40, 40);

    }
}
