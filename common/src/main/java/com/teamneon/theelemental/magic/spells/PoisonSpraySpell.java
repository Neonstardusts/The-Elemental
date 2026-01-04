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
        int particleCount = 30;
        double coneAngle = Math.toRadians(30);
        double range = 5.0;

        for (int i = 0; i < particleCount; i++) {
            double theta = (Math.random() - 0.5) * coneAngle;
            double phi = (Math.random() - 0.5) * coneAngle;

            Vec3 look = player.getLookAngle();

            double cosTheta = Math.cos(theta);
            double sinTheta = Math.sin(theta);
            double cosPhi = Math.cos(phi);
            double sinPhi = Math.sin(phi);

            double x1 = look.x * cosTheta - look.z * sinTheta;
            double z1 = look.x * sinTheta + look.z * cosTheta;
            double y1 = look.y;

            double x2 = x1 * cosPhi - y1 * sinPhi;
            double y2 = x1 * sinPhi + y1 * cosPhi;
            double z2 = z1;

            Vec3 particlePos = player.position()
                    .add(0, player.getEyeHeight() * 0.5, 0)
                    .add(x2 * range, y2 * range, z2 * range);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new DustColorTransitionOptions(0x6ca35f, 0x9be675, 1f),
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0, 0, 0, 0.1
                );

                DamageSource source = serverLevel.damageSources().playerAttack(player);
                AABB area = new AABB(
                        particlePos.x - 0.5, particlePos.y - 0.5, particlePos.z - 0.5,
                        particlePos.x + 0.5, particlePos.y + 0.5, particlePos.z + 0.5
                );

                for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
                    if (entity != player && entity.isAlive()) {
                        entity.invulnerableTime = 0;
                        entity.hurtServer(serverLevel, source, 2.0f);
                    }
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
