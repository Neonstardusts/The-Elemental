package com.teamneon.theelemental.data;

import com.teamneon.theelemental.network.SyncElementalDataPacket;
import com.teamneon.theelemental.store.PersistentElementStore;
import com.teamneon.theelemental.Theelemental; // Import your main mod class for logging/cleanup hook
import net.blay09.mods.balm.Balm;
import net.minecraft.world.entity.player.Player;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ElementalDataHandler {

    // This is the true "In-Memory" Store, mapping player UUID to their live data
    private static final Map<UUID, ElementalData> LIVE_PLAYER_DATA = new ConcurrentHashMap<>();

    // --- FIX 1: Cleanup method for world unload ---
    /**
     * Called when the server stops (e.g., quitting a single-player world).
     * Wipes the entire static cache to prevent data leaking into the next world.
     */
    public static void cleanup() {
        Theelemental.logger.info("Clearing Elemental Data memory cache.");
        LIVE_PLAYER_DATA.clear();
    }

    // --- FIX 2: Simplification of get() ---
    /**
     * Retrieves the live, in-memory data instance for a player.
     * Note: This assumes data was placed here by load() or save().
     */
    public static ElementalData get(Player player) {
        // We only use this to retrieve an object we know is active.
        // We rely on 'load' to put the correct object in first.
        return LIVE_PLAYER_DATA.computeIfAbsent(player.getUUID(), (uuid) -> {
            Theelemental.logger.warn("Requesting ElementalData for player not in cache. Initializing default data.");
            return new ElementalData();
        });
    }

    /**
     * Called on Player Login: Loads persistent NBT data into memory (NBT -> Memory)
     */
    // Inside ElementalDataHandler.java

    public static void load(Player player) {
        // 1. Wipe old memory
        LIVE_PLAYER_DATA.remove(player.getUUID());

        // 2. Load from NBT (Clean Slate Logic)
        ElementalData data = new ElementalData();
        data.setLevel(PersistentElementStore.getLevel(player));
        data.setCurrentMana(PersistentElementStore.getCurrentMana(player));
        data.setManaRegen(PersistentElementStore.getManaRegen(player));

        // --- NEW DATA LOADING ---
        data.setElement(PersistentElementStore.getElement(player));

        // Lists are loaded into the existing live lists
        // Use addAll to move the items from the NBT list into the RAM list
        data.getUnlockedSpells().clear();
        data.getUnlockedSpells().addAll(PersistentElementStore.getUnlockedSpells(player));

        // Clear the default slots and add the ones from NBT
        data.getActiveSlots().clear();
        data.getActiveSlots().addAll(PersistentElementStore.getActiveSlots(player));

        // 3. Store in RAM
        LIVE_PLAYER_DATA.put(player.getUUID(), data);

        // 4. THE CRITICAL STEP: Sync to Client
        // This tells the player's screen "Hey, you are actually Level 0 now!"
        syncToClient(player);

        Theelemental.logger.info("Loaded and Synced data for {}: Level {}", player.getName().getString(), data.getLevel());
    }

    public static void save(Player player) {
        // Use the MAP directly to avoid any "get()" side effects
        ElementalData data = LIVE_PLAYER_DATA.get(player.getUUID());
        if (data == null) return; // Nothing to save

        // Push memory -> NBT
        PersistentElementStore.setLevel(player, data.getLevel());
        PersistentElementStore.setCurrentMana(player, (int) data.getCurrentMana());
        // --- NEW DATA SAVING ---
        PersistentElementStore.setElement(player, data.getElement());

        // Lists are saved from the live lists
        PersistentElementStore.setUnlockedSpells(player, data.getUnlockedSpells());
        PersistentElementStore.setActiveSlots(player, data.getActiveSlots());

        Theelemental.logger.info("SAVE SUCCESS: {} saved at Level {}", player.getName().getString(), data.getLevel());
    }

    public static void syncToClient(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ElementalData data = get(player);
            Balm.networking().sendTo(serverPlayer, new SyncElementalDataPacket(
                    data.getCurrentMana(),
                    data.getLevel(),
                    data.getElement(),
                    data.getUnlockedSpells(),
                    data.getActiveSlots()
            ));
        }
    }
}