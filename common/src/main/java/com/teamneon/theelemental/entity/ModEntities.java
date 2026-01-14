package com.teamneon.theelemental.entity;

import com.teamneon.theelemental.Theelemental;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.blay09.mods.balm.world.entity.BalmEntityTypeRegistrar;

import java.util.function.Supplier;

public class ModEntities {
    // Change this from EntityType to Holder<EntityType<WaterSpellEntity>>
    public static Holder<EntityType<WaterSpellEntity>> WATER_SPELL;
    public static Holder<EntityType<SpawnLightningEntity>> SPAWN_LIGHTNING;

    public static void initialize(BalmEntityTypeRegistrar registrar) {
        // Balm's register usually returns a Registration object that can be converted to a Holder
        WATER_SPELL = registrar.register(
                "water_spell",
                () -> EntityType.Builder.<WaterSpellEntity>of(WaterSpellEntity::new, MobCategory.MISC)
                        .sized(0.8f, 0.8f)
                        .clientTrackingRange(8)
                        .updateInterval(1)
        ).asHolder(); // Ensure you use .asHolder() or similar Balm equivalent

        SPAWN_LIGHTNING = registrar.register(
                "spawn_lightning",
                () -> EntityType.Builder.<SpawnLightningEntity>of(SpawnLightningEntity::new, MobCategory.MISC)
                        .sized(0.1f, 0.1f) // Size doesn't matter much since it's just a line
                        .clientTrackingRange(64) // Increased range so players see the bolt from far away
                        .updateInterval(1)
        ).asHolder();
    }
}
