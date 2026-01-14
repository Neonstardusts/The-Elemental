package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.entity.SpawnLightningEntity;
import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
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

            // 1. Use ProjectileUtil to find the mob the player is looking at
            AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(1.5);
            EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                    player,
                    eyePos,
                    reachVec,
                    searchBox,
                    (entity) -> entity instanceof LivingEntity && entity != player,
                    range * range
            );

            if (hitResult != null && hitResult.getEntity() instanceof LivingEntity targetMob) {
                // --- PRIMARY STRIKE ---
                // Damage the main mob (5.0 = 2.5 hearts)
                targetMob.hurt(level.damageSources().lightningBolt(), 5.0f);

                // Spawn bolt from Player to Main Mob
                spawnVisualBolt(level, player, eyePos.subtract(0, 0.5,0), targetMob.position().add(0, targetMob.getBbHeight() * 0.5, 0), 5);

                // --- CHAIN LOGIC ---
                // Find up to 3 nearby mobs in a 4-block radius
                List<LivingEntity> chains = level.getEntitiesOfClass(LivingEntity.class,
                        targetMob.getBoundingBox().inflate(4.0),
                        (e) -> e != player && e != targetMob);

                int count = 0;
                for (LivingEntity chainTarget : chains) {
                    if (count >= 3) break; // Limit chain to 3 extra targets

                    // Damage the chained mob (3.0 = 1.5 hearts)
                    chainTarget.hurt(level.damageSources().lightningBolt(), 3.0f);

                    // Spawn bolt from Main Mob to the Chain Target
                    spawnVisualBolt(level, targetMob,
                            targetMob.position().add(0, targetMob.getBbHeight() * 0.5, 0),
                            chainTarget.position().add(0, chainTarget.getBbHeight() * 0.5, 0),
                            5);

                    count++;
                }
            } else {
                // --- NO TARGET FOUND ---
                // Just shoot a bolt 10 blocks forward into the air/ground
                spawnVisualBolt(level, player, eyePos, reachVec, 5);
            }
        }
        return SpellCastResult.success();
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
        bolt.setSourceAndLife(source, life);
        level.addFreshEntity(bolt);
    }
}