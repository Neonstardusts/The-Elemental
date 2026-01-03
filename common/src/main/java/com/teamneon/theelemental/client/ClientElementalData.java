package com.teamneon.theelemental.client;

import com.teamneon.theelemental.data.ElementalData;
import net.minecraft.client.Minecraft;

import java.util.List;

public class ClientElementalData {
    private static final ElementalData CLIENT_DATA = new ElementalData();
    private static int currentSlot = 0; // Currently selected slot index (0–7)

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
                .filter(slot -> slot >= 0) // Only unlocked slots
                .toList();
    }

    /**
     * Cycle to the next unlocked slot
     */
    public static void nextSlot() {
        List<Integer> activeSlots = CLIENT_DATA.getActiveSlots();
        int next = currentSlot;

        // Look for the next unlocked slot
        for (int i = 1; i <= activeSlots.size(); i++) {
            int candidate = (currentSlot + i) % activeSlots.size();
            if (activeSlots.get(candidate) >= 0) {
                next = candidate;
                break;
            }
        }

        currentSlot = next;
    }

    public static void update(float currentMana, int level, int element,
                              List<Integer> unlockedSpells, List<Integer> activeSlots) {
        CLIENT_DATA.setCurrentMana(currentMana);
        CLIENT_DATA.setLevel(level);
        CLIENT_DATA.setElement(element);
        CLIENT_DATA.setUnlockedSpells(unlockedSpells);
        CLIENT_DATA.setActiveSlots(activeSlots);

        // Optional: if currentSlot is now on a locked slot, move to first unlocked
        if (CLIENT_DATA.getActiveSlots().get(currentSlot) == -1) {
            nextSlot();
        }
    }

    // Add to ClientElementalData.java
    private static final java.util.Map<Integer, Long> CLIENT_COOLDOWNS = new java.util.HashMap<>();

    /**
     * Call this when the player successfully casts a spell locally.
     * currentTime should be Minecraft.getInstance().level.getGameTime()
     */
    public static void setLocalCooldown(int spellId, int durationTicks, long currentTime) {
        CLIENT_COOLDOWNS.put(spellId, currentTime + durationTicks);
    }

    /**
     * Use this in your HUD rendering code to see if a spell icon should be grayed out.
     */
    public static boolean isSpellOnCooldown(int spellId) {
        if (net.minecraft.client.Minecraft.getInstance().level == null) return false;
        long currentTime = net.minecraft.client.Minecraft.getInstance().level.getGameTime();
        Long expiry = CLIENT_COOLDOWNS.get(spellId);
        return expiry != null && currentTime < expiry;
    }

    /**
     * Returns a float from 0.0 to 1.0 representing the remaining cooldown percentage.
     * Useful for "winding down" overlays on icons.
     */

    // Add to ClientElementalData.java
    public static long getRemainingCooldownTicks(int spellId) {
        if (net.minecraft.client.Minecraft.getInstance().level == null) return 0;
        long currentTime = net.minecraft.client.Minecraft.getInstance().level.getGameTime();
        Long expiry = CLIENT_COOLDOWNS.get(spellId);
        if (expiry == null) return 0;
        return Math.max(0, expiry - currentTime);
    }
}

