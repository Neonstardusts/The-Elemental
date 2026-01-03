package com.teamneon.theelemental.helpers;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.HashMap;
import java.util.Map;

public final class ElementRegistry {

    // Record to hold all data related to an element
    public record ElementInfo(int id, String name, String race, String message, int color) {}

    private static final Map<Integer, ElementInfo> BY_ID = new HashMap<>();
    private static final Map<Item, ElementInfo> BY_ITEM = new HashMap<>();

    static {
        // Register elements with a color (0xRRGGBB)
        register(Items.FIRE_CHARGE, 1, "Fire", "Fae", "The flames awaken in you!", 0xFF4500);       // Orange-red
        register(Items.GLOW_BERRIES, 2, "Light", "Wisp", "A gentle glow surrounds you!", 0xFFFF00); // Yellow
        register(Items.EMERALD, 3, "Sorcery", "Sprite", "The arcane flows through your veins!", 0x00FFCC); // Teal
        register(Items.SPORE_BLOSSOM, 4, "Earth", "Ent", "The earth embraces your spirit!", 0x228B22); // Forest green
        register(Items.GUNPOWDER, 5, "Storm", "Mote", "Lightning dances in your hands!", 0x1E90FF);  // Dodger blue
        register(Items.NAUTILUS_SHELL, 6, "Ocean", "Siren", "The waves answer to your call!", 0x4682B4); // Steel blue
        register(Items.ECHO_SHARD, 7, "Dark", "Spectre", "Shadows whisper your name!", 0x4B0082);     // Indigo
        register(Items.ENDER_PEARL, 8, "Warped", "Sentinel", "The warped energies empower you!", 0x8A2BE2); // Blue violet
        register(Items.WIND_CHARGE, 9, "Wind", "Zephyr", "The wind guides your path!", 0x87CEEB); // Sky blue
    }

    private static void register(Item item, int id, String name, String race, String message, int color) {
        ElementInfo info = new ElementInfo(id, name, race, message, color);
        BY_ID.put(id, info);
        BY_ITEM.put(item, info);
    }

    // --- Utility Methods ---

    public static String getName(int id) {
        ElementInfo info = BY_ID.get(id);
        return info != null ? info.name() : "Unknown";
    }

    public static String getRace(int id) {
        ElementInfo info = BY_ID.get(id);
        return info != null ? info.race() : "None";
    }

    public static String getMessage(int id) {
        ElementInfo info = BY_ID.get(id);
        return info != null ? info.message() : "";
    }

    public static int getIdFromItem(Item item) {
        ElementInfo info = BY_ITEM.get(item);
        return info != null ? info.id() : 0;
    }

    /** Get the color for an element ID (default white if not found) */
    public static int getColor(int id) {
        ElementInfo info = BY_ID.get(id);
        return info != null ? info.color() : 0xFFFFFF;
    }

    private ElementRegistry() { throw new UnsupportedOperationException(); }
}
