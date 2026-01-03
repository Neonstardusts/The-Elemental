package com.teamneon.theelemental.block;

import com.mojang.serialization.MapCodec;
import com.teamneon.theelemental.block.entity.WorldCrafterEntity;
import com.teamneon.theelemental.block.entity.WorldCrafterPillarEntity;
import com.teamneon.theelemental.worldcrafter.WorldCrafterRecipe;
import net.blay09.mods.balm.Balm;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teamneon.theelemental.helpers.KingdomAnchorHelper.NUM_PILLARS;
import static com.teamneon.theelemental.helpers.KingdomAnchorHelper.RADIUS;


public class WorldCrafter extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 13, 14);
    public static final MapCodec<WorldCrafter> CODEC = simpleCodec(WorldCrafter::new);

    public WorldCrafter(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /* BLOCK ENTITY */

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new WorldCrafterEntity(blockPos, blockState);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (!(level.getBlockEntity(pos) instanceof WorldCrafterEntity worldCrafterEntity)) {
            return InteractionResult.PASS;
        }

        // Sneak-right-click triggers the crafting
        if (player.isShiftKeyDown()) {

            ItemStack centralStack = worldCrafterEntity.inventory.getItem(0);
            if (!centralStack.isEmpty()) {
                // Gather items from pillars
                List<ItemStack> pillarItems = new ArrayList<>();
                BlockPos altarPos = pos;
                for (int i = 0; i < NUM_PILLARS; i++) {
                    double angle = 2 * Math.PI / NUM_PILLARS * i;
                    int x = altarPos.getX() + (int) Math.round(Math.cos(angle) * RADIUS);
                    int z = altarPos.getZ() + (int) Math.round(Math.sin(angle) * RADIUS);
                    int y = altarPos.getY();
                    BlockPos pillarPos = new BlockPos(x, y, z);

                    if (level.getBlockEntity(pillarPos) instanceof WorldCrafterPillarEntity pillar) {
                        ItemStack stackInPillar = pillar.inventory.getItem(0);
                        if (!stackInPillar.isEmpty()) {
                            pillarItems.add(stackInPillar.copy());
                        }
                    }
                }

                // Combine central + pillar items
                List<ItemStack> craftingInputs = new ArrayList<>();
                craftingInputs.add(centralStack.copy());
                craftingInputs.addAll(pillarItems);

                // Run your recipe check / crafting logic
                Optional<ItemStack> result = WorldCrafterRecipe.findMatchingRecipe(craftingInputs);
                result.ifPresent(resultStack -> {
                    // Output result to player
                    if (!player.getInventory().add(resultStack)) {
                        player.drop(resultStack, false);
                    }

                    // Clear the central crafter
                    worldCrafterEntity.inventory.setItem(0, ItemStack.EMPTY);
                    worldCrafterEntity.setChanged();

                    // Clear all pillar inventories
                    for (int i = 0; i < NUM_PILLARS; i++) {
                        double angle = 2 * Math.PI / NUM_PILLARS * i;
                        int x = altarPos.getX() + (int) Math.round(Math.cos(angle) * RADIUS);
                        int z = altarPos.getZ() + (int) Math.round(Math.sin(angle) * RADIUS);
                        int y = altarPos.getY();
                        BlockPos pillarPos = new BlockPos(x, y, z);

                        if (level.getBlockEntity(pillarPos) instanceof WorldCrafterPillarEntity pillar) {
                            pillar.inventory.setItem(0, ItemStack.EMPTY);
                            pillar.setChanged();
                        }
                    }

                    level.playSound(player, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1f, 1f);
                });

                return InteractionResult.SUCCESS;
            }

        } else {
            // Normal right-click behavior (place/remove item)
            if (worldCrafterEntity.inventory.getItem(0).isEmpty() && !stack.isEmpty()) {
                worldCrafterEntity.inventory.setItem(0, stack.copy());
                stack.shrink(1);
                level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
            } else if (stack.isEmpty()) {
                ItemStack stackOnPedestal = worldCrafterEntity.inventory.removeItem(0, 1);
                player.setItemInHand(hand, stackOnPedestal);
                worldCrafterEntity.clearContents();
                level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
            }
        }

        return InteractionResult.SUCCESS;
    }
}