package com.teamneon.theelemental.api;

import com.teamneon.theelemental.Theelemental;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static SoundEvent ELECTRIC_DAMAGE;

    public static void initialize(BalmRegistrar.Scoped<SoundEvent> registrar) {
        // 1. We still use the full Identifier here to define the SoundEvent itself
        ELECTRIC_DAMAGE = SoundEvent.createVariableRangeEvent(Theelemental.id("electric_damage"));

        // 2. FIX: Pass only the String "electric_damage".
        // Balm automatically turns this into "theelemental:electric_damage"
        registrar.register("electric_damage", (id) -> ELECTRIC_DAMAGE);    }
}