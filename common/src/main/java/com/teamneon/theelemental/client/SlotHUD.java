package com.teamneon.theelemental.client;

import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.List;

import static com.teamneon.theelemental.Theelemental.id;

public class SlotHUD {

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int currentSlot = ClientElementalData.getCurrentSlot();

        List<Integer> activeSlots = ClientElementalData.get().getActiveSlots();
        int element = ClientElementalData.get().getElement();
        int elementColor = ElementRegistry.getColor(ClientElementalData.get().getElement()); // 0xRRGGBB
        float alphaF = 1f; // 0.0 - 1.0
        int alpha = (int)(alphaF * 255.0f) & 0xFF;
        int colorWithAlpha = (alpha << 24) | elementColor;

        // Validate
        if (currentSlot < 0 || currentSlot >= activeSlots.size()) return;

        int slotValue = activeSlots.get(currentSlot); // Value of selected slot
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // Bottom-left corner
        int x = 10;                  // 10 px from left
        int y = height - 42;          // 20 px from bottom

        String valueText = slotValue >= 0 ? Integer.toString(slotValue) : "error";
        Identifier slotTexture = id("textures/gui/spell_icons/" + valueText + ".png");
        Identifier elementTexture = id("textures/gui/element_icons/element_" + element + ".png");
        Identifier overlayTexture = id("textures/gui/spell_frame.png");

        ClientSpellInfo spellInfo = ClientSpellRegistry.getSpell(slotValue);
        String SpellName;

        if (spellInfo == null || spellInfo.name == null || spellInfo.name.isEmpty()) {
            SpellName = "Blank";
        } else {
            SpellName = spellInfo.name;
        }




        //Draw the spell icons
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, slotTexture, x, y, 0, 0, 32, 32, 32, 32);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, elementTexture, x-4, y-20, 0, 0, 16, 16, 16, 16, colorWithAlpha);

        // Current Slot
        String indexText = Integer.toString(currentSlot + 1);
        guiGraphics.drawString(mc.font, indexText, x+14, y-16, 0xFFFFFFFF, true);

        // Spell Name
        guiGraphics.drawString(mc.font, SpellName, x + 23, y-16, colorWithAlpha, true);


        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, overlayTexture, x-4, y-4, 0, 0, 40, 40, 40, 40);

        // 2. DRAW COOLDOWN OVERLAY
        if (slotValue > 0 && ClientElementalData.isSpellOnCooldown(slotValue)) {
            // Darken the icon if on cooldown
            guiGraphics.fill(x, y, x + 32, y + 32, 0x99000000);

            // Calculate remaining seconds
            long remainingTicks = ClientElementalData.getRemainingCooldownTicks(slotValue);
            float remainingSeconds = remainingTicks / 20f;

            // Format to 1 decimal place (e.g., "1.5s")
            String cdText = String.format("%.1fs", remainingSeconds);

            // Draw text in the middle of the slot
            int textWidth = mc.font.width(cdText);
            guiGraphics.drawString(mc.font, cdText, x + 16 - (textWidth / 2), y + 12, 0xFF555555, true);
        }



    }
}
