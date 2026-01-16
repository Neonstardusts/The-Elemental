package com.teamneon.theelemental.item;

import com.teamneon.theelemental.client.tooltip.InfoTooltipComponentData;
import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.UtilityHelper;
import com.teamneon.theelemental.store.ManaData;
import com.teamneon.theelemental.store.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class ManaCrystalItem extends Item {
    public ManaCrystalItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            ElementalData playerLevelData = ElementalDataHandler.get(player);
            ManaData crystalData = stack.getOrDefault(ModComponents.mana_storage.value(), ManaData.EMPTY);

            float currentCrystalMana = crystalData.mana();
            float currentPlayerMana = playerLevelData.getCurrentMana();
            float playerMax = playerLevelData.getMaxMana();

            if (player.isShiftKeyDown()) {
                // --- SHIFT + RIGHT CLICK: Player -> Crystal ---
                if (currentPlayerMana >= 10f && currentCrystalMana < 500f) {
                    float amountToTransfer = Math.min(10f, 500f - currentCrystalMana);
                    stack.set(ModComponents.mana_storage.value(), new ManaData(currentCrystalMana + amountToTransfer));
                    playerLevelData.setCurrentMana(currentPlayerMana - amountToTransfer);

                    player.displayClientMessage(Component.literal("Crystal stored " + ((int)(currentCrystalMana + amountToTransfer)) + "/500 Mana"), true);
                }
            } else {
                // --- RIGHT CLICK: Crystal -> Player ---
                if (currentCrystalMana > 0 && currentPlayerMana < playerMax) {
                    float amountToTransfer = Math.min(currentCrystalMana, playerMax - currentPlayerMana);
                    stack.set(ModComponents.mana_storage.value(), new ManaData(currentCrystalMana - amountToTransfer));
                    playerLevelData.setCurrentMana(currentPlayerMana + amountToTransfer);

                    player.displayClientMessage(Component.literal("Restored " + (int)amountToTransfer + " Mana!"), true);
                }
            }

            ElementalDataHandler.save(player);
            ElementalDataHandler.syncToClient(player);
        }

        // Return InteractionResult.SUCCESS (or CONSUME/PASS) instead of ResultHolder
        return InteractionResult.SUCCESS;
    }

    // 1. Force the bar to show even if the item isn't "damaged"
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    // 2. Calculate the "fullness" of the bar (0 to 13)
    @Override
    public int getBarWidth(ItemStack stack) {
        // Use the component to get mana
        ManaData data = stack.get(ModComponents.mana_storage.value());
        float currentMana = (data != null) ? data.mana() : 0f;
        float maxMana = 500f; // You can also put this in the record!

        return Math.round(13.0f * Math.min(currentMana / maxMana, 1.0f));
    }

    // 3. Choose the color of the bar (e.g., Light Blue for Mana)
    @Override
    public int getBarColor(ItemStack stack) {
        int rainbow = UtilityHelper.getPaleRainbowColor(10000);

        return rainbow;
    }

    /*
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        boolean shift = Minecraft.getInstance().hasShiftDown();

        // Use a list to hold your custom lines
        List<Component> lines = new java.util.ArrayList<>();

        if (shift) {
            // Main Mana Line (The 50 symbols logic can be handled in your TooltipComponent renderer)
            lines.add(Component.literal("||||||||||||||||||||||||||||||||||||||||||||||||||").withStyle(ChatFormatting.GRAY));

            // Instruction Lines
            lines.add(Component.literal("[Shift-Right-Click] ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Store 10 mana into the crystal").withStyle(ChatFormatting.WHITE)));

            lines.add(Component.literal("[Right-Click] ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Empty the mana into yourself").withStyle(ChatFormatting.WHITE)));

            lines.add(Component.literal("[Right-Click] ").withStyle(ChatFormatting.AQUA) // bluish color
                    .append(Component.literal("On kingdom core with magic crystals to create 10 mana").withStyle(ChatFormatting.WHITE)));
        } else {
            lines.add(Component.literal("ℹ [Shift for more information]").withStyle(ChatFormatting.DARK_GRAY));
        }

        // Return your custom TooltipComponent data containing the lines
        return Optional.of(new InfoTooltipComponentData(lines));
    }
    */
    
}