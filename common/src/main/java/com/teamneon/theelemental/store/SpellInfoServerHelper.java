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
            Map<String, Object> spellJson = SpellJsonLoader.getFullSpellJson(spellId, manager);

            String name = (String) spellJson.getOrDefault("SpellName", "Unknown Spell");
            // Extract the Description from the JSON
            String description = (String) spellJson.getOrDefault("Description", "No description provided.");

            int manaCost = ((Number) spellJson.getOrDefault("ManaCost", 0)).intValue();
            int cooldown = ((Number) spellJson.getOrDefault("Cooldown", 0)).intValue();
            int duration = ((Number) spellJson.getOrDefault("Duration", 0)).intValue(); // Read from JSON
            int requiredLevel = ((Number) spellJson.getOrDefault("RequiredLevel", 0)).intValue();

            // Return the updated constructor including the description
            return new ClientSpellInfo(spellId, name, description, manaCost, cooldown, duration,  requiredLevel);

        } catch (Exception e) {
            System.err.println("Failed to load SpellInfo for spellId " + spellId + ": " + e.getMessage());
            // Fallback object matching the updated ClientSpellInfo constructor
            return new ClientSpellInfo(spellId, "Unknown", "", 0, 0, 0, 0);        }
    }
}