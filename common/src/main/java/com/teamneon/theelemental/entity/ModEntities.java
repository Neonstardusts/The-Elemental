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

    public static void initialize(BalmEntityTypeRegistrar registrar) {
        // Balm's register usually returns a Registration object that can be converted to a Holder
        WATER_SPELL = registrar.register(
                "water_spell",
                () -> EntityType.Builder.<WaterSpellEntity>of(WaterSpellEntity::new, MobCategory.MISC)
                        .sized(0.8f, 0.8f)
                        .clientTrackingRange(8)
                        .updateInterval(1)
        ).asHolder(); // Ensure you use .asHolder() or similar Balm equivalent
    }
}
