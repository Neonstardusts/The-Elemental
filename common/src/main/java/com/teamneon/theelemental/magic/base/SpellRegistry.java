package com.teamneon.theelemental.magic.base;

import com.teamneon.theelemental.magic.spells.*;

import java.util.HashMap;
import java.util.Map;

public class SpellRegistry {
    // Maps the ID (1, 2, 3...) to the Spell implementation
    private static final Map<Integer, Spell> SPELLS = new HashMap<>();

    // A fallback spell for ID 0 or invalid IDs
    private static final Spell EMPTY = new Spell() {
        @Override public int getManaCost() { return 0; }
        @Override public int getCooldownTicks() { return 0; }
        @Override public SpellCastResult execute(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player) { return SpellCastResult.fail(); }
    };

    /**
     * Call this in your Mod Initializer / Common Setup
     */
    public static void init() {
        // ID 0 is nothing
        register(0, EMPTY);
        register(1, new FireballSpell());
        register(8001, new BlinkSpell());
        register(6001, new TorrentSpell());
        register(3001, new PoisonSpraySpell());
    }

    public static void register(int id, Spell spell) {
        SPELLS.put(id, spell);
    }

    public static Spell getSpell(int id) {
        return SPELLS.getOrDefault(id, EMPTY);
    }
}