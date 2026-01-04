package com.teamneon.theelemental.magic.base;

import com.teamneon.theelemental.helpers.SpellJsonLoader;
import com.teamneon.theelemental.magic.spells.*;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SpellRegistry {

    private static final Map<Integer, SpellFactory> SPELLS = new HashMap<>();
    private static final Map<Integer, Integer> SPELL_COUNT = new HashMap<>();

    private static final Spell EMPTY = new Spell(0, 0) {
        @Override
        public SpellCastResult execute(net.minecraft.world.level.Level level,
                                       net.minecraft.world.entity.player.Player player) {
            return SpellCastResult.fail();
        }
    };

    public static void init() {
        register(1001, json ->
                new FireballSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown")
                )
        );

        register(8001, json ->
                new BlinkSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown")
                )
        );

        register(3001, json ->
                new PoisonSpraySpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        intVal(json, "Duration")
                )
        );

        register(6001, json ->
                new TorrentSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        intVal(json, "Duration")
                )
        );
    }

    public static void register(int id, SpellFactory factory) {
        SPELLS.put(id, factory);

        if (id != 0) {
            int elementId = id / 1000;
            SPELL_COUNT.put(elementId, SPELL_COUNT.getOrDefault(elementId, 0) + 1);
        }
    }

    public static Spell createSpell(int id, Map<String, Object> json) {
        SpellFactory factory = SPELLS.get(id);
        return factory == null ? EMPTY : factory.create(json);
    }

    public static int getSpellCountForElement(int elementId) {
        return SPELL_COUNT.getOrDefault(elementId, 0);
    }

    private static int intVal(Map<String, Object> json, String key) {
        return ((Number) json.getOrDefault(key, 0)).intValue();
    }

    public static Spell getSpell(int spellId, ResourceManager manager) {
        try {
            Map<String, Object> json =
                    SpellJsonLoader.getFullSpellJson(spellId, manager);
            return createSpell(spellId, json);
        } catch (Exception e) {
            return EMPTY;
        }
    }

    public static Set<Integer> getAllSpellIds() {
        return SPELLS.keySet();
    }

}
