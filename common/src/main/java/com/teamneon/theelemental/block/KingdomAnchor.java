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
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
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

    public static final IntegerProperty ELEMENT =
            IntegerProperty.create("element", 0, 9);

    public KingdomAnchor(BlockBehaviour.Properties properties) {

        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(ELEMENT, 0)
        );
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ELEMENT);
    }


    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        int ToElement = getElementFromAnchor((ServerLevel) world, pos);

        KingdomSavedData globalData = KingdomSavedData.get((ServerLevel) world);
        BlockPos altarPos = globalData.getCorePos(ToElement);

        if (altarPos != null) {
            teleportToNearestSafeSpot(
                    player,
                    altarPos.getX(),
                    altarPos.getY(),
                    altarPos.getZ(),
                    ElementRegistry.getColor(ToElement)
            );
        } else {
            player.displayClientMessage(Component.literal("No Kingdom Found."), true);
        }


        return InteractionResult.CONSUME;
    }

    public void effects(ServerLevel level, BlockPos pos) {
        int color = ElementRegistry.getColor(getElementFromAnchor(level, pos));
        UtilityHelper.spawnDust(level, pos.getX()+0.5, pos.getY()+10.5, pos.getZ()+0.5, color, 50,0.5, 0.75, 0.5, 0.02, 3);
    }


}