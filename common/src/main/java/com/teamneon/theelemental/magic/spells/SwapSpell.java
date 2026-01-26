package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SwapSpell extends Spell {

    private static final int PURPLE_HEX = 0x69289c;
    private static final int TEAL_HEX = 0x3d7d67;

    public SwapSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        double range = 24.0;
        Vec3 startPos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 endPos = startPos.add(look.scale(range));

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player, startPos, endPos,
                player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.75D),
                entity -> entity instanceof LivingEntity && !entity.isSpectator(),
                range * range
        );

        if (hitResult == null) {
            spawnParticleLine(level, startPos, startPos.add(look.scale(10.0)), 1.0f);
            return SpellCastResult.fail("No target in range!");
        }

        Entity target = hitResult.getEntity();
        Vec3 targetOldPos = target.position();
        Vec3 playerOldPos = player.position();

        // 1. Success: Full gradient line
        spawnParticleLine(level, startPos, target.getEyePosition(), 2.2f);

        // 2. Execute Swap
        target.teleportTo(playerOldPos.x, playerOldPos.y, playerOldPos.z);
        player.teleportTo(targetOldPos.x, targetOldPos.y, targetOldPos.z);

        // 3. Deal 8 Damage (4 hearts) to the entity
        if (target instanceof LivingEntity living) {
            living.hurt(level.damageSources().magic(), 8.0f);
        }

        // 4. Effects: Player (Purple cloud) / Entity (Teal cloud)
        // We spawn the color that "clings" to them from their origin
        spawnCloud(level, player.position(), PURPLE_HEX);
        spawnCloud(level, target.position(), TEAL_HEX);

        playWarpSound(level, playerOldPos);
        playWarpSound(level, targetOldPos);

        return SpellCastResult.success();
    }

    private void spawnCloud(Level level, Vec3 pos, int colorHex) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Spawns a small burst of 15 particles around the entity
        serverLevel.sendParticles(new DustParticleOptions(colorHex, 1.5f),
                pos.x, pos.y + 1.0, pos.z, 15, 0.4, 0.5, 0.4, 0.05);
    }

    private void spawnParticleLine(Level level, Vec3 start, Vec3 end, float size) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        double distance = start.distanceTo(end);
        int points = (int) (distance * 4);

        for (int i = 0; i <= points; i++) {
            float pct = (float) i / points;
            Vec3 pos = start.lerp(end, pct);
            int currentColor = lerpHex(PURPLE_HEX, TEAL_HEX, pct);

            serverLevel.sendParticles(new DustParticleOptions(currentColor, size),
                    pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    private int lerpHex(int startColor, int endColor, float pct) {
        int r1 = (startColor >> 16) & 0xFF;
        int g1 = (startColor >> 8) & 0xFF;
        int b1 = startColor & 0xFF;
        int r2 = (endColor >> 16) & 0xFF;
        int g2 = (endColor >> 8) & 0xFF;
        int b2 = endColor & 0xFF;

        int r = (int) (r1 + (r2 - r1) * pct);
        int g = (int) (g1 + (g2 - g1) * pct);
        int b = (int) (b1 + (b2 - b1) * pct);

        return (r << 16) | (g << 8) | b;
    }

    private void playWarpSound(Level level, Vec3 pos) {
        level.playSound(null, pos.x, pos.y, pos.z,
                net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.6F);
    }
}