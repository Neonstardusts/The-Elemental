package com.teamneon.theelemental;

import com.teamneon.theelemental.block.entity.ModBlockEntities;
import com.teamneon.theelemental.client.ModRenderers;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.events.ElementalEvents;
import com.teamneon.theelemental.magic.base.SpellRegistry;
import com.teamneon.theelemental.menu.ModMenuTypes;
import com.teamneon.theelemental.network.ModNetworking;
import com.teamneon.theelemental.network.SyncElementalDataPacket;
import com.teamneon.theelemental.store.ModComponents;
import com.teamneon.theelemental.store.RuneData;
import com.teamneon.theelemental.worldcrafter.ModRecipes;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.blay09.mods.balm.core.BalmRegistrars;
import net.blay09.mods.balm.core.component.BalmDataComponentTypeRegistrar;
import net.blay09.mods.balm.network.BalmNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teamneon.theelemental.block.ModBlocks;
import com.teamneon.theelemental.item.ModItems;


public class Theelemental {

    public static final Logger logger = LoggerFactory.getLogger(Theelemental.class);
    public static final String MOD_ID = "theelemental";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void initialize(BalmRegistrars registrars) {
        Balm.config().registerConfig(TheelementalConfig.class);
        // Connects the event signal to your method (The Bridge)
        ElementalEvents.register();

        // ---- REGISTRIES ----
        registrars.dataComponentTypes(ModComponents::initialize);

        registrars.blocks(ModBlocks::initialize);
        registrars.items(ModItems::initialize);
        registrars.creativeModeTabs(ModItems::initialize);
        registrars.blockEntityTypes(ModBlockEntities::initialize);
        registrars.menuTypes(ModMenuTypes::initialize);

        SpellRegistry.init();
        ModRecipes.register();
        // Use the registrar to handle networking
        // If registrars.networking exists, use it. Otherwise, use Balm.networking()
        ModNetworking.initialize(Balm.networking());
        logger.info("The Elemental is initializing!");

    }



}
