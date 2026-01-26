package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.api.ModSounds;
import com.teamneon.theelemental.entity.SpawnLightningEntity;
import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import com.teamneon.theelemental.particles.ModParticles;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ChainLightningSpell extends Spell {

    public ChainLightningSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (!level.isClientSide()) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            double range = 10.0;
            Vec3 reachVec = eyePos.add(lookVec.scale(range));

            // --- NEW: Casting Sound (Lower pitch for a "heavy" feel) ---
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.ELECTRIC_DAMAGE,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.6f, // Slightly louder
                    0.8f + level.random.nextFloat() * 0.4f); // Lower pitch (0.6 - 0.8)

            AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(2.5f);
            EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                    player, eyePos, reachVec, searchBox,
                    (entity) -> entity instanceof LivingEntity && entity != player,
                    range * range
            );

            if (hitResult != null && hitResult.getEntity() instanceof LivingEntity targetMob) {
                // --- PRIMARY STRIKE ---
                targetMob.hurt(level.damageSources().lightningBolt(), 5.0f);

                // NEW: Effects on primary hit
                spawnImpactEffects((net.minecraft.server.level.ServerLevel) level, targetMob);

                spawnVisualBolt(level, player, eyePos.subtract(0, 0.5, 0),
                        targetMob.position().add(0, targetMob.getBbHeight() * 0.5, 0), 5);

                // --- CHAIN LOGIC ---
                List<LivingEntity> chains = level.getEntitiesOfClass(LivingEntity.class,
                        targetMob.getBoundingBox().inflate(5.0),
                        (e) -> e != player && e != targetMob);

                int count = 0;
                for (LivingEntity chainTarget : chains) {
                    if (count >= 3) break;

                    chainTarget.hurt(level.damageSources().lightningBolt(), 3.0f);

                    // NEW: Effects on each chain target
                    spawnImpactEffects((net.minecraft.server.level.ServerLevel) level, chainTarget);

                    spawnVisualBolt(level, targetMob,
                            targetMob.position().add(0, targetMob.getBbHeight() * 0.5, 0),
                            chainTarget.position().add(0, chainTarget.getBbHeight() * 0.5, 0),
                            5);

                    count++;
                }
            } else {
                spawnVisualBolt(level, player, eyePos, reachVec, 5);
            }
        }
        return SpellCastResult.success();
    }

    private void spawnImpactEffects(net.minecraft.server.level.ServerLevel world, LivingEntity entity) {
        double px = entity.getX();
        double py = entity.getY() + (entity.getBbHeight() * 0.5);
        double pz = entity.getZ();

        // 1. Particles
        world.sendParticles(ModParticles.STORM_SPARK.asSupplier().get(),
                px, py, pz, 12, 0.3, 0.3, 0.3, 0.1);

        // 2. Sound
        // We use a high pitch (2.0f) for the "crack" of the lightning
        world.playSound(null, px, py, pz,
                ModSounds.ELECTRIC_DAMAGE,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.4f, 1.4f + world.random.nextFloat() * 0.6f);
    }

    /**
     * Spawns the visual lightning entity with the "Forward Push" fix
     */
    private void spawnVisualBolt(Level level, Entity source, Vec3 start, Vec3 end, int life) {
        // Calculate a spawn position slightly in front of the source eye height
        // to prevent 1st person culling
        Vec3 dir = end.subtract(start).normalize();
        Vec3 spawnPos = start.add(dir.scale(0.5));

        SpawnLightningEntity bolt = new SpawnLightningEntity(level, spawnPos.x, spawnPos.y, spawnPos.z);
        bolt.setTarget(end.x, end.y, end.z);
        bolt.setSourceAndLife(source, life, true);
        level.addFreshEntity(bolt);
    }
}