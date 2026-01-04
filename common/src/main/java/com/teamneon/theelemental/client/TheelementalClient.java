package com.teamneon.theelemental.client;


import com.teamneon.theelemental.client.tooltip.ModTooltips;
import com.teamneon.theelemental.client.tooltip.RuneRecipeTooltipComponent;
import com.teamneon.theelemental.menu.ModMenuTypes;
import com.teamneon.theelemental.menu.SoulForgeScreen;
import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.blay09.mods.balm.client.platform.event.callback.RenderCallback;
import net.blay09.mods.balm.client.platform.event.callback.ScreenCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.stream.Collectors;


public class TheelementalClient {

    public static void initialize(BalmClientRegistrars registrars) {
        ModKeyMappings.initialize();

        registrars.blockEntityRenderers(ModRenderers::initialize);
        registrars.clientTooltipComponents(ModTooltips::initialize);

        registrars.menuScreens(screens -> {
            // Use the BalmMenuTypeRegistration as the holder
            screens.register(ModMenuTypes.SOULFORGE_MENU, SoulForgeScreen::new);
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
