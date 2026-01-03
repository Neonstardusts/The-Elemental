package com.teamneon.theelemental.neoforge;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import com.teamneon.theelemental.Theelemental;

@Mod(Theelemental.MOD_ID)
public class NeoForgeTheelemental {

    public NeoForgeTheelemental(IEventBus modEventBus) {
        final var context = new NeoForgeLoadContext(modEventBus);
        Balm.initializeMod(Theelemental.MOD_ID, context, Theelemental::initialize);
    }
}
