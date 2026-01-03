package com.teamneon.theelemental.block;

import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.helpers.UtilityHelper;
import com.teamneon.theelemental.kingdoms.KingdomSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;

import static com.teamneon.theelemental.helpers.KingdomAnchorHelper.getElementFromAnchor;
import static com.teamneon.theelemental.helpers.UtilityHelper.teleportToNearestSafeSpot;


public class KingdomAnchor extends Block {

    // Track shift-right-clicks per player
    private static final Map<UUID, ClickTracker> shiftClickTracker = new HashMap<>();

    private static class ClickTracker {
        long lastClickTime;
        int clickCount;
    }

    public KingdomAnchor(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        int ToElement = getElementFromAnchor((ServerLevel) world, pos);

        if (player.isShiftKeyDown()) {
            UUID playerId = player.getUUID();
            long currentTime = world.getGameTime();

            ClickTracker tracker = shiftClickTracker.getOrDefault(playerId, new ClickTracker());

            // Reset click count if more than 20 ticks (~1 sec) since last
            if (currentTime - tracker.lastClickTime > 20) tracker.clickCount = 0;

            tracker.clickCount++;
            tracker.lastClickTime = currentTime;

            shiftClickTracker.put(playerId, tracker);

            if (tracker.clickCount >= 2) {    // Get global data
                KingdomSavedData globalData = KingdomSavedData.get((ServerLevel) world);
                BlockPos altarPos = globalData.getCorePos(ToElement);

                if (altarPos != null) {
                    // Teleport player to the altar
                    teleportToNearestSafeSpot(player, altarPos.getX(), altarPos.getY(), altarPos.getZ(), ElementRegistry.getColor(ToElement));
                } else {
                    // Fallback if altar not yet registered
                    player.displayClientMessage(Component.literal("No Location found."), true);
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

        return InteractionResult.CONSUME;
    }

    public void effects(ServerLevel level, BlockPos pos) {
        int color = ElementRegistry.getColor(getElementFromAnchor(level, pos));
        UtilityHelper.spawnDust(level, pos.getX()+0.5, pos.getY()+10.5, pos.getZ()+0.5, color, 50,0.5, 0.75, 0.5, 0.02, 3);
    }

}