package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.helpers.TempBlock;
import com.teamneon.theelemental.magic.base.DurationSpell;
import com.teamneon.theelemental.magic.base.ActiveSpellManager;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class PoisonSpraySpell extends DurationSpell {

    public PoisonSpraySpell() {
        super(300); // spell lasts 200 ticks
    }

    @Override
    public int getManaCost() {
        return 10;
    }

    @Override
    public int getCooldownTicks() {
        return 320;
    }

    @Override
    public void tick(Level level, Player player) {
        int particleCount = 30; // more particles for smooth cone
        double coneAngle = Math.toRadians(30); // half-angle of cone
        double range = 5.0;

        for (int i = 0; i < particleCount; i++) {
            // Random offsets in spherical coordinates
            double theta = (Math.random() - 0.5) * coneAngle; // horizontal angle
            double phi = (Math.random() - 0.5) * coneAngle;   // vertical angle

            // Original look vector
            Vec3 look = player.getLookAngle();

            // Rotate vector by horizontal (Y) and vertical (X) offsets
            double cosTheta = Math.cos(theta);
            double sinTheta = Math.sin(theta);
            double cosPhi = Math.cos(phi);
            double sinPhi = Math.sin(phi);

            // Horizontal rotation (yaw)
            double x1 = look.x * cosTheta - look.z * sinTheta;
            double z1 = look.x * sinTheta + look.z * cosTheta;
            double y1 = look.y;

            // Vertical rotation (pitch)
            double x2 = x1 * cosPhi - y1 * sinPhi;
            double y2 = x1 * sinPhi + y1 * cosPhi;
            double z2 = z1;

            // Scale by range
            Vec3 particlePos = player.position().add(0, player.getEyeHeight() * 0.5, 0)
                    .add(x2 * range, y2 * range, z2 * range);

            // Spawn particle (server-side for multiplayer)
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new DustColorTransitionOptions(0x6ca35f, 0x9be675, 1f),
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0, 0, 0, 0.1
                );
            }

            // Damage mobs
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
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
            ActiveSpellManager.addSpell(player, new PoisonSpraySpell()); return SpellCastResult.success();
        } else {
            return SpellCastResult.fail();
        }
    }
}
