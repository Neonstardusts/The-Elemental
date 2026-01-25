package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BlinkSpell extends Spell {

    private static final int PURPLE_HEX = 0x8000FF;

    public BlinkSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    // VITAL: Validates the target before mana is spent or teleport occurs
    @Override
    public SpellCastResult checkConditions(Player player, Level level) {
        Vec3 look = player.getLookAngle();
        Vec3 targetPos = player.position().add(look.scale(7.0));
        BlockPos targetBlock = BlockPos.containing(targetPos);

        if (!level.getBlockState(targetBlock).isAir()) {
            // Visual feedback for the failed attempt
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new DustParticleOptions(PURPLE_HEX, 1.0f),
                        targetPos.x, targetPos.y + 1.0, targetPos.z,
                        12, 0.2, 0.2, 0.2, 0.02);
            }
            return SpellCastResult.fail("Target location is obstructed!");
        }

        return SpellCastResult.success();
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 startPos = player.position().add(0, 1.0, 0);
        Vec3 targetPos = player.position().add(look.scale(7.0));

        // Success Visuals
        if (level instanceof ServerLevel serverLevel) {
            // The skinny smoke trail
            spawnSmokeLine(serverLevel, startPos, targetPos.add(0, 1.0, 0));

            // Departure: Purple burst
            serverLevel.sendParticles(new DustParticleOptions(PURPLE_HEX, 1.5f),
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    10, 0.3, 0.5, 0.3, 0.05);

            // Arrival: Warped portal effect
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    targetPos.x, targetPos.y + 1.0, targetPos.z,
                    15, 0.3, 0.5, 0.3, 0.02);
        }

        // Action
        player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);

        return SpellCastResult.success();
    }

    private void spawnSmokeLine(ServerLevel level, Vec3 start, Vec3 end) {
        double distance = start.distanceTo(end);
        int points = (int) (distance * 5);
        for (int i = 0; i <= points; i++) {
            Vec3 pos = start.lerp(end, (double) i / points);
            level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }
}