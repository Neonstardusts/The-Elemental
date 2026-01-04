package com.teamneon.theelemental.store;

import com.teamneon.theelemental.client.ClientSpellInfo;
import com.teamneon.theelemental.helpers.SpellJsonLoader;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Map;

public class SpellInfoServerHelper {

    /**
     * Loads a SpellInfo for the given spellId from JSON.
     */
    public static ClientSpellInfo loadSpellInfo(int spellId, ResourceManager manager) {
        try {
            // 1️⃣ Load full JSON for the spell
            Map<String, Object> spellJson = SpellJsonLoader.getFullSpellJson(spellId, manager);

            // 2️⃣ Extract values
            String name = (String) spellJson.getOrDefault("SpellName", "Unknown Spell");

            Number manaNum = (Number) spellJson.getOrDefault("ManaCost", 0);
            int manaCost = manaNum.intValue();

            Number cooldownNum = (Number) spellJson.getOrDefault("Cooldown", 0);
            int cooldown = cooldownNum.intValue();

            // 3️⃣ Create the client-friendly SpellInfo
            return new ClientSpellInfo(spellId, name, manaCost, cooldown);

        } catch (Exception e) {
            // Fallback: if JSON fails, log and return dummy SpellInfo
            System.err.println("Failed to load SpellInfo for spellId " + spellId + ": " + e.getMessage());
            return new ClientSpellInfo(spellId, "Unknown Spell", 0, 0);
        }
    }
}
