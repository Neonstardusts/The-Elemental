package com.teamneon.theelemental.client;


import com.mojang.serialization.MapCodec;
import com.teamneon.theelemental.block.ModBlocks;
import com.teamneon.theelemental.client.tooltip.ModTooltips;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.helpers.UtilityHelper;
import com.teamneon.theelemental.item.property.ElementIdProperty;
import com.teamneon.theelemental.item.property.SpellVariantProperty;
import com.teamneon.theelemental.menu.ElementChooserScreen;
import com.teamneon.theelemental.menu.ElementalRuneCutterScreen;
import com.teamneon.theelemental.menu.ModMenuTypes;
import com.teamneon.theelemental.menu.SoulForgeScreen;
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



        registrars.menuScreens(screens -> {
            // Use the BalmMenuTypeRegistration as the holder
            screens.register(ModMenuTypes.SOULFORGE_MENU, SoulForgeScreen::new);
            screens.register(ModMenuTypes.RUNE_CUTTER_MENU, ElementalRuneCutterScreen::new);
            screens.register(ModMenuTypes.ELEMENT_CHOOSER_MENU, ElementChooserScreen::new);

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
