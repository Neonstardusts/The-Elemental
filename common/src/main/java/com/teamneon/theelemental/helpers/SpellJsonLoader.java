package com.teamneon.theelemental.helpers;

import com.google.gson.Gson;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.util.Map;

import static com.teamneon.theelemental.Theelemental.id;

public final class SpellJsonLoader {

    private static final Gson GSON = new Gson();

    private SpellJsonLoader() { throw new UnsupportedOperationException(); }

    /**
     * Loads the RecipeItems for a spell by reading its JSON file.
     */
    public static Map<String, Integer> getRecipeForSpell(int spellId, ResourceManager manager) throws Exception {
        Map<String, Object> jsonMap = getFullSpellJson(spellId, manager);

        Object recipeObj = jsonMap.get("RecipeItems");
        if (!(recipeObj instanceof Map<?, ?> recipeMapRaw)) {
            throw new Exception("Spell JSON missing 'RecipeItems' field: " + spellId);
        }

        @SuppressWarnings("unchecked")
        Map<String, Integer> recipeMap = recipeMapRaw.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> ((Number) e.getValue()).intValue()
                ));

        return recipeMap;
    }

    /**
     * Loads the full spell JSON as a Map<String, Object>.
     * Use this to get SpellName, ManaCost, Cooldown, etc.
     */
    public static Map<String, Object> getFullSpellJson(int spellId, ResourceManager manager) throws Exception {
        Identifier loc = id("spells/" + spellId + ".json");
        Resource resource = manager.getResource(loc)
                .orElseThrow(() -> new Exception("Spell JSON not found: " + spellId));

        try (InputStreamReader reader = new InputStreamReader(resource.open())) {
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = GSON.fromJson(reader, Map.class);
            return jsonMap;
        }
    }
}
