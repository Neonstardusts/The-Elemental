package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.block.ModBlocks;
import com.teamneon.theelemental.helpers.UtilityHelper;
import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import com.teamneon.theelemental.particles.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SpectralBridgeSpell extends Spell {

    private static final double MAX_DISTANCE = 12.0;

    public SpectralBridgeSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (level.isClientSide()) return SpellCastResult.success();

        ServerLevel serverLevel = (ServerLevel) level;

        // 1. Fixed Direction Mapping
        // Minecraft: 0=S, 90=W, 180=N, 270=E
        float yaw = player.getYRot();
        float normalizedYaw = ((yaw % 360) + 360) % 360;
        int directionIndex = Math.round(normalizedYaw / 45) & 7;

        // INVERTED VECTORS: These now match the actual looking direction
        // Index: 0:S, 1:SW, 2:W, 3:NW, 4:N, 5:NE, 6:E, 7:SE
        int[] dx = { 0, -1, -1, -1,  0,  1,  1,  1};
        int[] dz = { 1,  1,  0, -1, -1, -1,  0,  1};

        int mainX = dx[directionIndex];
        int mainZ = dz[directionIndex];

        boolean isDiagonal = (mainX != 0 && mainZ != 0);

        // 2. Determine Sidestep for Cardinal 3-wide
        int sideX = (mainX == 0) ? 1 : 0;
        int sideZ = (mainX != 0) ? (mainZ == 0 ? 1 : 0) : 0;

        BlockPos startPos = player.blockPosition().below();

        for (int i = 1; i < 24; i++) {
            BlockPos centerLine = startPos.offset(mainX * i, 0, mainZ * i);

            // Wall detection
            if (!level.getBlockState(centerLine).canBeReplaced() &&
                    !level.getBlockState(centerLine).is(ModBlocks.SPECTRAL_BLOCK.asBlock())) {
                break;
            }

            if (isDiagonal) {
                // DIAGONAL: Thicker 3-Block Logic
                // Places center, and both blocks that touch the corner to make it solid
                placeBridgeBlock(level, serverLevel, centerLine);
                placeBridgeBlock(level, serverLevel, centerLine.offset(-mainX, 0, 0));
                placeBridgeBlock(level, serverLevel, centerLine.offset(0, 0, -mainZ));
            } else {
                // CARDINAL: 3-Wide Logic
                for (int offset = -1; offset <= 1; offset++) {
                    BlockPos targetPos = centerLine.offset(sideX * offset, 0, sideZ * offset);
                    placeBridgeBlock(level, serverLevel, targetPos);
                }
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 2.0f);
        return SpellCastResult.success();
    }

    private void placeBridgeBlock(Level level, ServerLevel serverLevel, BlockPos pos) {
        if (level.getBlockState(pos).canBeReplaced()) {
            level.setBlockAndUpdate(pos, ModBlocks.SPECTRAL_BLOCK.asBlock().defaultBlockState());
            serverLevel.sendParticles(ModParticles.SORCERY_PARTICLE.asSupplier().get(),
                    pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    1, 0.1, 0.1, 0.1, 0.02);
        }
    }
}
