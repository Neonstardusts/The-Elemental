package com.teamneon.theelemental.mixin;

import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.particles.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel; // Added this import
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ImmunityMixin {

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void handleEnvironmentalEffects(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        // 1. Check if the entity is a player
        if ((Object) this instanceof Player player) {

            // 2. IMPORTANT: Only run server-specific logic (like adding effects or checking ServerLevel)
            // if we are actually on the server side.
            if (!player.level().isClientSide() && player.level() instanceof ServerLevel level) {

                int element = ElementalDataHandler.get(player).getElement();

                // --- Element 1: Fire Weakness in Water ---
                if (element == 1 && player.isInWater()) {
                    // 1. Existing Slowness/Weakness (Duration 20 ticks, Amp 1 = Level II)
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            MobEffects.SLOWNESS, 20, 1, false, false));
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.WEAKNESS, 20, 1, false, false));

                    // 2. Smoke Particles (20% chance per tick)
                    if (player.getRandom().nextFloat() < 0.25f) {
                        level.sendParticles(ParticleTypes.SMOKE,
                                player.getX(), player.getY() + 0.5, player.getZ(),
                                3, 0.2, 0.5, 0.2, 0.02);
                    }

                    // 3. Damage and Extinguish Sound (2% chance per tick)
                    if (player.getRandom().nextFloat() < 0.02f) {
                        // Damage the player (0.5 hearts) using generic damage
                        player.hurtServer(level, player.damageSources().generic(), 1.0f);

                        // Play the "hiss" sound of fire being extinguished
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.FIRE_EXTINGUISH,
                                net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.0f);

                        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                                player.getX(), player.getY() + 0.5, player.getZ(),
                                6, 0.2, 0.5, 0.2, 0.02);
                    }
                }

                // --- Element 2: Light/Dark Mechanics ---
                if (element == 2) {
                    // level is now safely a ServerLevel here
                    int blockLight = level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, player.blockPosition());
                    int skyLight = level.getBrightness(LightLayer.SKY, player.blockPosition());
                    int currentSkyLight = level.getSkyDarken();
                    int actualLight = Math.max(blockLight, skyLight - currentSkyLight);

                    if (actualLight > 12 && player.getHealth() < player.getMaxHealth()) {
                        if (player.getRandom().nextFloat() < 0.05f) {
                            player.heal(1.0f);
                            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                                    player.getX(), player.getY() + 1.5, player.getZ(), 1, 0.3, 0.3, 0.3, 0.1);
                        }
                    } else if (actualLight < 3 ) {
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.SLOWNESS, 20, 0, false, false));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.DARKNESS, 20, 0, false, false));

                    }
                }

                // --- Element 7: Light/Dark Mechanics ---
                if (element == 7) {
                    // level is now safely a ServerLevel here
                    int blockLight = level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, player.blockPosition());
                    int skyLight = level.getBrightness(LightLayer.SKY, player.blockPosition());
                    int currentSkyLight = level.getSkyDarken();
                    int actualLight = Math.max(blockLight, skyLight - currentSkyLight);

                    if (actualLight < 5) {
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.SPEED, 2, 0, true, false));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.INVISIBILITY, 2, 1, true, false));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.NIGHT_VISION, 2, 1, true, false));
                        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                                player.getX(), player.getY()+0.5, player.getZ(),
                                2, 0.2, 0.4, 0.2, 0.05);

                    } else if (actualLight > 12) {
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.SLOWNESS, 2, 0, false, false));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.WEAKNESS, 2, 0, false, false));

                    }
                }

                if (element == 7) {
                    // end strength
                    if (level.dimension() == net.minecraft.world.level.Level.END) {
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.REGENERATION, 20, 0, false, false));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.SPEED, 20, 0, false, false));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.STRENGTH, 20, 0, false, false));
                    }
                }

                if (element == 4) {
                    // 1. Check for Overworld (Regen/Weakness logic)
                    if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD) {
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.WEAKNESS, 20, 0, false, false));
                    }

                    // 2. Scan upwards for Leaves (up to 5 blocks above)
                    boolean foundLeaves = false;
                    net.minecraft.core.BlockPos.MutableBlockPos mutablePos = new net.minecraft.core.BlockPos.MutableBlockPos();

                    for (int i = 1; i <= 5; i++) {
                        mutablePos.set(player.getX(), player.getY() + i, player.getZ());
                        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(mutablePos);

                        // We check if it's in the #LEAVES tag (works for all tree types)
                        if (state.is(net.minecraft.tags.BlockTags.LEAVES)) {
                            foundLeaves = true;
                            break;
                        }

                        // If it's not air and not leaves, the path is blocked by something else (e.g., stone)
                        // You can remove this 'else if' if you want it to detect leaves through solid ceilings.
                        else if (!state.isAir()) {
                            break;
                        }
                    }

                    if (foundLeaves) {
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.REGENERATION, 20, 0, false, true));
                    }
                }


                // --- Element 5: Electricity ---
                if (element == 5) {
                    int surfaceY = level.getSeaLevel();

                    // Underground = below surface AND has a roof
                    boolean isUnderground = player.getY() < surfaceY && !level.canSeeSky(player.blockPosition());

                    if (isUnderground) {
                        // Slowness while underground (Heavy feet)
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                MobEffects.SLOWNESS, 20, 1, false, false));
                    } else {
                        // 2. Sprinting on the Surface
                        if (player.isSprinting()) {
                            // Give Speed II (Amp 1)
                            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    MobEffects.SPEED, 2, 0, true, false));

                            // 3. Custom Storm Spark Particles
                            if (player.getRandom().nextFloat() < 0.3f) {
                                level.sendParticles(ModParticles.STORM_SPARK.asSupplier().get(),
                                        player.getX(), player.getY()+0.5, player.getZ(),
                                        3, 0.2, 0.4, 0.2, 0.05);
                            }
                        }
                    }
                }

                if (element == 9) {
                    // If in air and NOT sneaking
                    if (!player.onGround() && !player.isShiftKeyDown()) {

                        // Apply Slow Falling for 2 ticks (just enough to keep it active)
                        // ambient = true, showParticles = false (since we handle particles ourselves)
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.SLOW_FALLING, 2, 0, true, false));

                        // Spawn your custom particles under feet
                        if (player.level() instanceof ServerLevel serverLevel && player.getRandom().nextFloat() < 0.2f) {
                            serverLevel.sendParticles(ParticleTypes.DUST_PLUME,
                                    player.getX(), player.getY() - 0.2, player.getZ(),
                                    1, 0.1, 0.0, 0.1, 0.01);
                        }
                    } else {
                        // If they touch the ground or start sneaking, remove it immediately
                        if (player.hasEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING)) {
                            player.removeEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING);
                        }
                    }
                }
            }
        }
    }

    // We use "hurtServer" because that is the name in your source file.
    // remap = false tells the game to look for that exact name in the code.
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true, remap = false)
    private void handleElementImmunities(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        if ((Object) this instanceof Player player) {
            int element = ElementalDataHandler.get(player).getElement();

            // Fire Immunity (Element 1)
            if (element == 1 && source.is(DamageTypeTags.IS_FIRE)) {
                cir.setReturnValue(false);
            }

            // Fire Weakness (Element 6 - Water)
            if (element == 6 && source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
                // 1. Play the "hiss" sound of water evaporating
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);

                // 2. Spawn thick steam/smoke particles
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        2, 0.3, 0.5, 0.3, 0.05);
            }

            if (element == 9 && source.is(DamageTypeTags.IS_FALL)) {
                cir.setReturnValue(false);
            }

        }
    }

    @org.spongepowered.asm.mixin.injection.ModifyVariable(
            method = "hurtServer",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false
    )
    private float adjustDamageAmount(float amount, ServerLevel level, DamageSource source) {
        if ((Object) this instanceof Player player) {
            int element = ElementalDataHandler.get(player).getElement();

            // Boost Fire damage for Water Element (6)
            if (element == 6 && source.is(DamageTypeTags.IS_FIRE)) {
                return amount * 1.5f;
            }
        }
        return amount;
    }

    @Inject(method = "canBreatheUnderwater", at = @At("HEAD"), cancellable = true, remap = false)
    private void handleWaterBreathing(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player) {
            int element = ElementalDataHandler.get(player).getElement();

            //Water breathing
            if (element == 6) {
                cir.setReturnValue(true);
            }
        }
    }

}