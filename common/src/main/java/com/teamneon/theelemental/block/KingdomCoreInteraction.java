package com.teamneon.theelemental.block;

import com.teamneon.theelemental.block.entity.KingdomCoreBlockEntity;
import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.kingdoms.KingdomSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.teamneon.theelemental.helpers.UtilityHelper.teleportToNearestSafeSpot;

public class KingdomCoreInteraction {

    // Track shift-right-clicks per player
    private static final Map<UUID, ClickTracker> shiftClickTracker = new HashMap<>();

    private static class ClickTracker {
        long lastClickTime;
        int clickCount;
    }

    /**
     * Handles all right-clicks (with item or empty hand)
     */
    public static InteractionResult handleWithItem(Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (!(world.getBlockEntity(pos) instanceof KingdomCoreBlockEntity core)) {
            return InteractionResult.PASS;
        }

        if (world.isClientSide()) return InteractionResult.SUCCESS;

        // --- Check if the item is amethyst ---
        if (stack.getItem() == Items.AMETHYST_SHARD) {
            // Get player's elemental data
            ElementalData data = ElementalDataHandler.get(player);

            // Element check
            if (data.getElement() != core.getElement()) {
                player.displayClientMessage(Component.literal("Your element does not match this Kingdom Core!"), true);
                return InteractionResult.FAIL;
            }

            // Increase mana
            data.setCurrentMana(data.getCurrentMana() + 10f);

            // Consume 1 amethyst
            stack.shrink(1);

            player.displayClientMessage(Component.literal("Your mana increased by 10!"), true);
            return InteractionResult.CONSUME;
        } else {
            // --- Empty-hand or any other item logic ---

            // Only trigger teleport if player is sneaking
            if (player.isShiftKeyDown()) {
                UUID playerId = player.getUUID();
                long currentTime = world.getGameTime();

                ClickTracker tracker = shiftClickTracker.getOrDefault(playerId, new ClickTracker());

                // Reset click count if more than 20 ticks (~1 sec) since last
                if (currentTime - tracker.lastClickTime > 20) tracker.clickCount = 0;

                tracker.clickCount++;
                tracker.lastClickTime = currentTime;

                shiftClickTracker.put(playerId, tracker);

                if (tracker.clickCount >= 2) {
                    // Get global data
                    KingdomSavedData globalData = KingdomSavedData.get((ServerLevel) world);
                    BlockPos altarPos = globalData.getCorePos(-1); // -1 = elemental altar

                    if (altarPos != null) {
                        // Teleport player to the altar
                        teleportToNearestSafeSpot(player, altarPos.getX(), altarPos.getY(), altarPos.getZ(), ElementRegistry.getColor(core.getElement()));
                    } else {
                        // Fallback if altar not yet registered
                        player.displayClientMessage(Component.literal("No Elemental Altar found."), true);
                    }

                    // Reset click tracker
                    tracker.clickCount = 0;
                    shiftClickTracker.put(playerId, tracker);

                    return InteractionResult.CONSUME;
                }

                // Optional feedback for first click
                player.displayClientMessage(Component.literal("Shift-right-click again to teleport..."), true);
                return InteractionResult.CONSUME;
            }

            // Normal empty-hand interaction (owner check)
            if (player.getUUID().equals(core.getOwner())) {
                player.displayClientMessage(Component.literal("You opened your Kingdom Core!"), true);
            } else {
                player.displayClientMessage(Component.literal("You are not the owner!"), true);
            }

            return InteractionResult.CONSUME;
        }
    }
}
