package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.ActiveSpellManager;
import com.teamneon.theelemental.magic.base.DurationSpell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import com.teamneon.theelemental.particles.ModParticles;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class SpectralShotSpell extends DurationSpell {

    public SpectralShotSpell(int manaCost, int cooldownTicks, String name, long durationTicks) {
        super(manaCost, cooldownTicks, name, durationTicks);
    }

    @Override
    public void tick(Level level, Player player) {
        if (level.isClientSide()) return;

        // 1. Rate Limiting: Every 5 ticks
        if (player.tickCount % 5 != 0) return;

        // 2. Target acquisition
        double reachDistance = 40.0;
        HitResult hitResult = player.pick(reachDistance, 0.0f, false);
        Vec3 targetPos = hitResult.getLocation();

        // 3. Find spawn position (behind and above)
        Random random = new Random();
        Vec3 lookVec = player.getLookAngle();
        Vec3 spawnPos = null;

        for (int i = 0; i < 10; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 10.0;
            double offsetY = random.nextDouble() * 5.0 + 3.0;
            double offsetZ = (random.nextDouble() - 0.5) * 10.0;

            Vec3 potentialPos = player.position().add(offsetX, offsetY, offsetZ);
            Vec3 toPotential = potentialPos.subtract(player.position()).normalize();

            if (toPotential.dot(lookVec) < 0) {
                spawnPos = potentialPos;
                break;
            }
        }

        if (spawnPos == null) spawnPos = player.position().add(0, 5, 0);

        // 4. Calculate flight math
        Vec3 trajectory = targetPos.subtract(spawnPos);
        Vec3 direction = trajectory.normalize();
        double speed = 3.0;

        // 5. Spawn the arrow
        Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), null);
        arrow.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        arrow.setDeltaMovement(direction.scale(speed));
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        arrow.addTag("spectral_shot");
        arrow.setNoGravity(true);

        level.addFreshEntity(arrow);

        // 6. Particle Trail (The first 3 blocks of flight)
        // We use ServerLevel to ensure everyone nearby sees the magic
        net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) level;
        double trailLength = 10.0;
        double stepSize = 0.3; // Distance between each particle

        for (double d = 0; d < trailLength; d += stepSize) {
            // Position = Start + (Direction * Distance)
            Vec3 particlePos = spawnPos.add(direction.scale(d));

            serverLevel.sendParticles(
                    ModParticles.SORCERY_PARTICLE.asSupplier().get(),
                    particlePos.x, particlePos.y, particlePos.z,
                    2,     // count
                    0.05, 0.05, 0.05, // spread/jitter
                    0.02   // speed
            );
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
