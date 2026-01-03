package com.teamneon.theelemental.block;

import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.Map;

public class ElementalAltar extends Block {

    public ElementalAltar(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // Right-click with no item
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        player.displayClientMessage(Component.literal("Right-click with a valid item to choose your element!"), true);
        return InteractionResult.CONSUME;
    }

    // Right-click with an item
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        ElementalData data = ElementalDataHandler.get(player);

        // Already has element
        if (data.getElement() > 0) {
            player.displayClientMessage(Component.literal("[debug clearing element]!"), true);
            data.setElement(0);
            return InteractionResult.CONSUME;
        }

        int ElId = ElementRegistry.getIdFromItem(stack.getItem());

        if (ElId != 0) {
            data.setElement(ElId);

            // Show full element + creature message
            player.displayClientMessage(
                    Component.literal(ElementRegistry.getName(ElId) + " " + ElementRegistry.getRace(ElId))
                            .withStyle(style -> style.withColor(ElementRegistry.getColor(ElId)).withBold(true))
                            .append(Component.literal(" - " + ElementRegistry.getMessage(ElId))),
                    true
            );

            if (!player.isCreative()) stack.shrink(1);

            player.addItem(ModItems.KINGDOM_CORE_ITEM.createStack(1));
            // 4. Sync + Save
            ElementalDataHandler.syncToClient(player);
            ElementalDataHandler.save(player);
        } else {
            player.displayClientMessage(Component.literal("Right-click with a valid item to choose your element!"), true);
        }

        return InteractionResult.CONSUME;
    }

}
