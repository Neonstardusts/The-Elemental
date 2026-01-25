package com.teamneon.theelemental.magic.world.effects;

import com.teamneon.theelemental.magic.world.AbstractWorldEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;

import java.util.UUID;

public class GrowthSigilEffect extends AbstractWorldEffect {

    private final BlockPos centerPos;
    private static final double RADIUS = 6.0;
    private static final int VERTICAL_REACH = 2;
    private static final int TICKS_PER_OPERATION = 4;

    public GrowthSigilEffect(Level level, BlockPos pos, UUID owner, int durationTicks) {
        super(level, owner, durationTicks);
        this.centerPos = pos;
    }

    @Override
    protected void tickEffect(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        spawnDetailedNatureRing(serverLevel);
        applyFilteredAreaGrowth(serverLevel);
    }

    private void spawnDetailedNatureRing(ServerLevel level) {
        int potentialPoints = 120;
        double y = centerPos.getY() + 0.15;
        ColorParticleOption greenLeaf = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, 0.0F, 0.8F, 0.0F);

        for (int i = 0; i < potentialPoints; i++) {
            if (level.random.nextFloat() > 0.15F) continue;

            double angle = 2 * Math.PI * i / potentialPoints;
            double offsetX = Math.cos(angle) * RADIUS;
            double offsetZ = Math.sin(angle) * RADIUS;

            double px = centerPos.getX() + 0.5 + offsetX;
            double pz = centerPos.getZ() + 0.5 + offsetZ;

            int type = i % 4;
            switch (type) {
                case 0 -> level.sendParticles(greenLeaf, px, y, pz, 1, 0, 0, 0, 0);
                case 1 -> level.sendParticles(ParticleTypes.CHERRY_LEAVES, px, y, pz, 1, 0.02, 0.02, 0.02, 0);
                case 2 -> level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, px, y, pz, 1, 0.05, 0.05, 0.05, 0);
                case 3 -> level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, y, pz, 1, 0, 0, 0, 0);
            }
        }
    }

    private void applyFilteredAreaGrowth(ServerLevel level) {
        ItemStack dummyStack = new ItemStack(Items.BONE_MEAL, 64);

        for (int i = 0; i < TICKS_PER_OPERATION; i++) {
            int x = centerPos.getX() + level.random.nextInt((int) RADIUS * 2 + 1) - (int) RADIUS;
            int z = centerPos.getZ() + level.random.nextInt((int) RADIUS * 2 + 1) - (int) RADIUS;
            int y = centerPos.getY() + level.random.nextInt(VERTICAL_REACH * 2 + 1) - VERTICAL_REACH;

            BlockPos targetPos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(targetPos);

            // NEW FILTER LOGIC:
            // 1. Don't bonemeal the floor (Grass Block/Dirt/Moss) - this prevents weeds from spawning.
            // 2. Don't bonemeal existing weeds (Short grass/Ferns).
            if (isForbiddenBaseBlock(state) || isGrassBlock(state)) {
                continue;
            }

            if (BoneMealItem.growCrop(dummyStack, level, targetPos)) {
                level.levelEvent(1505, targetPos, 15);

                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                        5, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }

    /**
     * Blocks that, when bonemealed, create unwanted "clutter" like tall grass and flowers.
     */
    private boolean isForbiddenBaseBlock(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.MOSS_BLOCK);
    }

    /**
     * Helper to identify short grass, tall grass, or other "weed" blocks.
     */
    private boolean isGrassBlock(BlockState state) {
        return state.is(Blocks.SHORT_GRASS) ||
                state.is(Blocks.TALL_GRASS) ||
                state.is(Blocks.FERN) ||
                state.is(Blocks.LARGE_FERN) ||
                state.is(BlockTags.REPLACEABLE_BY_TREES); // Catches most small foliage
    }
}