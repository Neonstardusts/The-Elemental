package com.teamneon.theelemental.block.entity;

import com.teamneon.theelemental.block.ModBlocks;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityTypeRegistrar;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityTypeRegistration;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static BalmBlockEntityTypeRegistration<KingdomCoreBlockEntity> KINGDOM_CORE_REG;

    public static void initialize(BalmBlockEntityTypeRegistrar registrars) {
        KINGDOM_CORE_REG = registrars.register("kingdom_core",
                KingdomCoreBlockEntity::new,
                ModBlocks.KINGDOM_CORE);
    }
}