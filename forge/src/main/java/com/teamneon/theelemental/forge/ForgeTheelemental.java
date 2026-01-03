package com.teamneon.theelemental.forge;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.client.BalmClient;
import net.blay09.mods.balm.forge.ForgeLoadContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import com.teamneon.theelemental.Theelemental;

@Mod(Theelemental.MOD_ID)
public class ForgeTheelemental {

    public ForgeTheelemental(FMLJavaModLoadingContext context) {
        final var loadContext = new ForgeLoadContext(context.getModBusGroup());
        Balm.initializeMod(Theelemental.MOD_ID, loadContext, Theelemental::initialize);
        if (FMLEnvironment.dist.isClient()) {
            BalmClient.initializeMod(Theelemental.MOD_ID, loadContext, TheelementalClient::initialize);
        }
    }

}
