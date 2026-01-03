package com.teamneon.theelemental.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TempBlock {

    // All active temp blocks across all levels
    private static final Map<BlockPos, TempBlock> ACTIVE_BLOCKS = new ConcurrentHashMap<>();

    private final Level level;
    private final BlockPos pos;
    private final BlockState originalState;
    private final BlockState newState;
    private final long revertTick; // game tick when this block should revert

    private TempBlock(Level level, BlockPos pos, BlockState newState, long durationTicks) {
        this.level = level;
        this.pos = pos;
        this.originalState = level.getBlockState(pos);
        this.newState = newState;
        this.revertTick = level.getGameTime() + durationTicks;
    }

    /** Place a temporary block and return the instance */
    public static TempBlock place(Level level, BlockPos pos, BlockState state, long durationTicks) {
        TempBlock temp = new TempBlock(level, pos, state, durationTicks);
        ACTIVE_BLOCKS.put(pos, temp);
        level.setBlock(pos, state, 2);
        return temp;
    }

    /** Revert immediately */
    public void revert() {
        level.setBlockAndUpdate(pos, originalState);
        ACTIVE_BLOCKS.remove(pos);
    }

    /** Tick all active temp blocks and revert expired ones */
    public static void tickAll() {
        Iterator<TempBlock> iterator = ACTIVE_BLOCKS.values().iterator();
        while (iterator.hasNext()) {
            TempBlock temp = iterator.next();
            if (temp.level.getGameTime() >= temp.revertTick) {
                temp.level.setBlock(temp.pos, temp.originalState, 2);
                iterator.remove();
            }
        }
    }

    public static boolean isTempBlock(Level level, BlockPos pos) {
        TempBlock tb = ACTIVE_BLOCKS.get(pos);
        return tb != null;
    }

    /** Convenience helper for almost-full water (level = 1) */
    public static TempBlock placeAlmostFullWater(Level level, BlockPos pos, long durationTicks) {
        BlockState water = net.minecraft.world.level.block.Blocks.WATER
                .defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LEVEL, 5);
        return place(level, pos, water, durationTicks);
    }
}
