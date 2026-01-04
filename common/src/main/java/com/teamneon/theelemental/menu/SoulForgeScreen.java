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
        graphics.blit(RenderPipelines.GUI, TEXTURE, this.leftPos, this.topPos, 0f, 0f, this.imageWidth, this.imageHeight, 256, 256);

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

        // Display Player Stats from RAM
        var data = ClientElementalData.get();
        graphics.drawString(this.font, "Level: " + data.getLevel(), 8, 20, 0x404040, false);
        graphics.drawString(this.font, "Mana: " + (int)data.getCurrentMana() + "/" + data.getMaxMana(), 8, 30, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}