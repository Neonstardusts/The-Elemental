package com.teamneon.theelemental.menu;

import com.teamneon.theelemental.client.ClientElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SoulForgeMenu extends AbstractContainerMenu {
    private final Container forgeContainer = new SimpleContainer(8);
    private final Player player;

    public SoulForgeMenu(int windowId, Inventory playerInv, Player player) {
        super(ModMenuTypes.SOULFORGE_MENU.value(), windowId);
        this.player = player;

        // Fetch the slot states (Client or Server side)
        List<Integer> slotStates = player.level().isClientSide()
                ? ClientElementalData.get().getActiveSlots()
                : ElementalDataHandler.get(player).getActiveSlots();

        // 1. Add Soul Forge Slots (8 slots)
        for (int i = 0; i < 8; i++) {
            this.addSlot(new SoulForgeSlot(forgeContainer, i, 8 + (i * 18), 20, i, slotStates));
        }

        // 2. Add Player Inventory (Standard positions)
        // Changed Y from 51 to 84
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 9; col++) {
                        this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
                    }
                }

        // 3. Add Hotbar
        // Increased Y from 142 to 144
                for (int col = 0; col < 9; col++) {
                    this.addSlot(new Slot(playerInv, col, 8 + col * 18, 144));
                }
            }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Temporary GUI: Drop items back to player if they close without "Assigning"
        if (!player.level().isClientSide()) {
            this.clearContainer(player, forgeContainer);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Essential for shift-clicking items into/out of the forge
        return ItemStack.EMPTY; // Implementation omitted for brevity
    }
}