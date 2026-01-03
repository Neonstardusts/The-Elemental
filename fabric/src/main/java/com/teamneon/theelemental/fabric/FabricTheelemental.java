package com.teamneon.theelemental.fabric;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.fabric.platform.runtime.FabricLoadContext;
import net.fabricmc.api.ModInitializer;
import com.teamneon.theelemental.Theelemental;

public class FabricTheelemental implements ModInitializer {
    @Override
    public void onInitialize() {
        Balm.initializeMod(Theelemental.MOD_ID, FabricLoadContext.INSTANCE, Theelemental::initialize);
    }
}
