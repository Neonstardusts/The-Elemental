package com.teamneon.theelemental.block.entity;

import com.teamneon.theelemental.block.ModBlocks;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityTypeRegistrar;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityTypeRegistration;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    public static BalmBlockEntityTypeRegistration<KingdomCoreBlockEntity> KINGDOM_CORE_REG;
    public static BalmBlockEntityTypeRegistration<ElementalAltarBlockEntity> ELEMENTAL_ALTAR_REG;
    public static BalmBlockEntityTypeRegistration<WorldCrafterEntity> WORLDCRAFTER_BE;
    public static BalmBlockEntityTypeRegistration<WorldCrafterPillarEntity> WORLDCRAFTER_PILLAR_BE;

    public static void initialize(BalmBlockEntityTypeRegistrar registrars) {
        KINGDOM_CORE_REG = registrars.register("kingdom_core",
                KingdomCoreBlockEntity::new,
                ModBlocks.KINGDOM_CORE);

        ELEMENTAL_ALTAR_REG = registrars.register("elemental_altar",
                ElementalAltarBlockEntity::new,
                ModBlocks.ELEMENTAL_ALTAR);

        WORLDCRAFTER_BE = registrars.register("world_crafter",
        WorldCrafterEntity::new,
        ModBlocks.WORLD_CRAFTER);

        WORLDCRAFTER_PILLAR_BE = registrars.register("world_crafter_pillar",
                WorldCrafterPillarEntity::new,
                ModBlocks.WORLD_CRAFTER_PILLAR);
    }
}