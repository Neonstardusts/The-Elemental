package com.teamneon.theelemental.client;


import com.mojang.serialization.MapCodec;
import com.teamneon.theelemental.client.tooltip.ModTooltips;
import com.teamneon.theelemental.item.property.ElementIdProperty;
import com.teamneon.theelemental.item.property.SpellVariantProperty;
import com.teamneon.theelemental.menu.ModMenuTypes;
import com.teamneon.theelemental.menu.SoulForgeScreen;
import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.blay09.mods.balm.client.platform.event.callback.RenderCallback;
import net.blay09.mods.balm.client.platform.event.callback.ScreenCallback;
import net.minecraft.client.Minecraft;

import static com.teamneon.theelemental.Theelemental.id;


public class TheelementalClient {

    public static void initialize(BalmClientRegistrars registrars) {
        ModKeyMappings.initialize();

        registrars.blockEntityRenderers(ModRenderers::initialize);
        registrars.clientTooltipComponents(ModTooltips::initialize);

        registrars.menuScreens(screens -> {
            // Use the BalmMenuTypeRegistration as the holder
            screens.register(ModMenuTypes.SOULFORGE_MENU, SoulForgeScreen::new);
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
                if (ClientElementalData.get().getElement() > 0) {
                    SlotHUD.render(guiGraphics);
                    ManaHUD.render(guiGraphics);
                }
            }
        });


    }

}
