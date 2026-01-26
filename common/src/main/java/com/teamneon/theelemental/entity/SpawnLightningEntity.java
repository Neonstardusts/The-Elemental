package com.teamneon.theelemental.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class SpawnLightningEntity extends Projectile {
    private static final EntityDataAccessor<Float> TARGET_X = SynchedEntityData.defineId(SpawnLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TARGET_Y = SynchedEntityData.defineId(SpawnLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TARGET_Z = SynchedEntityData.defineId(SpawnLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> MAX_LIFE = SynchedEntityData.defineId(SpawnLightningEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SOURCE_ID = SynchedEntityData.defineId(SpawnLightningEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FROM_SOURCE = SynchedEntityData.defineId(SpawnLightningEntity.class, EntityDataSerializers.BOOLEAN);

    public SpawnLightningEntity(EntityType<? extends SpawnLightningEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SpawnLightningEntity(Level level, double x, double y, double z) {
        this(ModEntities.SPAWN_LIGHTNING.value(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TARGET_X, 0.0f);
        builder.define(TARGET_Y, 0.0f);
        builder.define(TARGET_Z, 0.0f);
        builder.define(MAX_LIFE, 5);
        builder.define(SOURCE_ID, -1);
        builder.define(FROM_SOURCE, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.tickCount > this.entityData.get(MAX_LIFE)) {
                this.discard();
            }
        }
    }


    // --- GETTERS & SETTERS ---

    public void setTarget(double x, double y, double z) {
        this.entityData.set(TARGET_X, (float) x);
        this.entityData.set(TARGET_Y, (float) y);
        this.entityData.set(TARGET_Z, (float) z);
    }

    public Vector3f getTarget() {
        return new Vector3f(entityData.get(TARGET_X), entityData.get(TARGET_Y), entityData.get(TARGET_Z));
    }

    public void setSourceAndLife(Entity source, int life, boolean fromSource) {
        if (source != null) {
            this.entityData.set(SOURCE_ID, source.getId());
            this.setOwner(source);
        } else {
            this.entityData.set(SOURCE_ID, -1);
            this.setOwner((Entity) null);
        }
        this.entityData.set(FROM_SOURCE, fromSource);
        this.entityData.set(MAX_LIFE, life);
    }

    public boolean shouldRenderFromSource() {
        return this.entityData.get(FROM_SOURCE);
    }

    public int getSourceId() { return this.entityData.get(SOURCE_ID); }
}