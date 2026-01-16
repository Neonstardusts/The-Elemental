package com.teamneon.theelemental.magic.world.effects;

import com.teamneon.theelemental.magic.world.AbstractWorldEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public class ExplosiveSigilEffect extends AbstractWorldEffect {

    private final BlockPos pos;
    private static final double RADIUS = 1.2;

    // We now pass 'level' into the constructor so the Manager knows where this sigil lives
    public ExplosiveSigilEffect(Level level, BlockPos pos, UUID owner, int durationTicks) {
        super(level, owner, durationTicks);
        this.pos = pos;
    }

    @Override
    protected void tickEffect(Level level) {
        // Visuals: Flame Circle
        if (level instanceof ServerLevel serverLevel) {
            spawnCircleParticles(serverLevel);
        }

        // Logic: Check for proximity
        if (entityEntered(level)) {
            explode(level);
            // Instead of age = maxAge, we'll let the Manager handle cleanup next tick
            this.age = this.maxAge;
        }
    }

    private void spawnCircleParticles(ServerLevel serverLevel) {
        int particleCount = 16;
        double y = pos.getY() + 0.5;

        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;
            double xOffset = Math.cos(angle) * RADIUS;
            double zOffset = Math.sin(angle) * RADIUS;

            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    pos.getX() + 0.5 + xOffset,
                    y,
                    pos.getZ() + 0.5 + zOffset,
                    1, 0, 0, 0, 0
            );
        }
    }

    private boolean entityEntered(Level level) {
        // AABB box check
        AABB box = new AABB(pos).inflate(RADIUS);

        return !level.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> !e.getUUID().equals(owner) && e.isAlive()
        ).isEmpty();
    }

    private void explode(Level level) {
        Player ownerPlayer = level.getPlayerByUUID(owner);
        level.explode(
                ownerPlayer,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                3.0f,
                Level.ExplosionInteraction.NONE
        );
    }
}