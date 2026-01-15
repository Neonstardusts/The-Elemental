package com.teamneon.theelemental.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class HollowIce extends FrostedIceBlock {

    public HollowIce(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void melt(BlockState state, Level level, BlockPos pos) {
        // 'true' would drop items (like a loot table), 'false' just does particles + sound.
        level.destroyBlock(pos, false);
    }

}
