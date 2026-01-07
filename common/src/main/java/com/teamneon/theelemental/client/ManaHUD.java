package com.teamneon.theelemental.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.teamneon.theelemental.helpers.ElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.List;

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

        int x = 10 + 40;
        int y = height - 42;

        Identifier manaFront = id("textures/gui/mana_frame.png");
        Identifier manaBar = id("textures/gui/mana_full.png");

        int barWidth = 9;
        int barHeight = 32;

        // Current filled mana
        int filledHeight = (int) (barHeight * percent);
        int yOffset = barHeight - filledHeight;

        int elementColor = ElementRegistry.getColor(ClientElementalData.get().getElement());
        int alpha = 0xFF;
        int colorWithAlpha = (alpha << 24) | elementColor;

        // Draw the mana frame
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, manaFront, x, y - 4, 0, 0, 40, 40, 40, 40);

        // Draw the mana bar
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                manaBar,
                x + 4, y + yOffset,
                0, yOffset,
                barWidth, filledHeight,
                barWidth, barHeight,
                colorWithAlpha
        );

        int currentSlot = ClientElementalData.getCurrentSlot();
        List<Integer> activeSlots = ClientElementalData.get().getActiveSlots();
        int slotValue = activeSlots.get(currentSlot);

        ClientSpellInfo spellInfo = ClientSpellRegistry.getSpell(slotValue);
        int ManaCost;

        if (spellInfo == null ) {
            ManaCost = 0;
        } else {
            ManaCost = spellInfo.manaCost;
        }

        float spellCost = (float) ManaCost;
        if (spellCost > 0) {

            if (currentMana >= spellCost) {
                // Enough mana: draw a solid white line at the bottom of the usage
                int usageHeight = (int) (barHeight * (spellCost / maxMana));

                // Y position: where the bar will be after spending mana
                int lineY = y + (barHeight - filledHeight + usageHeight);

                // Draw a 1-pixel tall white line across the bar
                guiGraphics.fill(
                        x + 4,           // left
                        lineY,           // top
                        x + 4 + barWidth,// right
                        lineY + 1,       // bottom (1 pixel tall)
                        0xFFFFFFFF        // solid white
                );
            } else {
                // Not enough mana: draw a red box over the entire bar
                guiGraphics.fill(
                        x + 4,
                        y,
                        x + 4 + barWidth,
                        y + barHeight,
                        0x80FF0000 // semi-transparent red
                );
            }
        }

    }

}


