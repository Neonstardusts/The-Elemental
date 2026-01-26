package com.teamneon.theelemental.magic.world.effects;

import com.teamneon.theelemental.api.ModSounds;
import com.teamneon.theelemental.magic.world.AbstractWorldEffect;
import com.teamneon.theelemental.particles.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public class StormCloudEffect extends AbstractWorldEffect {

    private final BlockPos pos;
    private static final double RADIUS = 1.2;

    // We now pass 'level' into the constructor so the Manager knows where this sigil lives
    public StormCloudEffect(Level level, BlockPos pos, UUID owner, int durationTicks) {
        super(level, owner, durationTicks);
        this.pos = pos;
    }

    @Override
    protected void tickEffect(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // 1. Create the Cloud Body with Ombre Transitions
        for (int i = 0; i < 3; i++) {
            double offsetX = (serverLevel.random.nextDouble() - 0.5) * RADIUS * 2;
            double offsetZ = (serverLevel.random.nextDouble() - 0.5) * RADIUS * 2;
            double offsetY = (serverLevel.random.nextDouble() - 0.5) * 0.5;

            double px = pos.getX() + 0.5 + offsetX;
            double py = pos.getY() + 0.5 + offsetY;
            double pz = pos.getZ() + 0.5 + offsetZ;

            if (serverLevel.random.nextFloat() < 0.25f) {
                serverLevel.sendParticles(ModParticles.STORM_SPARK.asSupplier().get(), px, py, pz, 3, 0.3, 0.3, 0.3, 0.1);
            }

            float particleChoice = serverLevel.random.nextFloat();

            if (particleChoice < 0.60f) {
                // 60% Chance: Dark Grey to Light Grey transition
                DustColorTransitionOptions smoke = new DustColorTransitionOptions(0x333333, 0xAAAAAA, 3.5f);
                serverLevel.sendParticles(smoke, px, py, pz, 1, 0, 0, 0, 0.02);
            } else {
                // 40% Chance: Dark Grey to Cyan transition
                DustColorTransitionOptions energy = new DustColorTransitionOptions(0x333333, 0x00FFFF, 4f);
                serverLevel.sendParticles(energy, px, py, pz, 1, 0, 0, 0, 0.02);
            }
        }



        // 2. Add "Rain" underneath
        if (serverLevel.random.nextFloat() < 0.5f) {
            double rx = (serverLevel.random.nextDouble() - 0.5) * RADIUS;
            double rz = (serverLevel.random.nextDouble() - 0.5) * RADIUS;
            serverLevel.sendParticles(ParticleTypes.DRIPPING_WATER, pos.getX() + 0.5 + rx, pos.getY(), pos.getZ() + 0.5 + rz, 1, 0, 0, 0, 0.1);
        }

        // 3. Lightning logic
        if (serverLevel.random.nextFloat() < 0.15f) {
            strikeTargets(serverLevel);
        }
    }

    private void strikeTargets(ServerLevel level) {
        AABB strikeZone = new AABB(pos)
                .inflate(7.0, 0, 7.0)
                .expandTowards(0, -14, 0);

        var targets = level.getEntitiesOfClass(LivingEntity.class, strikeZone, entity -> {
            boolean isCaster = entity.getUUID().equals(this.getOwner());
            return entity.isAlive() && !entity.isSpectator() && !isCaster;
        });

        if (!targets.isEmpty()) {
            Player playerOwner = level.getPlayerByUUID(this.getOwner());

            for (LivingEntity target : targets) {
                // For every valid target in the zone, give them a 30% chance to be struck
                // This allows for single strikes, multi-strikes, or missing entirely
                if (level.random.nextFloat() < 0.3f) {
                    spawnLightningAt(level, target, playerOwner);
                }
            }
        }
    }

    private void spawnLightningAt(ServerLevel level, LivingEntity target, Player owner) {
        com.teamneon.theelemental.entity.SpawnLightningEntity bolt =
                new com.teamneon.theelemental.entity.SpawnLightningEntity(
                        level,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5
                );

        double targetY = target.getY() + (target.getBbHeight() * 0.5);
        bolt.setTarget(target.getX(), targetY, target.getZ());

        // Renders from cloud, credits player
        bolt.setSourceAndLife(owner, 5, false);

        level.addFreshEntity(bolt);

        // Effects
        target.hurt(level.damageSources().lightningBolt(), 5.0f);
        level.playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS, 0.4f, 1.2f);

        level.playSound(null, target.blockPosition(), ModSounds.ELECTRIC_DAMAGE,
                SoundSource.PLAYERS, 1.4f, 1.4f + level.random.nextFloat() * 0.6f);
    }
}