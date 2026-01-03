package com.teamneon.theelemental.fabric.client;

import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.fabric.platform.runtime.FabricLoadContext;
import net.fabricmc.api.ClientModInitializer;
import com.teamneon.theelemental.Theelemental;
import com.teamneon.theelemental.client.TheelementalClient;

public class FabricTheelementalClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BalmClient.initializeMod(Theelemental.MOD_ID, FabricLoadContext.INSTANCE, TheelementalClient::initialize);
    }
}
