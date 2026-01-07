package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ChainLightningSpell extends Spell {

    public ChainLightningSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        Vec3 start = player.getEyePosition(1.0f); // Start from player's eyes
        Vec3 look = player.getLookAngle(); // Direction player is facing
        double maxDistance = 10; // max range of the initial lightning
        double step = 0.5; // particle step

        // Step 1: Raycast forward
        for (double i = 0; i < maxDistance; i += step) {
            Vec3 point = start.add(look.scale(i));
            level.addParticle(ParticleTypes.END_ROD, point.x, point.y, point.z, 0, 0, 0);

            // Check if we hit an entity
            List<Entity> entities = level.getEntities(player, player.getBoundingBox().expandTowards(look.scale(i)).inflate(0.5), e -> e instanceof LivingEntity && e != player);
            if (!entities.isEmpty()) {
                LivingEntity hit = (LivingEntity) entities.get(0);

                // Step 2: Create chain lightning to nearby entities
                List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, hit.getBoundingBox().inflate(4), e -> e != hit);
                for (LivingEntity target : nearby) {
                    spawnLightningLine(level, hit.position(), target.position());
                    // Optionally apply damage
                    target.hurt(player.damageSources().playerAttack(player), 4.0f);
                }
                break; // Stop the initial raycast after hitting
            }
        }

        return SpellCastResult.success();
    }

    /**
     * Spawns a line of particles from start to end.
     */
    private void spawnLightningLine(Level level, Vec3 start, Vec3 end) {
        int points = 20;
        Vec3 diff = end.subtract(start);
        for (int i = 0; i <= points; i++) {
            Vec3 point = start.add(diff.scale(i / (double) points));
            level.addParticle(ParticleTypes.END_ROD, point.x, point.y, point.z, 0, 0, 0);
        }
    }

}