package com.teamneon.theelemental.store;

import com.teamneon.theelemental.store.RuneData;
import net.blay09.mods.balm.core.component.BalmDataComponentTypeRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;

public class ModComponents {

    public static Holder<DataComponentType<RuneData>> rune;
    public static Holder<DataComponentType<ManaData>> mana_storage;

    public static void initialize(BalmDataComponentTypeRegistrar components) {
        // This mirrors your Waystones example
        rune = components.register("rune_data", RuneData.CODEC).asHolder();
        mana_storage = components.register("mana_storage", ManaData.CODEC).asHolder();
    }
}