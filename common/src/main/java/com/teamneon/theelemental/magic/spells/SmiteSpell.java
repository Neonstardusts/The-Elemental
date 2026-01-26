package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.api.ModSounds;
import com.teamneon.theelemental.magic.base.ActiveSpellManager;
import com.teamneon.theelemental.magic.base.DurationSpell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import com.teamneon.theelemental.particles.ModParticles;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SmiteSpell extends DurationSpell {

    public SmiteSpell(int manaCost, int cooldownTicks, String name, long durationTicks) {
        super(manaCost, cooldownTicks, name, durationTicks);
    }

    @Override
    public void tick(Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return;


        // 1. Calculate Progress (0.0 at start, 1.0 at end)
        float progress = ((float) this.ticksElapsed/(float)this.durationTicks);

        // 2. Scaling: Radius and Particle Size
        double radius = 3.0 - (3 * progress);
        float particleScale = 3.0f - (2.5f * progress);

        // 3. Acceleration & Rotation
        // Increases rotation speed over time
        double speedMultiplier = 1.0 + (progress * 8.0);
        double angle = (level.getGameTime() * speedMultiplier) * 0.2;

        double px = player.getX() + Math.cos(angle) * radius;
        double py = player.getY() + 1.5;
        double pz = player.getZ() + Math.sin(angle) * radius;

        // Constant lightning sparks for flavor
        serverLevel.sendParticles(ModParticles.STORM_SPARK.asSupplier().get(), px, py, pz, 2, 0.01, 0.01, 0.01, 0.05);

        // 5. Sound: Higher pitch as it accelerates
        if (level.getGameTime() % 5 == 0) { // Play every 5 ticks for a "whirring" feel
            float pitch = 0.5f + (progress * 1.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.ELECTRIC_DAMAGE, SoundSource.PLAYERS, 1.5f, pitch);
        }
    }


    @Override
    public void onDurationEnd(Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // 1. Target acquisition: Nearest 3 Living Entities within 10 blocks
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                        player.getBoundingBox().inflate(24),
                        entity -> entity != player && entity.isAlive())
                .stream()
                .sorted(java.util.Comparator.comparingDouble(player::distanceToSqr))
                .limit(3)
                .forEach(target -> {
                    // 2. Summon the actual Lightning
                    net.minecraft.world.entity.LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.MOB_SUMMONED);
                    if (bolt != null) {
                        bolt.setPos(target.position());
                        level.addFreshEntity(bolt);
                    }

                    // 3. Draw the Ombre Ray
                    // Start from player's chest/hand area, end at target's chest
                    spawnOmbreRay(serverLevel, player.position().add(0, 1.2, 0), target.position().add(0, 1, 0));
                });

        // Epic finish sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 2.0f, 0.8f);
    }

    private void spawnOmbreRay(ServerLevel serverLevel, Vec3 start, Vec3 end) {
        int points = 20; // Increased points for a smoother line
        Vec3 diff = end.subtract(start);

        // Color 1: Dark Grey (0x333333) | Color 2: Light Grey (0xAAAAAA)
        int r1 = 0x33, g1 = 0x33, b1 = 0x33;
        int r2 = 0xAA, g2 = 0xAA, b2 = 0xAA;

        for (int i = 0; i <= points; i++) {
            float t = (float) i / points;

            // Linear interpolation for the Ombre effect
            int r = (int) (r1 + (r2 - r1) * t);
            int g = (int) (g1 + (g2 - g1) * t);
            int b = (int) (b1 + (b2 - b1) * t);
            int packedColor = (r << 16) | (g << 8) | b;

            double x = start.x + diff.x * t;
            double y = start.y + diff.y * t;
            double z = start.z + diff.z * t;

            // Gradient Dust
            net.minecraft.core.particles.DustParticleOptions dust =
                    new net.minecraft.core.particles.DustParticleOptions(packedColor, 1.5f);
            serverLevel.sendParticles(dust, x, y, z, 1, 0, 0, 0, 0);

            // Add "Storm Sparks" occasionally along the ray for texture
            if (i % 4 == 0) {
                serverLevel.sendParticles(ModParticles.STORM_SPARK.asSupplier().get(),
                        x, y, z, 2, 0.1, 0.1, 0.1, 0.05);
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
