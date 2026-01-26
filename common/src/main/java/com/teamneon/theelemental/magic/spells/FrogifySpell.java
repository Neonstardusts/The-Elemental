package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class FrogifySpell extends Spell {

    private static final double MAX_DISTANCE = 12.0;
    private static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("minecraft", "bosses"));

    public FrogifySpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    @Override
    public SpellCastResult checkConditions(Player player, Level level) {
        LivingEntity target = getTargetEntity(level, player);

        if (level instanceof ServerLevel serverLevel) {
            spawnRayParticlesServer(serverLevel, player.getEyePosition(), target.position().add(0, target.getBbHeight() / 2, 0));
        }

        if (target == null || !target.isAlive()) {
            return SpellCastResult.fail("No valid creature in sight!");
        }



        if (target.getType().is(BOSSES)) {
            return SpellCastResult.fail("This creature's soul is too heavy to transform!");
        }

        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        if (health > 15.0f && health > (maxHealth * 0.8f)) {
            return SpellCastResult.fail("Target is too healthy! Weaken them first.");
        }

        return SpellCastResult.success();
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return SpellCastResult.success();

        LivingEntity victim = getTargetEntity(level, player);
        if (victim != null) {
            spawnRayParticlesServer(serverLevel, player.getEyePosition(), victim.position().add(0, victim.getBbHeight() / 2, 0));

            Frog frog = EntityType.FROG.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
            if (frog != null) {
                // Position and Rotation
                frog.setPos(victim.getX(), victim.getY(), victim.getZ());
                frog.setYRot(victim.getYRot());
                frog.setXRot(victim.getXRot());
                frog.setYHeadRot(victim.getYHeadRot());

                // --- Variant Selection Logic ---
                double roll = level.getRandom().nextDouble();
                ResourceKey<FrogVariant> variantKey = (roll < 0.60) ? FrogVariants.COLD :
                        (roll < 0.80) ? FrogVariants.TEMPERATE :
                                FrogVariants.WARM;

                // --- Apply via Data Components (Bypasses private visibility) ---
                serverLevel.registryAccess()
                        .lookup(Registries.FROG_VARIANT)
                        .flatMap(registry -> registry.get(variantKey))
                        .ifPresent(holder -> {
                            // Use the public component system instead of the private setter
                            frog.setComponent(DataComponents.FROG_VARIANT, holder);
                        });

                // --- Visuals & Sounds ---
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, victim.getX(), victim.getY() + 0.5, victim.getZ(), 20, 0.2, 0.2, 0.2, 0);
                serverLevel.sendParticles(ParticleTypes.POOF, victim.getX(), victim.getY() + 0.5, victim.getZ(), 22, 0.3, 0.3, 0.3, 0);

                level.playSound(null, victim.blockPosition(), SoundEvents.WITCH_CELEBRATE, SoundSource.PLAYERS, 1.5f, 1.2f);
                level.playSound(null, victim.blockPosition(), SoundEvents.FROG_AMBIENT, SoundSource.NEUTRAL, 2.0f, 1.0f);

                serverLevel.addFreshEntity(frog);
                victim.discard();

                return SpellCastResult.success();
            }
        }
        return SpellCastResult.fail();
    }

    private LivingEntity getTargetEntity(Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0F);
        Vec3 reachVec = eyePos.add(viewVec.scale(MAX_DISTANCE));
        AABB searchBox = player.getBoundingBox().expandTowards(viewVec.scale(MAX_DISTANCE)).inflate(1.75D, 1.75D, 1.75D);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player, eyePos, reachVec, searchBox,
                (entity) -> entity instanceof LivingEntity && !entity.isSpectator(),
                MAX_DISTANCE * MAX_DISTANCE
        );
        return (entityHit != null) ? (LivingEntity) entityHit.getEntity() : null;
    }

    private void spawnRayParticlesServer(ServerLevel level, Vec3 start, Vec3 end) {
        int points = 20;
        Vec3 diff = end.subtract(start);
        ColorParticleOption greenLeaf = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, 0.0F, 0.8F, 0.0F);

        for (int i = 0; i <= points; i++) {
            double t = (double) i / points;
            double px = start.x + diff.x * t;
            double py = start.y + diff.y * t;
            double pz = start.z + diff.z * t;

            double chance = level.getRandom().nextDouble();
            if (chance < 0.75) {
                for (int j = 0; j < 2; j++) {
                    double offsetX = (level.getRandom().nextDouble() - 0.5) * 0.15;
                    double offsetY = (level.getRandom().nextDouble() - 0.5) * 0.15;
                    double offsetZ = (level.getRandom().nextDouble() - 0.5) * 0.15;

                    if (chance < 0.25) {
                        level.sendParticles(greenLeaf, px + offsetX, py + offsetY, pz + offsetZ, 1, 0, 0, 0, 0);
                    } else if (chance < 0.50) {
                        level.sendParticles(ParticleTypes.CHERRY_LEAVES, px + offsetX, py + offsetY, pz + offsetZ, 1, 0, 0, 0, 0);
                    } else {
                        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, px + offsetX, py + offsetY, pz + offsetZ, 1, 0, 0, 0, 0);
                    }
                }
            }
        }
    }
}