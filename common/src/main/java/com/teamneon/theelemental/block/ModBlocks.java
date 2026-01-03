package com.teamneon.theelemental.block;

import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static DeferredBlock yourBlock;
    public static DeferredBlock ELEMENTAL_ALTAR;
    public static DeferredBlock WORLD_REACTOR;
    public static DeferredBlock KINGDOM_CORE;
    public static DeferredBlock KINGDOM_ANCHOR;

    public static void initialize(BalmBlockRegistrar blocks) {
        yourBlock = blocks.register("your_block", Block::new, it -> it.strength(1.5f)).withDefaultItem().asDeferredBlock();
        ELEMENTAL_ALTAR = blocks.register("elemental_altar", ElementalAltar::new,  it -> it.strength(1.5f)).withDefaultItem().asDeferredBlock();
        WORLD_REACTOR = blocks.register("world_reactor", WorldReactor::new,  it -> it.strength(1.5f)).withDefaultItem().asDeferredBlock();
        KINGDOM_CORE = blocks.register("kingdom_core", KingdomCoreBlock::new, it -> it.strength(5.0f).noOcclusion()).withDefaultItem().asDeferredBlock();
        KINGDOM_ANCHOR = blocks.register("kingdom_anchor", KingdomAnchor::new, it -> it.strength(5.0f).noOcclusion()).withDefaultItem().asDeferredBlock();

    }

}
