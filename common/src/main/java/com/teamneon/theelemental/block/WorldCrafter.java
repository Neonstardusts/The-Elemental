package com.teamneon.theelemental.block;

import com.mojang.serialization.MapCodec;
import com.teamneon.theelemental.Theelemental;
import com.teamneon.theelemental.block.entity.WorldCrafterEntity;
import com.teamneon.theelemental.block.entity.WorldCrafterPillarEntity;
import com.teamneon.theelemental.item.ElementRune;
import com.teamneon.theelemental.item.ModItems;
import com.teamneon.theelemental.store.ModComponents;
import com.teamneon.theelemental.store.RuneData;
import com.teamneon.theelemental.worldcrafter.WorldCrafterRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

import java.util.*;

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

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WorldCrafterEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {

        if (!(level.getBlockEntity(pos) instanceof WorldCrafterEntity worldCrafter)) {
            return InteractionResult.PASS;
        }

        /* ============================================================
           SHIFT-RIGHT-CLICK : CRAFTING
           ============================================================ */
        if (player.isShiftKeyDown()) {

            ItemStack centralStack = worldCrafter.inventory.getItem(0);
            Theelemental.logger.warn("[WorldCrafter] Shift-right-click at {}", pos);

            /* ---------------- RUNE CRAFTING ---------------- */
            if (centralStack.getItem() instanceof ElementRune) {

                if (!centralStack.has(ModComponents.rune.value())) {
                    Theelemental.logger.warn("[WorldCrafter] Rune has NO RuneData");
                    return InteractionResult.SUCCESS;
                }

                RuneData runeData = centralStack.get(ModComponents.rune.value());
                if (runeData.recipeItems().isEmpty()) {
                    Theelemental.logger.warn("[WorldCrafter] Rune recipe EMPTY");
                    return InteractionResult.SUCCESS;
                }

                // Collect pillar items
                List<ItemStack> pillarItems = new ArrayList<>();

                for (int i = 0; i < NUM_PILLARS; i++) {
                    double angle = 2 * Math.PI / NUM_PILLARS * i;
                    BlockPos pillarPos = new BlockPos(
                            pos.getX() + (int) Math.round(Math.cos(angle) * RADIUS),
                            pos.getY(),
                            pos.getZ() + (int) Math.round(Math.sin(angle) * RADIUS)
                    );

                    if (level.getBlockEntity(pillarPos) instanceof WorldCrafterPillarEntity pillar) {
                        ItemStack pillarStack = pillar.inventory.getItem(0);
                        if (!pillarStack.isEmpty()) {
                            pillarItems.add(pillarStack.copy());
                        }
                    }
                }

                // Build multiset
                Map<String, Integer> available = new HashMap<>();
                for (ItemStack pillarStack : pillarItems) {
                    String id = pillarStack.getItem()
                            .builtInRegistryHolder()
                            .key()
                            .identifier()
                            .toString();

                    available.merge(id, pillarStack.getCount(), Integer::sum);
                }

                // Match recipe
                for (var entry : runeData.recipeItems().entrySet()) {
                    String id = entry.getKey();
                    int needed = entry.getValue();

                    int have = available.getOrDefault(id, 0);
                    if (have < needed) {
                        return InteractionResult.SUCCESS;
                    }
                    available.put(id, have - needed);
                }

                // Reject extras
                if (available.values().stream().anyMatch(v -> v > 0)) {
                    return InteractionResult.SUCCESS;
                }

                // Craft result
                ItemStack result = new ItemStack(ModItems.SPELL_RUNE.asItem());
                result.set(ModComponents.rune.value(), runeData);

                if (!player.getInventory().add(result)) {
                    player.drop(result, false);
                }

                // Clear central + pillars
                worldCrafter.inventory.setItem(0, ItemStack.EMPTY);
                worldCrafter.setChanged();

                for (int i = 0; i < NUM_PILLARS; i++) {
                    double angle = 2 * Math.PI / NUM_PILLARS * i;
                    BlockPos pillarPos = new BlockPos(
                            pos.getX() + (int) Math.round(Math.cos(angle) * RADIUS),
                            pos.getY(),
                            pos.getZ() + (int) Math.round(Math.sin(angle) * RADIUS)
                    );

                    if (level.getBlockEntity(pillarPos) instanceof WorldCrafterPillarEntity pillar) {
                        pillar.inventory.setItem(0, ItemStack.EMPTY);
                        pillar.setChanged();
                    }
                }

                level.playSound(player, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, 1f);
                return InteractionResult.SUCCESS;
            }

            /* ---------------- NORMAL CRAFTING ---------------- */
            if (!centralStack.isEmpty()) {

                List<ItemStack> inputs = new ArrayList<>();
                inputs.add(centralStack.copy());

                for (int i = 0; i < NUM_PILLARS; i++) {
                    double angle = 2 * Math.PI / NUM_PILLARS * i;
                    BlockPos pillarPos = new BlockPos(
                            pos.getX() + (int) Math.round(Math.cos(angle) * RADIUS),
                            pos.getY(),
                            pos.getZ() + (int) Math.round(Math.sin(angle) * RADIUS)
                    );

                    if (level.getBlockEntity(pillarPos) instanceof WorldCrafterPillarEntity pillar) {
                        ItemStack pillarStack = pillar.inventory.getItem(0);
                        if (!pillarStack.isEmpty()) {
                            inputs.add(pillarStack.copy());
                        }
                    }
                }

                Optional<ItemStack> result = WorldCrafterRecipe.findMatchingRecipe(inputs);
                result.ifPresent(resultStack -> {
                    if (!player.getInventory().add(resultStack)) {
                        player.drop(resultStack, false);
                    }

                    worldCrafter.inventory.setItem(0, ItemStack.EMPTY);
                    worldCrafter.setChanged();

                    for (int i = 0; i < NUM_PILLARS; i++) {
                        double angle = 2 * Math.PI / NUM_PILLARS * i;
                        BlockPos pillarPos = new BlockPos(
                                pos.getX() + (int) Math.round(Math.cos(angle) * RADIUS),
                                pos.getY(),
                                pos.getZ() + (int) Math.round(Math.sin(angle) * RADIUS)
                        );

                        if (level.getBlockEntity(pillarPos) instanceof WorldCrafterPillarEntity pillar) {
                            pillar.inventory.setItem(0, ItemStack.EMPTY);
                            pillar.setChanged();
                        }
                    }

                    level.playSound(player, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1f, 1f);
                });

                return InteractionResult.SUCCESS;
            }

            return InteractionResult.SUCCESS;
        }

        /* ============================================================
           NORMAL RIGHT CLICK (PLACE / REMOVE)
           ============================================================ */
        if (worldCrafter.inventory.getItem(0).isEmpty() && !stack.isEmpty()) {
            worldCrafter.inventory.setItem(0, stack.copy());
            stack.shrink(1);
            level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
        } else if (stack.isEmpty()) {
            ItemStack taken = worldCrafter.inventory.removeItem(0, 1);
            player.setItemInHand(hand, taken);
            worldCrafter.clearContents();
            level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
        }

        return InteractionResult.SUCCESS;
    }
}
