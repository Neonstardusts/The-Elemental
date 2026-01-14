package com.teamneon.theelemental.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class WaterSpellEntity extends Projectile {
    public static final int MODE_STRAIGHT = 0;
    public static final int MODE_ORBIT = 1;

    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(WaterSpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFE = SynchedEntityData.defineId(WaterSpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ORBIT_ANGLE = SynchedEntityData.defineId(WaterSpellEntity.class, EntityDataSerializers.FLOAT);

    public WaterSpellEntity(EntityType<? extends WaterSpellEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public WaterSpellEntity(Level level, double x, double y, double z) {
        this(ModEntities.WATER_SPELL.value(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(MODE, MODE_STRAIGHT);
        builder.define(MAX_LIFE, 100);
        builder.define(ORBIT_ANGLE, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.entityData.get(MODE) == MODE_ORBIT) {
            handleOrbitLogic();
        } else {
            this.move(MoverType.SELF, this.getDeltaMovement());
        }

        if (!this.level().isClientSide()) {
            // 1. Hit Detection for Mobs
            this.level().getEntities(this, this.getBoundingBox().inflate(0.2)).forEach(target -> {
                if (target != this.getOwner()) {
                    target.hurt(this.damageSources().magic(), 5.0f);
                    this.splash(); // Trigger Splash
                    this.discard();
                }
            });

            // 2. Hit Detection for Blocks
            if (!this.level().getBlockState(this.blockPosition()).isAir()) {
                this.splash(); // Trigger Splash
                this.discard();
            }

            // 3. Lifespan check
            if (this.tickCount > this.entityData.get(MAX_LIFE)) {
                this.discard(); // Usually we don't splash on timeout, but you can add it here if you want
            }
        }
    }

    /**
     * Creates the visual and auditory "Splash" effect.
     */
    private void splash() {
        if (this.level() instanceof ServerLevel serverLevel) {
            // Play Sound: Sound, Category, Pos, Volume, Pitch
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_SPLASH, SoundSource.NEUTRAL, 0.5f, 1.2f + this.random.nextFloat() * 0.4f);

            // Spawn Particles
            // Parameters: ParticleType, x, y, z, count, spreadX, spreadY, spreadZ, speed
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    this.getX(), this.getY(), this.getZ(),
                    12, 0.2, 0.2, 0.2, 0.1);

            serverLevel.sendParticles(ParticleTypes.BUBBLE,
                    this.getX(), this.getY(), this.getZ(),
                    5, 0.1, 0.1, 0.1, 0.05);
        }
    }

    private void handleOrbitLogic() {
        if (this.getOwner() instanceof Player player) {
            float currentAngle = this.entityData.get(ORBIT_ANGLE);
            currentAngle += 0.30f;

            if (!this.level().isClientSide()) {
                this.entityData.set(ORBIT_ANGLE, currentAngle);
            }

            double radius = 2;
            double x = player.getX() + Math.cos(currentAngle) * radius;
            double z = player.getZ() + Math.sin(currentAngle) * radius;

            this.setPos(x, player.getY() + 1.2, z);
        } else if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    public void setMode(int mode) { this.entityData.set(MODE, mode); }
    public void setMaxLife(int ticks) { this.entityData.set(MAX_LIFE, ticks); }
    public void setOrbitAngle(float angle) { this.entityData.set(ORBIT_ANGLE, angle); }
}