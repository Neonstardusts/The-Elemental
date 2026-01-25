package com.teamneon.theelemental.block;

import com.teamneon.theelemental.particles.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jspecify.annotations.Nullable;

public class SpectralBlock extends FrostedIceBlock {

    public SpectralBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }


    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        // We bypass IceBlock logic entirely to prevent water spawning
        player.causeFoodExhaustion(0.005F);
        level.removeBlock(pos, false);

        // Optional: trigger the same spectral melt effects when a player breaks it
        this.melt(state, level, pos);
    }


    @Override
    protected void melt(BlockState state, Level level, BlockPos pos) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            level.removeBlock(pos, false);

            serverLevel.sendParticles(ModParticles.SORCERY_PARTICLE.asSupplier().get(),
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    8, 0.2, 0.2, 0.2, 0.02);

            if (level.random.nextFloat() < 0.2f) {
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.BLOCKS, 1.0f, 1.5f);
            }
        }
    }

    

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}