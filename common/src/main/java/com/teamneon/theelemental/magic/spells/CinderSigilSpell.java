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
    private final long sigilDuration; // how long the sigil will last in ticks

    public CinderSigilSpell(int manaCost, int cooldownTicks, String name, long sigilDuration) {
        super(manaCost, cooldownTicks, name);
        this.sigilDuration = sigilDuration;
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (level.isClientSide()) return SpellCastResult.fail();

        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(RAY_DISTANCE));
        // Raycast to find target block
        BlockPos hitPos = raycast(level, player);
        spawnRayParticlesServer(level, start, end);
        if (hitPos != null) {
            // Spawn the sigil with a duration
            WorldEffectManager.add(new ExplosiveSigilEffect(hitPos, player.getUUID(), (int)sigilDuration));
            return SpellCastResult.success();
        }

        return SpellCastResult.fail("No valid target block!");
    }

    private BlockPos raycast(Level level, Player player) {
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
            // Place the sigil at the face of the block
            return result.getBlockPos().relative(result.getDirection());
        }

        return null;
    }

    private void spawnRayParticlesServer(Level level, Vec3 start, Vec3 end) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        int points = 20; // number of particles along the ray
        Vec3 diff = end.subtract(start);

        for (int i = 0; i <= points; i++) {
            double t = (double)i / points;
            double x = start.x + diff.x * t;
            double y = start.y + diff.y * t;
            double z = start.z + diff.z * t;

            serverLevel.sendParticles(
                    ParticleTypes.FLAME, // particle type
                    x, y, z,            // particle position
                    1,                   // particle count
                    0, 0, 0,             // offset
                    0                    // speed
            );
        }
    }


}
