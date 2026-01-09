package com.teamneon.theelemental.client;

import com.teamneon.theelemental.block.entity.ModBlockEntities;
import com.teamneon.theelemental.block.renderer.WorldCrafterEntityRenderer;
import com.teamneon.theelemental.block.renderer.WorldCrafterPillarEntityRenderer;
import com.teamneon.theelemental.client.renderer.ElementalAltarRenderer;
import com.teamneon.theelemental.client.renderer.KingdomCoreRenderer;
import net.blay09.mods.balm.client.renderer.blockentity.BalmBlockEntityRendererRegistrar;

public class ModRenderers {

    public static void initialize(BalmBlockEntityRendererRegistrar renderers) {
        // Balm expects the Holder and the Provider (the constructor reference)
        renderers.register(
                ModBlockEntities.WORLDCRAFTER_BE.asHolder(),
                WorldCrafterEntityRenderer::new
        );

        renderers.register(
                ModBlockEntities.KINGDOM_CORE_REG.asHolder(),
                KingdomCoreRenderer::new
        );

        renderers.register(
                ModBlockEntities.ELEMENTAL_ALTAR_REG.asHolder(),
                ElementalAltarRenderer::new
        );

        renderers.register(
                ModBlockEntities.WORLDCRAFTER_PILLAR_BE.asHolder(),
                WorldCrafterPillarEntityRenderer::new
        );
    }
}