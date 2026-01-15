package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.block.ModBlocks;
import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import com.teamneon.theelemental.particles.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

public class IceShieldSpell extends Spell {

    public IceShieldSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (level.isClientSide()) return SpellCastResult.success();

        Vec3 look = player.getLookAngle();
        // This is the center point of our "circle" in front of the player
        Vec3 center = player.getEyePosition().add(look.scale(4));

        int radius = 5; // How big the wall is
        double thickness = 1.2; // Tolerance to make sure we don't have holes

        // Iterate through a small cube area around the center point
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos targetPos = BlockPos.containing(center.x + x, center.y + y, center.z + z);
                    Vec3 blockVec = targetPos.getCenter();

                    // 1. Check distance from the player (Sphere check)
                    double distFromPlayer = blockVec.distanceTo(player.getEyePosition());

                    // 2. Check angle relative to where the player is looking (Cone/Slice check)
                    Vec3 dirToBlock = blockVec.subtract(player.getEyePosition()).normalize();
                    double dot = dirToBlock.dot(look);

                    // dot > 0.9 means roughly a 25-degree cone in front of the player
                    if (distFromPlayer >= 3.0 && distFromPlayer <= 4.5 && dot > 0.85) {
                        if (level.getBlockState(targetPos).canBeReplaced()) {
                            // Use your custom Hollow Ice block here
                            level.setBlockAndUpdate(targetPos, ModBlocks.HOLLOW_ICE.defaultBlockState());

                            // Add some cool frost particles
                            ((ServerLevel)level).sendParticles(ModParticles.FROST_PARTICLE.asSupplier().get(),
                                    targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                                    2, 0.1, 0.1, 0.1, 0.05);
                        }
                    }
                }
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1.0f, 1.0f);
        return SpellCastResult.success();
    }
}