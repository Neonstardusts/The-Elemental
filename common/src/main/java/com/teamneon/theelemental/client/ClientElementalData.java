package com.teamneon.theelemental.client;

import com.teamneon.theelemental.data.ElementalData;
import net.minecraft.client.Minecraft;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientElementalData {
    // Stores the full data (mana, level, etc.) for the LOCAL player
    private static final ElementalData CLIENT_DATA = new ElementalData();
    private static int currentSlot = 0; // Currently selected slot index (0–7)

    // Stores the Element ID for EVERY entity in render distance to fix the "copy-cat" wing bug
    private static final Map<Integer, Integer> ENTITY_ELEMENTS = new ConcurrentHashMap<>();

    // Tracks spell cooldowns locally for HUD rendering
    private static final Map<Integer, Long> CLIENT_COOLDOWNS = new ConcurrentHashMap<>();

    /**
     * Called by SyncEntityElementPacket to store which player has what element.
     */
    public static void setEntityElement(int entityId, int elementId) {
        ENTITY_ELEMENTS.put(entityId, elementId);
    }

    /**
     * Used by the Wing/Cosmetic Renderer to find the element for a specific entity ID.
     */
    public static int getElementForEntity(int entityId) {
        // If it's the local player, return our primary data
        if (Minecraft.getInstance().player != null && entityId == Minecraft.getInstance().player.getId()) {
            return CLIENT_DATA.getElement();
        }
        // Otherwise, return the specific element mapped to this entity ID
        return ENTITY_ELEMENTS.getOrDefault(entityId, 0);
    }

    public static ElementalData get() {
        return CLIENT_DATA;
    }

    public static int getCurrentSlot() {
        return currentSlot;
    }

    public static void setCurrentSlot(int slot) {
        if (slot >= 0 && slot < 8) {
            currentSlot = slot;
        }
    }

    /**
     * Returns a list of all slot indices that are unlocked (>=0)
     */
    public static List<Integer> getUnlockedSlotIndices() {
        List<Integer> unlocked = CLIENT_DATA.getActiveSlots();
        return unlocked.stream()
                .filter(slot -> slot >= 0)
                .toList();
    }

    /**
     * Cycle to the next unlocked spell slot.
     * Fixed to stay within the 0-7 index range.
     */
    public static void nextSlot() {
        List<Integer> activeSlots = CLIENT_DATA.getActiveSlots();
        if (activeSlots.isEmpty()) return;

        int next = currentSlot;
        // We strictly search within 8 slots to prevent index jumps
        int maxSlots = Math.min(activeSlots.size(), 8);

        for (int i = 1; i <= maxSlots; i++) {
            int candidateIndex = (currentSlot + i) % maxSlots;

            // Check if the slot at this index is unlocked (not -1)
            if (activeSlots.get(candidateIndex) >= 0) {
                next = candidateIndex;
                break;
            }
        }
        currentSlot = next;
    }

    /**
     * Updates the local player's specific elemental data (from SyncElementalDataPacket)
     */
    public static void update(float currentMana, int level, int element,
                              List<Integer> unlockedSpells, List<Integer> activeSlots) {
        CLIENT_DATA.setCurrentMana(currentMana);
        CLIENT_DATA.setLevel(level);
        CLIENT_DATA.setElement(element);
        CLIENT_DATA.setUnlockedSpells(unlockedSpells);
        CLIENT_DATA.setActiveSlots(activeSlots);

        // Safety: Ensure currentSlot is not out of bounds of the new list
        if (currentSlot >= activeSlots.size()) {
            currentSlot = 0;
        }

        // Auto-move if the current slot was locked (-1)
        if (activeSlots.get(currentSlot) == -1) {
            nextSlot();
        }
    }

    /**
     * Sets a local cooldown for HUD visuals.
     */
    public static void setLocalCooldown(int spellId, int durationTicks, long currentTime) {
        CLIENT_COOLDOWNS.put(spellId, currentTime + durationTicks);
    }

    /**
     * Checks if a spell icon should be grayed out in the HUD.
     */
    public static boolean isSpellOnCooldown(int spellId) {
        if (Minecraft.getInstance().level == null) return false;
        long currentTime = Minecraft.getInstance().level.getGameTime();
        Long expiry = CLIENT_COOLDOWNS.get(spellId);
        return expiry != null && currentTime < expiry;
    }

    /**
     * Returns the remaining ticks for a cooldown.
     */
    public static long getRemainingCooldownTicks(int spellId) {
        if (Minecraft.getInstance().level == null) return 0;
        long currentTime = Minecraft.getInstance().level.getGameTime();
        Long expiry = CLIENT_COOLDOWNS.get(spellId);
        if (expiry == null) return 0;
        return Math.max(0, expiry - currentTime);
    }
}