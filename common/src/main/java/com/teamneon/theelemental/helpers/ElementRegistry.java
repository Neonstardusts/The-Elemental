package com.teamneon.theelemental.helpers;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ElementRegistry {

    // Record to hold all data related to an element
    public record ElementInfo(
            int id,
            String name,
            String race,
            String message,
            int color,
            List<String> passives,
            List<String> kingdom_effect,
            List<String> restrictions
    ) {}

    private static final Map<Integer, ElementInfo> BY_ID = new HashMap<>();
    private static final Map<Item, ElementInfo> BY_ITEM = new HashMap<>();

    static {
        // 1. FIRE
        register(Items.FIRE_CHARGE, 1, "Fire", "Fae", "The flames awaken in you!", 0xf04b22,
                List.of("Internal Flame: Immune to fire and lava"),
                List.of("Burning Speed: Flames quicken your step"),
                List.of("Extinguished: Weakened and slowed by water"));

        // 2. LIGHT
        register(Items.GLOW_BERRIES, 2, "Light", "Wisp", "A gentle glow surrounds you!", 0xfabe28,
                // Passive: Faster regeneration
                List.of("Radiance: Natural healing is accelerated"),
                // Kingdom: Spawn Light Blocks in dark spots
                List.of("Luminescence: Banishes shadows with spectral light"),
                // Restriction: Weakness in light level <3
                List.of("Faded: Power withers in the deep dark"));

        // 3. SORCERY
        register(Items.EMERALD, 3, "Sorcery", "Sprite", "The arcane flows through your veins!", 0xb4fa28,
                // Passive: Chance to gain double xp
                List.of("Arcane Intellect: Chance to harvest double experience"),
                // Kingdom: Mobs drop xp on hit
                List.of("Mana Siphon: Striking foes extracts raw magic"),
                List.of("No Restrictions"));

        // 4. EARTH
        register(Items.SPORE_BLOSSOM, 4, "Earth", "Ent", "The earth embraces your spirit!", 0x40bf3b,
                // Passive: Regeneration when under leaves
                List.of("Forest Guard: Leaves grant you mending life"),
                // Kingdom: Growth tick for crops
                List.of("Bloom: Nearby crops flourish instantly"),
                // Restriction: Weaker outside overworld
                List.of("Uprooted: Your strength fails outside the Overworld"));

        // 5. STORM
        register(Items.GUNPOWDER, 5, "Storm", "Mote", "Lightning dances in your hands!", 0x62e3d0,
                // Passive: naturally quicker
                List.of("Galvanized: Your body moves like a bolt"),
                // Kingdom: Strike hostile mobs with lightning
                List.of("Thunderstruck: Nature strikes your enemies down"),
                // Restriction: Weakness underground
                List.of("Insulated: The crushing depths dampen your spark"));

        // 6. WATER
        register(Items.NAUTILUS_SHELL, 6, "Ocean", "Siren", "The waves answer to your call!", 0x1c87eb,
                // Passive: Water breathing
                List.of("Gills: The ocean is your atmosphere"),
                // Kingdom: Kingdom wide conduit
                List.of("Conduit Core: The sea protects your territory"),
                // Restriction: Painful fire damage
                List.of("Evaporation: Fire is exceptionally lethal to you"));

        // 7. DARK
        register(Items.ECHO_SHARD, 7, "Dark", "Spectre", "Shadows whisper your name!", 0x5046c2,
                // Passive: Invisible in <3 light
                List.of("Cloak of Night: Become unseen in total darkness"),
                // Kingdom: Regen in the dark
                List.of("Shadow Mend: Darkness fuels your recovery"),
                // Restriction: Weakness in >7 light
                List.of("Photosensitivity: Bright lights sap your strength"));

        // 8. WARPED
        register(Items.ENDER_PEARL, 8, "Warped", "Sentinel", "The warped energies empower you!", 0x8e3bed,
                // Passive: No enderpearl damage
                List.of("Stability: Ender pearls no longer harm you"),
                // Kingdom: Teleport mobs away
                List.of("Spatial Rift: Trespassers are blinked away"),
                // Restriction: Strength and Jump boost in the End (Note: this is a positive restriction, basically 'End Bound')
                List.of("End-Linked: Peak power is only achieved in the End"));

        // 9. WIND
        register(Items.WIND_CHARGE, 9, "Wind", "Zephyr", "The wind guides your path!", 0xf285f2,
                // Passive: Slowfalling + particles
                List.of("Airstride: Gravity loses its grip on you"),
                // Kingdom: Speed, Jump, Slowfall
                List.of("Cloud-Step: Movement is effortless and light"),
                List.of("No Restrictions"));
    }

    private static void register(Item item, int id, String name, String race, String message, int color, List<String> passives,List<String> kingdom_effect ,List<String> restrictions) {
        ElementInfo info = new ElementInfo(id, name, race, message, color, passives, kingdom_effect, restrictions);
        BY_ID.put(id, info);
        BY_ITEM.put(item, info);
    }

    // --- Utility Methods ---

    public static List<String> getPassives(int id) {
        ElementInfo info = BY_ID.get(id);
        return info != null ? info.passives() : List.of();
    }

    public static List<String> getKingdomEffect(int id) {
        ElementInfo info = BY_ID.get(id);
        return info != null ? info.kingdom_effect() : List.of();
    }

    public static List<String> getRestrictions(int id) {
        ElementInfo info = BY_ID.get(id);
        return info != null ? info.restrictions() : List.of();
    }

    public static List<Integer> getAllIds() {
        return BY_ID.keySet().stream().sorted().toList();
    }


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
