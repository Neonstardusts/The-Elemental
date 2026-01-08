package com.teamneon.theelemental.menu;

import net.blay09.mods.balm.world.BalmMenuFactory;
import net.blay09.mods.balm.world.inventory.BalmMenuTypeRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {

    /* ------------------------
       SOUL FORGE
       ------------------------ */
    public record SoulForgeData() {
        public static final StreamCodec<RegistryFriendlyByteBuf, SoulForgeData> STREAM_CODEC =
                StreamCodec.unit(new SoulForgeData());
    }

    public static Holder<MenuType<SoulForgeMenu>> SOULFORGE_MENU;

    /* ------------------------
       ELEMENTAL RUNE CUTTER
       ------------------------ */
    public record RuneCutterData(int element) {
        public static final StreamCodec<RegistryFriendlyByteBuf, RuneCutterData> STREAM_CODEC = StreamCodec.of(
                // 1. ENCODER (Value, Buffer)
                (buf, data) -> buf.writeInt(data.element()),
                // 2. DECODER (Buffer)
                buf -> new RuneCutterData(buf.readInt())
        );
    }



    public static Holder<MenuType<ElementalRuneCutterMenu>> RUNE_CUTTER_MENU;

    public static void initialize(BalmMenuTypeRegistrar menus) {
        // SoulForge
        SOULFORGE_MENU = menus.register("soulforge_menu", new BalmMenuFactory<SoulForgeMenu, SoulForgeData>() {
            @Override
            public SoulForgeMenu create(int windowId, Inventory inventory, SoulForgeData data) {
                return new SoulForgeMenu(windowId, inventory, inventory.player);
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, SoulForgeData> getStreamCodec() {
                return SoulForgeData.STREAM_CODEC;
            }
        }).asHolder();

        // Rune Cutter
        RUNE_CUTTER_MENU = menus.register("rune_cutter_menu", new BalmMenuFactory<ElementalRuneCutterMenu, RuneCutterData>() {
            @Override
            public ElementalRuneCutterMenu create(int windowId, Inventory inventory, RuneCutterData data) {
                // Use the element sent from the server
                return new ElementalRuneCutterMenu(windowId, inventory, ContainerLevelAccess.NULL, data.element());
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, RuneCutterData> getStreamCodec() {
                return RuneCutterData.STREAM_CODEC;
            }
        }).asHolder();
    }
}
