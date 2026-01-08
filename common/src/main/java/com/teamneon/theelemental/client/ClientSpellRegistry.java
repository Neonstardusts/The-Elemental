package com.teamneon.theelemental.client;
import com.teamneon.theelemental.magic.base.SpellDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientSpellRegistry {
    private static final Map<Integer, ClientSpellInfo> SPELLS = new HashMap<>();

    public static void registerSpell(ClientSpellInfo info) {
        SPELLS.put(info.spellId, info);
    }

    public static ClientSpellInfo getSpell(int spellId) {
        return SPELLS.get(spellId);
    }

    public static List<SpellDefinition> getSpellsForElement(int elementId) {
        return SPELLS.values().stream()
                .filter(info -> info.spellId / 1000 == elementId)
                .sorted((a, b) -> Integer.compare(a.spellId, b.spellId))
                .map(info -> new SpellDefinition(info.spellId, info.requiredLevel))
                .toList();
    }


}
