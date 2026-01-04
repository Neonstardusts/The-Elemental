package com.teamneon.theelemental.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SoulForgeSlot extends Slot {
    private final int listIndex;
    private final List<Integer> slotStates;

    public SoulForgeSlot(Container container, int slot, int x, int y, int listIndex, List<Integer> slotStates) {
        super(container, slot, x, y);
        this.listIndex = listIndex;
        this.slotStates = slotStates;
    }

    @Override
    public boolean isActive() {
        // Only active if index < size AND value isn't -1
        return listIndex < slotStates.size() && slotStates.get(listIndex) != -1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isActive(); // Prevents placing runes in locked slots
    }
}