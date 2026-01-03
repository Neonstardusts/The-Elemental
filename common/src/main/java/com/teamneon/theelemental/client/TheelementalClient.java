package com.teamneon.theelemental.client;

import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.blay09.mods.balm.client.platform.event.callback.RenderCallback;
import net.blay09.mods.balm.client.platform.event.callback.ScreenCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;


public class TheelementalClient {

    public static void initialize(BalmClientRegistrars registrars) {
        ModKeyMappings.initialize();

        registrars.blockEntityRenderers(ModRenderers::initialize);

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
