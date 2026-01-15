package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.helpers.UtilityHelper;
import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import com.teamneon.theelemental.particles.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FreezeSpell extends Spell {

    private static final double MAX_DISTANCE = 12.0;

    public FreezeSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(MAX_DISTANCE));

        // 1) Raycast for Blocks
        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, player));
        double blockDist = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation().distanceTo(start) : MAX_DISTANCE;

        // 2) Raycast for Entities (stopping at the block hit if one exists)
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(MAX_DISTANCE)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, start, end, searchBox, (e) -> e instanceof LivingEntity && !e.isSpectator(), blockDist * blockDist);

        Vec3 finalHitPos;
        LivingEntity targetEntity = null;

        if (entityHit != null) {
            targetEntity = (LivingEntity) entityHit.getEntity();
            finalHitPos = entityHit.getLocation();
        } else {
            finalHitPos = blockHit.getLocation();
        }

        // --- VISUALS: Particle Line ---
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            double distance = start.distanceTo(finalHitPos);
            int particleCount = (int) (distance * 4); // 4 particles per block
            for (int i = 0; i < particleCount; i++) {
                double step = (double) i / particleCount;
                Vec3 pos = start.lerp(finalHitPos, step);
                // Use your custom Frost Particle here
                serverLevel.sendParticles(ModParticles.FROST_PARTICLE.asSupplier().get(), pos.x, pos.y, pos.z, 1, 0.1, 0.1, 0.1, 0.02);
            }

            // Sound at impact
            level.playSound(null, finalHitPos.x, finalHitPos.y, finalHitPos.z, SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1.0F, 1.2F);
        }

        // 3) EFFECT LOGIC
        if (targetEntity != null) {
            // HIT ENTITY
            targetEntity.hurt(level.damageSources().magic(), 4.0F);
            targetEntity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
            targetEntity.setTicksFrozen(targetEntity.getTicksFrozen() + 100);
        }
        else if (blockHit.getType() != HitResult.Type.MISS) {
            // HIT BLOCK (Water or Ground)
            BlockPos center = blockHit.getBlockPos();
            int radius = 5;

            for (BlockPos currentPos : BlockPos.betweenClosed(center.offset(-radius, -1, -radius), center.offset(radius, 1, radius))) {
                if (currentPos.closerThan(center, radius)) {
                    BlockState state = level.getBlockState(currentPos);
                    BlockPos abovePos = currentPos.above();
                    BlockState aboveState = level.getBlockState(abovePos);

                    // Water to Ice (Frost Walker style)
                    if (state.is(Blocks.WATER) && aboveState.isAir()) {
                        level.setBlockAndUpdate(currentPos, Blocks.FROSTED_ICE.defaultBlockState());
                    }
                    // Ground to Snow Layer
                    else if (aboveState.isAir()) {
                        BlockState snowState = Blocks.SNOW.defaultBlockState();

                        // This checks if the snow can actually stay on the block below (state)
                        if (snowState.canSurvive(level, abovePos)) {
                            // Ensure we aren't replacing something we shouldn't
                            if (!state.is(Blocks.SNOW) && !state.is(Blocks.FROSTED_ICE)) {
                                level.setBlockAndUpdate(abovePos, snowState);
                            }
                        }
                    }
                }
            }
        }

        return SpellCastResult.success();
    }
}