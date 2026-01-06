package com.teamneon.theelemental.magic.world.effects;

import com.teamneon.theelemental.magic.world.AbstractWorldEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public class ExplosiveSigilEffect extends AbstractWorldEffect {

    private final BlockPos pos;
    private static final double RADIUS = 1.2;

    public ExplosiveSigilEffect(BlockPos pos, UUID owner, int durationTicks) {
        super(owner, durationTicks); // maxAge = durationTicks
        this.pos = pos;
    }

    @Override
    protected void tickEffect(Level level) {

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            int particleCount = 16; // number of particles around the circle
            double y = pos.getY() + 0.5; // height of the circle (center of block)

            for (int i = 0; i < particleCount; i++) {
                double angle = 2 * Math.PI * i / particleCount;
                double xOffset = Math.cos(angle) * RADIUS;
                double zOffset = Math.sin(angle) * RADIUS;

                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        pos.getX() + 0.5 + xOffset,
                        y,
                        pos.getZ() + 0.5 + zOffset,
                        1,      // only one particle per point
                        0, 0, 0,// no random offset
                        0       // speed
                );
            }
        }

        if (entityEntered(level)) {
            explode(level);
            age = maxAge;
        }
    }

    private boolean entityEntered(Level level) {
        AABB box = new AABB(pos).inflate(RADIUS);

        return !level.getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class,
                box,
                e -> !e.getUUID().equals(owner)
        ).isEmpty();
    }

    private void explode(Level level) {
        Player ownerPlayer = level.getPlayerByUUID(owner);
        level.explode(
                ownerPlayer,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                3.0f, // explosion radius
                Level.ExplosionInteraction.NONE
        );
    }
}
