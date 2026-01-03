package com.teamneon.theelemental.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ElementalData {

    // --- Live Variables ---
    private int element = 0;
    private int level = 0;
    private float currentMana = 0;
    private float manaRegen = 1.0f;
    private final List<Integer> unlockedSpells = new ArrayList<>();
    // Default slots: [0, 0, 0, -1, -1, -1, -1, -1]
    private final List<Integer> activeSlots = new ArrayList<>(Arrays.asList(0, 0, 0, -1, -1, -1, -1, -1));

    /**
     * Called every tick. Regenerates mana based on the regen rate.
     */
    public void tickMana() {
        int max = getMaxMana();
        if (this.currentMana < max) {
            this.currentMana += (this.manaRegen);
            if (this.currentMana > max) {
                this.currentMana = max;
            }
        }
    }

    public int getMaxMana() {
        return 100 + (level * 10);
    }

    // --- Getters & Setters ---

    public int getElement() {
        return element;
    }

    public void setElement(int element) {
        this.element = element;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public void setCurrentMana(float currentMana) {
        // Clamp the mana to max when setting it manually
        this.currentMana = Math.min(currentMana, getMaxMana());
    }

    public float getManaRegen() {
        return manaRegen;
    }

    public void setManaRegen(float manaRegen) {
        this.manaRegen = manaRegen;
    }

    /**
     * Returns the live list of unlocked spell IDs.
     */
    public List<Integer> getUnlockedSpells() {
        return unlockedSpells;
    }


    /**
     * Returns the live list of active slots.
     */
    public List<Integer> getActiveSlots() {
        return activeSlots;
    }

    /**
     * Helper to check if a spell is unlocked in memory.
     */
    public boolean hasSpell(int spellId) {
        return unlockedSpells.contains(spellId);
    }

    /**
     * Helper to unlock a spell in memory.
     */
    public void unlockSpell(int spellId) {
        if (!unlockedSpells.contains(spellId)) {
            unlockedSpells.add(spellId);
        }
    }

    /**
     * Helper to update a specific slot.
     */
    public void setSlot(int index, int spellId) {
        if (index >= 0 && index < activeSlots.size()) {
            activeSlots.set(index, spellId);
        }
    }

    public void setUnlockedSpells(List<Integer> spells) {
        this.unlockedSpells.clear();
        this.unlockedSpells.addAll(spells);
    }

    /**
     * Overwrites the entire active slots list.
     * Useful for bulk loading from NBT or Sync Packets.
     */
    public void setActiveSlots(List<Integer> slots) {
        this.activeSlots.clear();
        this.activeSlots.addAll(slots);
    }

    //COOLDOWNS
    private final java.util.Map<Integer, Long> spellCooldowns = new java.util.HashMap<>();

    /**
     * Checks if a specific SPELL ID is on cooldown.
     */
    public boolean isSpellOnCooldown(int spellId, long currentTime) {
        Long expiry = spellCooldowns.get(spellId);
        return expiry != null && currentTime < expiry;
    }

    /**
     * Sets the cooldown for a specific SPELL ID.
     */
    public void setSpellCooldown(int spellId, int durationTicks, long currentTime) {
        this.spellCooldowns.put(spellId, currentTime + durationTicks);
    }
}