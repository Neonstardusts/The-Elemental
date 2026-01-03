package com.teamneon.theelemental.neoforge.client;

import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import com.teamneon.theelemental.Theelemental;
import com.teamneon.theelemental.client.TheelementalClient;

@Mod(value = Theelemental.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeTheelementalClient {

    public NeoForgeTheelementalClient(IEventBus modEventBus) {
        final var context = new NeoForgeLoadContext(modEventBus);
        BalmClient.initializeMod(Theelemental.MOD_ID, context, TheelementalClient::initialize);
    }
}
