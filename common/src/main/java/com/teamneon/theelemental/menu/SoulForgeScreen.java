package com.teamneon.theelemental.menu;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.teamneon.theelemental.Theelemental;
import com.teamneon.theelemental.menu.SoulForgeMenu;
import com.teamneon.theelemental.menu.SoulForgeSlot;
import com.teamneon.theelemental.menu.C2SAssignSpellsPacket;
import com.teamneon.theelemental.store.ModComponents;
import com.teamneon.theelemental.store.RuneData;
import com.teamneon.theelemental.client.ClientElementalData;
import net.blay09.mods.balm.Balm;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.teamneon.theelemental.Theelemental.id;

public class SoulForgeScreen extends AbstractContainerScreen<SoulForgeMenu> {
    private static final Identifier TEXTURE = id("textures/gui/soul_forge.png");

    public SoulForgeScreen(SoulForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 166; // Standard height or adjusted for your art
    }

    @Override
    protected void init() {
        super.init();

        // Add the "Assign Spells" Button
        this.addRenderableWidget(Button.builder(Component.literal("Assign"), (btn) -> {
            this.sendAssignmentPacket();
        }).bounds(this.leftPos + 60, this.topPos + 40, 56, 20).build());
    }

    private void sendAssignmentPacket() {
        List<Integer> spellIds = new ArrayList<>();

        // Loop through the 8 Forge slots in the Menu
        for (int i = 0; i < 8; i++) {
            ItemStack stack = this.menu.getSlot(i).getItem();
            RuneData data = stack.get(ModComponents.rune.value());

            if (!stack.isEmpty() && data != null) {
                spellIds.add(data.spellId());
            } else {
                spellIds.add(0); // Empty or non-rune item
            }
        }

        // Send the IDs to the server
        Balm.networking().sendToServer(new C2SAssignSpellsPacket(spellIds));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Parameters: Texture, X, Y, U, V, Width, Height, TextureWidth, TextureHeight
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0f, 0f, this.imageWidth, this.imageHeight, 256, 256);

        // Visual feedback for LOCKED slots (-1)
        for (Slot slot : this.menu.slots) {
            if (slot instanceof SoulForgeSlot forgeSlot && !forgeSlot.isActive()) {
                graphics.fill(this.leftPos + slot.x, this.topPos + slot.y,
                        this.leftPos + slot.x + 16, this.topPos + slot.y + 16, 0xAA000000);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Draw standard titles
        super.renderLabels(graphics, mouseX, mouseY);
        // 1. Render Spell Icons BELOW Forge Slots
        for (int i = 0; i < 8; i++) {
            Slot slot = this.menu.getSlot(i);
            ItemStack stack = slot.getItem();

            if (!stack.isEmpty()) {
                RuneData data = stack.get(ModComponents.rune.value());
                if (data != null && data.spellId() > 0) {
                    // Determine icon path based on spellId
                    Identifier spellIcon = id("textures/gui/spell_icons/" + data.spellId() + ".png");

                    // Render icon 20 pixels below the slot (adjust as needed)
                    // Use 16x16 size to match slot dimensions
                    graphics.blit(RenderPipelines.GUI_TEXTURED, spellIcon, slot.x, slot.y + 20, 0, 0, 16, 16, 16, 16);
                }
            }
        }

        // Display Player Stats from RAM
        var data = ClientElementalData.get();

        // 2. Render Current Active Slots ABOVE the GUI
        List<Integer> activeSpells = data.getActiveSlots(); // Assuming this returns the 8 spell IDs

        int previewStartX = 8;
        int previewY = -25; // 25 pixels above the top of the GUI

        graphics.drawString(this.font, "Current Spells:", previewStartX, previewY - 10, 0xFFFFFFFF);

        for (int i = 0; i < activeSpells.size(); i++) {
            int spellId = activeSpells.get(i);
            if (spellId > 0) {
                Identifier spellIcon = id("textures/gui/spell_icons/" + spellId + ".png");
                graphics.blit(RenderPipelines.GUI_TEXTURED, spellIcon, previewStartX + (i * 20), previewY, 0, 0, 16, 16, 16, 16);
            } else {
                // Optional: Render an empty slot/placeholder for ID 0
                graphics.fill(previewStartX + (i * 20), previewY, previewStartX + (i * 20) + 16, previewY + 16, 0x44FFFFFF);
            }
        }

        graphics.drawString(this.font, "Level: " + data.getLevel(), 8, 20, 0xFF404040, false);
        graphics.drawString(this.font, "Mana: " + (int)data.getCurrentMana() + "/" + data.getMaxMana(), 8, 30, 0xFF404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}