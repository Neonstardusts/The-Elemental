package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LuxSpell extends Spell {

    private static final double MAX_DISTANCE = 12.0;

    public LuxSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getLookAngle();
        Vec3 end = start.add(direction.scale(MAX_DISTANCE));

        // Play casting sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 2.0F, 2F);

        // 1) Raycast for blocks
        BlockHitResult blockHit = level.clip(
                new ClipContext(
                        start,
                        end,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                )
        );

        Vec3 hitPos = end; // default if nothing hit

        // 2) Check for entities along ray
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().expandTowards(direction.scale(MAX_DISTANCE)).inflate(1D),
                e -> e != player
        );

        double closestDist = MAX_DISTANCE;
        LivingEntity closestEntity = null;

        for (LivingEntity entity : entities) {
            Vec3 entityPos = entity.position();
            double dist = start.distanceTo(entityPos);
            if (dist < closestDist) {
                closestDist = dist;
                closestEntity = entity;
            }
        }

        // 3) Decide target first (entity vs. block)
        if (closestEntity != null &&
                closestDist < start.distanceTo(blockHit.getLocation())) {

            // → Hit entity
            closestEntity.hurt(
                    player.damageSources().playerAttack(player),
                    6.0F
            );

            hitPos = closestEntity.position();

            level.playSound(null, hitPos.x, hitPos.y, hitPos.z,
                    SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0F, 2F);

        } else if (blockHit.getType() != HitResult.Type.MISS) {

            // → Hit block
            Vec3 blockPoint = blockHit.getLocation();
            hitPos = blockPoint;

            BlockPos placePos = BlockPos.containing(blockPoint.subtract(direction.scale(0.1)));
            if (level.getBlockState(placePos).isAir()) {
                level.setBlock(placePos, Blocks.LIGHT.defaultBlockState(), 3);
            }
            // Play casting sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.PLAYERS, 1.0F, 2F);

        } else {

            // → Hit nothing: place light at 75% of range
            Vec3 placePosVec = start.add(direction.scale(MAX_DISTANCE * 0.75));
            hitPos = placePosVec;

            BlockPos placePos = BlockPos.containing(placePosVec);
            if (level.getBlockState(placePos).isAir()) {
                level.setBlock(placePos, Blocks.LIGHT.defaultBlockState(), 3);
            }
            // Play casting sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.PLAYERS, 1.0F, 2F);
        }

        // 4) Spawn particle beam along the ray
        if (level instanceof ServerLevel server) {
            int steps = 20; // how many particle steps along the beam
            for (int i = 0; i <= steps; i++) {
                double t = i / (double) steps;
                Vec3 particlePos = start.add(direction.scale(start.distanceTo(hitPos) * t));
                server.sendParticles(
                        ParticleTypes.END_ROD,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0, 0, 0, 0
                );
            }

            // Spawn impact particles at the hit position
            server.sendParticles(
                    ParticleTypes.END_ROD,
                    hitPos.x, hitPos.y + 0.5, hitPos.z,
                    20, 0.5, 0.5, 0.5, 0.03
            );
        }

        return SpellCastResult.success();
    }
}
