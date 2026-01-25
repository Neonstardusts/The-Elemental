package com.teamneon.theelemental.block;

import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public class ModBlocks {

    public static DeferredBlock yourBlock;
    public static DeferredBlock ELEMENTAL_ALTAR;
    public static DeferredBlock WORLD_REACTOR;
    public static DeferredBlock KINGDOM_CORE;
    public static DeferredBlock KINGDOM_ANCHOR;
    public static DeferredBlock WORLD_CRAFTER;
    public static DeferredBlock WORLD_CRAFTER_PILLAR;
    public static DeferredBlock HOLLOW_ICE;
    public static DeferredBlock SPECTRAL_BLOCK;


    public static void initialize(BalmBlockRegistrar blocks) {
        yourBlock = blocks.register("your_block", Block::new, it -> it.strength(-1.0f).pushReaction(PushReaction.BLOCK)).withDefaultItem().asDeferredBlock();

        ELEMENTAL_ALTAR = blocks.register("elemental_altar", ElementalAltar::new, it -> it.strength(-1.0f).pushReaction(PushReaction.BLOCK)).withDefaultItem().asDeferredBlock();

        WORLD_REACTOR = blocks.register("world_reactor", WorldReactor::new, it -> it.strength(-1.0f).pushReaction(PushReaction.BLOCK)).withDefaultItem().asDeferredBlock();

        KINGDOM_CORE = blocks.register("kingdom_core", KingdomCoreBlock::new, it -> it.strength(-1.0f).pushReaction(PushReaction.BLOCK).noOcclusion()).withDefaultItem().asDeferredBlock();

        KINGDOM_ANCHOR = blocks.register("kingdom_anchor", KingdomAnchor::new, it -> it.strength(-1.0f).pushReaction(PushReaction.BLOCK).noOcclusion()).withDefaultItem().asDeferredBlock();

        WORLD_CRAFTER = blocks.register("world_crafter", WorldCrafter::new, it -> it.strength(-1.0f).pushReaction(PushReaction.BLOCK).noOcclusion()).withDefaultItem().asDeferredBlock();

        WORLD_CRAFTER_PILLAR = blocks.register("world_crafter_pillar", WorldCrafterPillar::new, it -> it.strength(-1.0f).pushReaction(PushReaction.BLOCK).noOcclusion()).withDefaultItem().asDeferredBlock();

        HOLLOW_ICE = blocks.register("hollow_ice", HollowIce::new, it ->
                BlockBehaviour.Properties.ofFullCopy(Blocks.FROSTED_ICE)
        ).asDeferredBlock();

        SPECTRAL_BLOCK = blocks.register("spectral_block", SpectralBlock::new, it ->
                it.strength(0.5f)
                        .friction(0.6f)
                        .speedFactor(1.2f)
                        .noOcclusion()
                        .noLootTable()
                        .randomTicks()
                        .lightLevel(state -> 7)
                        .hasPostProcess((state, level, pos) -> true)
                        .sound(SoundType.GLASS)
        ).asDeferredBlock();
    }

}
