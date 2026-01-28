package com.teamneon.theelemental.client;


import com.mojang.serialization.MapCodec;
import com.teamneon.theelemental.block.ModBlocks;
import com.teamneon.theelemental.client.model.*;
import com.teamneon.theelemental.client.renderer.SpawnLightningRenderer;
import com.teamneon.theelemental.client.renderer.WaterSpellRenderer;
import com.teamneon.theelemental.client.tooltip.ModTooltips;
import com.teamneon.theelemental.entity.ModEntities;
import com.teamneon.theelemental.entity.WaterSpellEntity;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.helpers.UtilityHelper;
import com.teamneon.theelemental.item.property.ElementIdProperty;
import com.teamneon.theelemental.item.property.SpellVariantProperty;
import com.teamneon.theelemental.menu.ElementChooserScreen;
import com.teamneon.theelemental.menu.ElementalRuneCutterScreen;
import com.teamneon.theelemental.menu.ModMenuTypes;
import com.teamneon.theelemental.menu.SoulForgeScreen;
import com.teamneon.theelemental.particles.FrostParticle;
import com.teamneon.theelemental.particles.ModParticles;
import com.teamneon.theelemental.particles.SorceryParticle;
import com.teamneon.theelemental.particles.StormSpark;
import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.blay09.mods.balm.client.platform.event.callback.RenderCallback;
import net.blay09.mods.balm.client.platform.event.callback.ScreenCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import static com.teamneon.theelemental.Theelemental.id;
import static com.teamneon.theelemental.block.KingdomAnchor.ELEMENT;
import static com.teamneon.theelemental.block.KingdomCoreBlock.ELEMENTCore;


public class TheelementalClient {

    public static void initialize(BalmClientRegistrars registrars) {
        ModKeyMappings.initialize();

        registrars.blockEntityRenderers(ModRenderers::initialize);
        registrars.clientTooltipComponents(ModTooltips::initialize);

        registrars.blockRenderTypes(registrar -> {
            registrar.setRenderLayer(ModBlocks.HOLLOW_ICE, net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);
            registrar.setRenderLayer(ModBlocks.SPECTRAL_BLOCK, net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);

        });


        registrars.entityRenderers(registrar -> {
            registrar.register(
                    ModEntities.WATER_SPELL,
                    WaterSpellRenderer::new
            );

            registrar.register(
                    ModEntities.SPAWN_LIGHTNING,
                    SpawnLightningRenderer::new
            );


        });

        registrars.modelLayers(registrar -> {
            // Original Water Spell
            registrar.register(
                    ModModelLayers.WATER_SPELL.model(),
                    ModModelLayers.WATER_SPELL.layer(),
                    WaterSpellModel::createBodyLayer
            );

            // Element 1: Standard Wings
            registrar.register(
                    ModModelLayers.WINGS.model(),
                    ModModelLayers.WINGS.layer(),
                    WingCosmeticModel::createBodyLayer
            );

            // Element 2: Flutter Wings (Smaller/Faster)
            registrar.register(
                    ModModelLayers.FLUTTER_WINGS.model(),
                    ModModelLayers.FLUTTER_WINGS.layer(),
                    FlutterWingModel::createBodyLayer
            );

            // Element 3: Halo
            registrar.register(
                    ModModelLayers.HALO.model(),
                    ModModelLayers.HALO.layer(),
                    HaloModel::createBodyLayer
            );

            // Element 4: Flower Crown
            registrar.register(
                    ModModelLayers.FLOWER_CROWN.model(),
                    ModModelLayers.FLOWER_CROWN.layer(),
                    HaloModel::createBodyLayer
            );
        });


        registrars.menuScreens(screens -> {
            // Use the BalmMenuTypeRegistration as the holder
            screens.register(ModMenuTypes.SOULFORGE_MENU, SoulForgeScreen::new);
            screens.register(ModMenuTypes.RUNE_CUTTER_MENU, ElementalRuneCutterScreen::new);
            screens.register(ModMenuTypes.ELEMENT_CHOOSER_MENU, ElementChooserScreen::new);

        });


        registrars.particleProviders((registrar) -> {
            registrar.register(ModParticles.SORCERY_PARTICLE.asHolder(), SorceryParticle.Provider::new);
            registrar.register(ModParticles.FROST_PARTICLE.asHolder(), FrostParticle.Provider::new);
            registrar.register(ModParticles.STORM_SPARK.asHolder(), StormSpark.Provider::new);



        });


        registrars.blockColors(colors -> {
            // --- KINGDOM ANCHOR ---
            colors.register(
                    (state, level, pos, tintIndex) -> {
                        if (state.hasProperty(ELEMENT)) {
                            int element = state.getValue(ELEMENT);
                            return ElementRegistry.getColor(element);
                        }
                        return 0xFFFFFF; // Default white if property is missing
                    },
                    ModBlocks.KINGDOM_ANCHOR
            );

            // --- KINGDOM CORE ---
            colors.register(
                    (state, level, pos, tintIndex) -> {
                        if (state.hasProperty(ELEMENTCore)) {
                            int element = state.getValue(ELEMENTCore);
                            return ElementRegistry.getColor(element);
                        }
                        return 0xFFFFFF;
                    },
                    ModBlocks.KINGDOM_CORE
            );

            /*
            // --- ELEMENTAL ALTAR (Rainbow) ---
            colors.register(
                    (state, level, pos, tintIndex) -> {
                        // This will update the block's color every frame to match your HUD
                        return UtilityHelper.getRainbowColor(10000);
                    },
                    ModBlocks.ELEMENTAL_ALTAR
            );*/
        });


        registrars.rangeSelectItemModelProperties(properties -> {
            // "theelemental:element_id" matches the "property" key in your JSON
            properties.register(
                    id("element_id"),
                    ElementIdProperty.MAP_CODEC
            );

            // "theelemental:spell_variant" matches the nested property in your JSON
            properties.register(
                    id("spell_variant"),
                    SpellVariantProperty.MAP_CODEC
            );
        });

        RenderCallback.Gui.AFTER.register((guiGraphics, window) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                // to put or not display here
                if (ClientElementalData.get().getElement() > 0) {
                    SlotHUD.render(guiGraphics);
                    ManaHUD.render(guiGraphics);
                }
                KingdomCoreFaceHUD.render(guiGraphics);
                BlockInfoHUD.render(guiGraphics);
            }
        });



    }

}
