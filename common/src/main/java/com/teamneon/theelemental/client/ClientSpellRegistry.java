package com.teamneon.theelemental.client;
import java.util.HashMap;
import java.util.Map;

public class ClientSpellRegistry {
    private static final Map<Integer, ClientSpellInfo> SPELLS = new HashMap<>();

    public static void registerSpell(ClientSpellInfo info) {
        SPELLS.put(info.spellId, info);
    }

    public static ClientSpellInfo getSpell(int spellId) {
        return SPELLS.get(spellId);
    }

}
