package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import com.teamneon.theelemental.magic.world.WorldEffectManager;
import com.teamneon.theelemental.magic.world.effects.ExplosiveSigilEffect;
import com.teamneon.theelemental.magic.world.effects.StormCloudEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class StormCloudSpell extends Spell {

    private static final double MAX_RAY_DISTANCE = 10.0;
    private final long sigilDuration;

    public StormCloudSpell(int manaCost, int cooldownTicks, String name, long sigilDuration) {
        super(manaCost, cooldownTicks, name);
        this.sigilDuration = sigilDuration;
    }

    @Override
    public SpellCastResult checkConditions(Player player, Level level) {
        // Since we now have a "fallback" position in the air,
        // this spell almost always has a valid target.
        return SpellCastResult.success();
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (level.isClientSide()) return SpellCastResult.fail();

        BlockPos targetPos = calculateCloudPosition(level, player);

        // Visual ray from player to the cloud center
        Vec3 start = player.getEyePosition();
        Vec3 end = Vec3.atCenterOf(targetPos);
        spawnRayParticlesServer(level, start, end);

        level.playSound(null, targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.8f,
                0.5f + level.random.nextFloat() * 1.5f);

        // Spawn the actual effect
        WorldEffectManager.add(new StormCloudEffect(level, targetPos, player.getUUID(), (int)sigilDuration));

        return SpellCastResult.success();
    }

    private BlockPos calculateCloudPosition(Level level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(MAX_RAY_DISTANCE));

        BlockHitResult result = level.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
        ));

        BlockPos basePos;

        if (result.getType() == HitResult.Type.BLOCK) {
            // If we hit a block, we start our search from that block
            basePos = result.getBlockPos();
        } else {
            // If we hit nothing (air), we start our search from the end of the 10-block ray
            basePos = BlockPos.containing(end);
        }

        // --- The Safety Lift Logic ---
        // If the base position is solid, we move up until we find air (max 4 blocks)
        while (!level.getBlockState(basePos).isAir() && basePos.getY() < level.getMaxY()) {
            basePos = basePos.above();
            // Optional: limit this search so it doesn't go infinitely up if in a solid world
            if (basePos.getY() > result.getBlockPos().getY() + 4) break;
        }

        // Now that we are (hopefully) in air, we want to go up 4 blocks for the "Cloud height"
        // But we check each step to make sure we don't go through a ceiling
        BlockPos finalPos = basePos;
        for (int i = 0; i < 4; i++) {
            BlockPos nextUp = finalPos.above();
            if (level.getBlockState(nextUp).isAir()) {
                finalPos = nextUp;
            } else {
                // Hit a ceiling! Stop here.
                break;
            }
        }

        return finalPos;
    }

    private void spawnRayParticlesServer(Level level, Vec3 start, Vec3 end) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        int points = 15;
        Vec3 diff = end.subtract(start);

        // Color 1: Dark Grey (0x333333)
        int colorStart = 0x333333;
        // Color 2: Bright Cyan (0x00FFFF)
        int colorEnd = 0x00FFFF;

        // Extract RGB from hex
        int r1 = (colorStart >> 16) & 0xFF;
        int g1 = (colorStart >> 8) & 0xFF;
        int b1 = colorStart & 0xFF;

        int r2 = (colorEnd >> 16) & 0xFF;
        int g2 = (colorEnd >> 8) & 0xFF;
        int b2 = colorEnd & 0xFF;

        for (int i = 0; i <= points; i++) {
            float t = (float) i / points;

            // Lerp individual components
            int r = (int) (r1 + (r2 - r1) * t);
            int g = (int) (g1 + (g2 - g1) * t);
            int b = (int) (b1 + (b2 - b1) * t);

            // Re-pack into 0xRRGGBB
            int packedColor = (r << 16) | (g << 8) | b;

            // In this version, DustParticleOptions takes (int color, float scale)
            net.minecraft.core.particles.DustParticleOptions dust =
                    new net.minecraft.core.particles.DustParticleOptions(packedColor, 1.0f);

            double x = start.x + diff.x * t;
            double y = start.y + diff.y * t;
            double z = start.z + diff.z * t;

            serverLevel.sendParticles(dust, x, y, z, 1, 0, 0, 0, 0);
        }
    }
}