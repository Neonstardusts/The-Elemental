package com.teamneon.theelemental.menu;

import net.blay09.mods.balm.world.BalmMenuFactory;
import net.blay09.mods.balm.world.inventory.BalmMenuTypeRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {

    // 1. Define the Data record
    public record SoulForgeData() {
        public static final StreamCodec<RegistryFriendlyByteBuf, SoulForgeData> STREAM_CODEC =
                StreamCodec.unit(new SoulForgeData());
    }

    // 2. Use Holder (matching Waystones)
    public static Holder<MenuType<SoulForgeMenu>> SOULFORGE_MENU;

    public static void initialize(BalmMenuTypeRegistrar menus) {
        SOULFORGE_MENU = menus.register("soulforge_menu", new BalmMenuFactory<SoulForgeMenu, SoulForgeData>() {
            @Override
            public SoulForgeMenu create(int windowId, Inventory inventory, SoulForgeData data) {
                // This now resolves because the parameter is explicitly 'Inventory'
                return new SoulForgeMenu(windowId, inventory, inventory.player);
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, SoulForgeData> getStreamCodec() {
                return SoulForgeData.STREAM_CODEC;
            }
        }).asHolder();
    }
}