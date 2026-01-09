package com.teamneon.theelemental.menu;

import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.ElementRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

public class ElementChooserMenu extends AbstractContainerMenu {
    private final Player player;

    public ElementChooserMenu(int windowId, Inventory playerInv, Player player) {
        super(ModMenuTypes.ELEMENT_CHOOSER_MENU.value(), windowId);
        this.player = player;
    }


    public List<Integer> getAvailableElements() {
        return ElementRegistry.getAllIds(); // list of all element IDs
    }

    public int getCurrentElementId() {
        return ElementalDataHandler.get(player).getElement();
    }

    public void setElement(int id) {
        ElementalDataHandler.get(player).setElement(id);
        ElementalDataHandler.syncToClient(player);
        ElementalDataHandler.save(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // player-bound menu
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        // No slots, so nothing to move
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

}
