package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.ActiveSpellManager;
import com.teamneon.theelemental.magic.base.DurationSpell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PoisonSpraySpell extends DurationSpell {

    public PoisonSpraySpell(int manaCost, int cooldownTicks,  String name, long durationTicks) {
        super(manaCost, cooldownTicks, name, durationTicks);
    }

    @Override
    public void tick(Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        // 1. Start point 0.5 blocks ahead of eyes
        Vec3 startPoint = eyePos.add(look.scale(0.5));

        double range = 5.0; // Total length of the cone
        int rings = 5;      // Number of "steps" forward
        int perRing = 6;    // Particles per step

        // Find orthogonal vectors to the look vector to build the circles
        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(look.y) > 0.9) up = new Vec3(1, 0, 0); // Avoid parallel vectors
        Vec3 right = look.cross(up).normalize();
        Vec3 vertical = look.cross(right).normalize();

        for (int i = 1; i <= rings; i++) {
            double distance = (i / (double) rings) * range;
            // The radius grows as we move further away (the "fanning out" effect)
            double radius = distance * 0.4;

            Vec3 ringCenter = startPoint.add(look.scale(distance));

            for (int j = 0; j < perRing; j++) {
                double angle = (j / (double) perRing) * Math.PI * 2;

                // Calculate particle position on the ring
                Vec3 offset = right.scale(Math.cos(angle) * radius)
                        .add(vertical.scale(Math.sin(angle) * radius));
                Vec3 particlePos = ringCenter.add(offset);

                // Particles
                serverLevel.sendParticles(
                        new DustColorTransitionOptions(0x6ca35F, 0x9BE675, 1f),
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0, 0, 0, 0.0
                );
            }

            // Damage logic: Check a larger area once per ring rather than per particle
            AABB damageArea = new AABB(ringCenter.subtract(radius, radius, radius),
                    ringCenter.add(radius, radius, radius));

            DamageSource source = serverLevel.damageSources().playerAttack(player);
            for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, damageArea)) {
                if (entity != player && entity.isAlive()) {
                    entity.invulnerableTime = 0;
                    entity.hurtServer(serverLevel, source, 1.0f); // Reduced damage since it ticks every frame
                }
            }
        }
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (!level.isClientSide()) {
            ActiveSpellManager.addSpell(player, this);
            return SpellCastResult.success();
        }
        return SpellCastResult.fail();
    }
}
