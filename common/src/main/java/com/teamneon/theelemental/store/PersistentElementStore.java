package com.teamneon.theelemental.store;

import net.blay09.mods.balm.Balm;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersistentElementStore {

    private static final String TAG_NAME = "TheElementalData";

    // Keys
    private static final String KEY_ELEMENT = "Element";
    private static final String KEY_LEVEL = "Level";
    private static final String KEY_CURRENT_MANA = "CurrentMana";
    private static final String KEY_MANA_REGEN = "ManaRegen";
    private static final String KEY_UNLOCKED_SPELLS = "UnlockedSpells";
    private static final String KEY_ACTIVE_SLOTS = "ActiveSlots";

    // Defaults
    private static final int DEFAULT_ELEMENT = 0;
    private static final int DEFAULT_LEVEL = 0;
    private static final int DEFAULT_CURRENT_MANA = 0;
    private static final float DEFAULT_MANA_REGEN = 1.0f;
    // 0 = unlocked/empty, -1 = locked, >0 = spell id
    private static final List<Integer> DEFAULT_ACTIVE_SLOTS = Arrays.asList(0, 0, 0, -1, -1, -1, -1, -1);

    /**
     * Helper to get the mod-specific data tag.
     */

    private static CompoundTag getData(Player player) {
        // 1. Get the player's root persistent NBT (ForgeData on Forge, custom on Fabric)
        CompoundTag rootPersistedData = Balm.hooks().getPersistentData(player);

        // 2. Safely retrieve or create your MOD-SPECIFIC sub-tag
        if (!rootPersistedData.contains(TAG_NAME)) {
            rootPersistedData.put(TAG_NAME, new CompoundTag());
        }

        return rootPersistedData.getCompoundOrEmpty(TAG_NAME);
    }

    private static void save(Player player, CompoundTag modData) {
        Balm.hooks().getPersistentData(player).put(TAG_NAME, modData);
    }

    // ============================
    //         Simple Stats
    // ============================

    public static int getElement(Player player) {
        return getData(player).getIntOr(KEY_ELEMENT, DEFAULT_ELEMENT);
    }

    public static void setElement(Player player, int value) {
        CompoundTag data = getData(player);
        data.putInt(KEY_ELEMENT, value);
    }

    public static int getLevel(Player player) {
        return getData(player).getIntOr(KEY_LEVEL, DEFAULT_LEVEL);
    }

    public static void setLevel(Player player, int value) {
        CompoundTag data = getData(player);
        data.putInt(KEY_LEVEL, value);
    }

    public static int getCurrentMana(Player player) {
        return getData(player).getIntOr(KEY_CURRENT_MANA, DEFAULT_CURRENT_MANA);
    }

    public static void setCurrentMana(Player player, int value) {
        CompoundTag data = getData(player);
        data.putInt(KEY_CURRENT_MANA, value);
        //save(player, data);
    }

    public static float getManaRegen(Player player) {
        CompoundTag data = getData(player);
        // Balm might not have getFloatOr, so we use standard NBT check
        if (data.contains(KEY_MANA_REGEN)) {
            Tag tag = data.get(KEY_MANA_REGEN);
            if (tag instanceof FloatTag floatTag) {
                return floatTag.floatValue();
            }
        }
        return DEFAULT_MANA_REGEN;
    }

    public static void setManaRegen(Player player, float value) {
        CompoundTag data = getData(player);
        data.putFloat(KEY_MANA_REGEN, value);
    }

    // ============================
    //       Unlocked Spells
    // ============================

    public static List<Integer> getUnlockedSpells(Player player) {
        List<Integer> spells = new ArrayList<>();
        ListTag listTag = getData(player).getListOrEmpty(KEY_UNLOCKED_SPELLS);

        for (Tag tag : listTag) {
            if (tag instanceof IntTag intTag) {
                spells.add(intTag.intValue());
            }
        }
        return spells;
    }

    public static void unlockSpell(Player player, int spellId) {
        CompoundTag data = getData(player);
        ListTag listTag = data.getListOrEmpty(KEY_UNLOCKED_SPELLS);

        boolean alreadyUnlocked = false;
        for (Tag tag : listTag) {
            if (tag instanceof IntTag && ((IntTag) tag).intValue() == spellId) {
                alreadyUnlocked = true;
                break;
            }
        }

        if (!alreadyUnlocked) {
            listTag.add(IntTag.valueOf(spellId));
            data.put(KEY_UNLOCKED_SPELLS, listTag);
        }
    }



    // ============================
    //        Active Slots
    // ============================

    /**
     * Gets the current active slots. Initializes them to default if missing or invalid size.
     */
    public static List<Integer> getActiveSlots(Player player) {
        CompoundTag data = getData(player);
        ListTag listTag = data.getListOrEmpty(KEY_ACTIVE_SLOTS);

        // If data is missing or corrupted (wrong size), reset to default
        if (listTag.isEmpty() || listTag.size() != 8) {
            resetActiveSlotsToDefault(player);
            return new ArrayList<>(DEFAULT_ACTIVE_SLOTS);
        }

        List<Integer> slots = new ArrayList<>();
        for (Tag tag : listTag) {
            if (tag instanceof IntTag intTag) {
                slots.add(intTag.intValue());
            } else {
                slots.add(0); // Safety fallback
            }
        }
        return slots;
    }

    /**
     * Sets a specific slot index to a spell ID (or 0 for empty, -1 for locked).
     */
    public static void setActiveSlot(Player player, int index, int spellId) {
        if (index < 0 || index >= 8) return;

        CompoundTag data = getData(player);
        ListTag listTag = data.getListOrEmpty(KEY_ACTIVE_SLOTS);

        // Ensure list is valid before modifying
        if (listTag.isEmpty() || listTag.size() != 8) {
            listTag = createDefaultActiveSlotsTag();
        }

        // Update the specific index
        listTag.set(index, IntTag.valueOf(spellId));

        data.put(KEY_ACTIVE_SLOTS, listTag);
    }

    /**
     * Helper to write the default [0,0,0,-1,-1,-1,-1,-1] to NBT
     */
    private static void resetActiveSlotsToDefault(Player player) {
        CompoundTag data = getData(player);
        data.put(KEY_ACTIVE_SLOTS, createDefaultActiveSlotsTag());
    }

    private static ListTag createDefaultActiveSlotsTag() {
        ListTag list = new ListTag();
        for (int val : DEFAULT_ACTIVE_SLOTS) {
            list.add(IntTag.valueOf(val));
        }
        return list;
    }


    public static void setUnlockedSpells(Player player, List<Integer> spells) {
        CompoundTag data = getData(player);
        ListTag listTag = new ListTag();
        for (int id : spells) {
            listTag.add(IntTag.valueOf(id));
        }
        data.put(KEY_UNLOCKED_SPELLS, listTag);
    }

    public static void setActiveSlots(Player player, List<Integer> slots) {
        CompoundTag data = getData(player);
        ListTag listTag = new ListTag();
        for (int id : slots) {
            listTag.add(IntTag.valueOf(id));
        }
        data.put(KEY_ACTIVE_SLOTS, listTag);
    }
}