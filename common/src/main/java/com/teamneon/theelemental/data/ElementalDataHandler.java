package com.teamneon.theelemental.data;

import com.teamneon.theelemental.network.SyncElementalDataPacket;
import com.teamneon.theelemental.network.SyncEntityElementPacket;
import com.teamneon.theelemental.store.PersistentElementStore;
import com.teamneon.theelemental.Theelemental;
import net.blay09.mods.balm.Balm;
import net.minecraft.world.entity.player.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ElementalDataHandler {

    private static final Map<UUID, ElementalData> LIVE_PLAYER_DATA = new ConcurrentHashMap<>();

    public static void cleanup() {
        Theelemental.logger.info("Clearing Elemental Data memory cache.");
        LIVE_PLAYER_DATA.clear();
    }

    public static ElementalData get(Player player) {
        return LIVE_PLAYER_DATA.computeIfAbsent(player.getUUID(), (uuid) -> new ElementalData());
    }

    public static void load(Player player) {
        LIVE_PLAYER_DATA.remove(player.getUUID());

        ElementalData data = new ElementalData();
        data.setLevel(PersistentElementStore.getLevel(player));
        data.setCurrentMana(PersistentElementStore.getCurrentMana(player));
        data.setManaRegen(PersistentElementStore.getManaRegen(player));
        data.setElement(PersistentElementStore.getElement(player));

        // Use the setters that clear the list first!
        data.setUnlockedSpells(PersistentElementStore.getUnlockedSpells(player));
        data.setActiveSlots(PersistentElementStore.getActiveSlots(player));

        LIVE_PLAYER_DATA.put(player.getUUID(), data);

        syncToClient(player);
        broadcastElement(player);

        Theelemental.logger.info("Loaded and Synced data for {}.", player.getName().getString());
    }

    public static void save(Player player) {
        ElementalData data = LIVE_PLAYER_DATA.get(player.getUUID());
        if (data == null) return;

        PersistentElementStore.setLevel(player, data.getLevel());
        PersistentElementStore.setCurrentMana(player, (int) data.getCurrentMana());
        PersistentElementStore.setElement(player, data.getElement());
        PersistentElementStore.setUnlockedSpells(player, data.getUnlockedSpells());
        PersistentElementStore.setActiveSlots(player, data.getActiveSlots());
    }

    /**
     * Syncs full private data (Mana, Spells) to the owner ONLY.
     */
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

    /**
     * Syncs minimal public data (EntityID + ElementID) to everyone in range.
     */
    public static void broadcastElement(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            int element = get(player).getElement();
            SyncEntityElementPacket packet = new SyncEntityElementPacket(player.getId(), element);

            // Tell all nearby players
            Balm.networking().sendToTracking(serverPlayer, packet);
            // Tell the player themselves so their client-side entity map is updated
            Balm.networking().sendTo(serverPlayer, packet);
        }
    }
}