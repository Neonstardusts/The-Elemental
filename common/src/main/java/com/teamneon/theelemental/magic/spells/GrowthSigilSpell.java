package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import com.teamneon.theelemental.magic.world.WorldEffectManager;
import com.teamneon.theelemental.magic.world.effects.GrowthSigilEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class GrowthSigilSpell extends Spell {

    private static final double RAY_DISTANCE = 12.0;
    private final long sigilDuration;

    public GrowthSigilSpell(int manaCost, int cooldownTicks, String name, long sigilDuration) {
        super(manaCost, cooldownTicks, name);
        this.sigilDuration = sigilDuration;
    }

    @Override
    public SpellCastResult checkConditions(Player player, Level level) {
        BlockPos hitPos = getTargetBlock(level, player);

        if (hitPos == null) {
            return SpellCastResult.fail("No valid target surface!");
        }

        BlockState state = level.getBlockState(hitPos);

        // ALLOW PLACEMENT IF:
        // 1. It is air
        // 2. It is a replaceable block (tall grass, flowers, etc.)
        // 3. It is not a full solid block (like a torch or sign)
        boolean isPlaceable = state.isAir() || state.canBeReplaced() || !state.isSolid();

        if (!isPlaceable) {
            return SpellCastResult.fail("Target space is blocked!");
        }

        return SpellCastResult.success();
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (level.isClientSide()) return SpellCastResult.success(); // Return success to trigger swing/mana

        BlockPos hitPos = getTargetBlock(level, player);

        if (hitPos != null) {
            Vec3 start = player.getEyePosition();
            Vec3 end = Vec3.atCenterOf(hitPos);

            spawnRayParticlesServer(level, start, end);

            WorldEffectManager.add(new GrowthSigilEffect(level, hitPos, player.getUUID(), (int)sigilDuration));
            return SpellCastResult.success();
        }

        return SpellCastResult.fail();
    }

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
            // This is the magic line.
            // If you hit the top of the dirt, this returns the Air block above it.
            // If you hit the side of a tree, this returns the Air block next to it.
            return result.getBlockPos().relative(result.getDirection());
        }

        return null;
    }

    private void spawnRayParticlesServer(Level level, Vec3 start, Vec3 end) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int points = 15;
        Vec3 diff = end.subtract(start);

        for (int i = 0; i <= points; i++) {
            double t = (double)i / points;
            double x = start.x + diff.x * t;
            double y = start.y + diff.y * t;
            double z = start.z + diff.z * t;

            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, 0, 0, 0, 0);
        }
    }
}