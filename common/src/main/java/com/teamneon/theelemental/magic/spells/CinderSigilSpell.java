package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import com.teamneon.theelemental.magic.world.WorldEffectManager;
import com.teamneon.theelemental.magic.world.effects.ExplosiveSigilEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

public class CinderSigilSpell extends Spell {

    private static final double RAY_DISTANCE = 12.0;
    private final long sigilDuration;

    public CinderSigilSpell(int manaCost, int cooldownTicks, String name, long sigilDuration) {
        super(manaCost, cooldownTicks, name);
        this.sigilDuration = sigilDuration;
    }

    @Override
    public SpellCastResult checkConditions(Player player, Level level) {
        // We perform the raycast here to see if the player is actually looking at a block
        BlockPos hitPos = getTargetBlock(level, player);

        if (hitPos == null) {
            return SpellCastResult.fail("No valid target block!");
        }

        // Check if the block we are trying to place the sigil IN is air/replaceable
        if (!level.getBlockState(hitPos).isAir()) {
            return SpellCastResult.fail("Target surface is obstructed!");
        }

        return SpellCastResult.success();
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (level.isClientSide()) return SpellCastResult.fail();

        // We run the raycast again to get the position for execution
        BlockPos hitPos = getTargetBlock(level, player);

        if (hitPos != null) {
            Vec3 start = player.getEyePosition();
            Vec3 end = Vec3.atCenterOf(hitPos); // Visuals look better pointing at the actual sigil

            spawnRayParticlesServer(level, start, end);

            WorldEffectManager.add(new ExplosiveSigilEffect(level, hitPos, player.getUUID(), (int)sigilDuration));
            return SpellCastResult.success();
        }

        return SpellCastResult.fail();
    }

    /**
     * Helper to find the BlockPos where the sigil should be placed.
     * Returns the position relative to the face hit (the empty space in front of the block).
     */
    private BlockPos getTargetBlock(Level level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(RAY_DISTANCE));

        BlockHitResult result = level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        if (result.getType() == HitResult.Type.BLOCK) {
            return result.getBlockPos().relative(result.getDirection());
        }

        return null;
    }

    private void spawnRayParticlesServer(Level level, Vec3 start, Vec3 end) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        int points = 15;
        Vec3 diff = end.subtract(start);

        for (int i = 0; i <= points; i++) {
            double t = (double)i / points;
            double x = start.x + diff.x * t;
            double y = start.y + diff.y * t;
            double z = start.z + diff.z * t;

            serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0, 0, 0);
        }
    }
}