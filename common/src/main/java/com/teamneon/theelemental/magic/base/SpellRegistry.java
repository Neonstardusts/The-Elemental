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


    private static final Spell EMPTY = new Spell(0, 0, "") {
        @Override
        public SpellCastResult execute(net.minecraft.world.level.Level level,
                                       net.minecraft.world.entity.player.Player player) {
            return SpellCastResult.fail();
        }
    };

    private static String strVal(Map<String, Object> json, String key) {
        return (String) json.getOrDefault(key, "");
    }


    public static void init() {
        register(1001, json ->
                new FireballSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName")
                )
        );

        register(1002, json ->
                new CinderSigilSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName"),
                        intVal(json, "Duration")
                )
        );

        register(2001, json ->
                new LuxSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName")
                )
        );

        register(3001, json ->
                new SpectralShotSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName"),
                        intVal(json, "Duration")
                )
        );

        register(4001, json ->
                new PoisonSpraySpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName"),
                        intVal(json, "Duration")
                )
        );

        register(5001, json ->
                new ChainLightningSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName")
                )
        );

        register(6001, json ->
                new TorrentSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName"),
                        intVal(json, "Duration")
                )
        );

        register(6002, json ->
                new WaterJetSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName")
                )
        );

        register(6003, json ->
                new FreezeSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName")
                )
        );

        register(6004, json ->
                new IceShieldSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName")
                )
        );

        register(7001, json ->
                new WitherSkullSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName")
                )
        );



        register(8001, json ->
                new BlinkSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName")
                )
        );

        register(9001, json ->
                new LeapSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName")
                )
        );

        register(9002, json ->
                new AirSpoutSpell(
                        intVal(json, "ManaCost"),
                        intVal(json, "Cooldown"),
                        strVal(json, "SpellName"),
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

    public static int getSpellDuration(int spellId, ResourceManager manager) {
        try {
            Map<String, Object> json = SpellJsonLoader.getFullSpellJson(spellId, manager);
            // We use Number to handle various JSON numeric formats safely before converting to int
            return ((Number) json.getOrDefault("Duration", 0)).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getSpellName(int spellId, ResourceManager manager) {
        try {
            Map<String, Object> json =
                    SpellJsonLoader.getFullSpellJson(spellId, manager);
            return (String) json.getOrDefault("SpellName", "");
        } catch (Exception e) {
            return "";
        }
    }


    public static int getRequiredLevel(int spellId, ResourceManager manager) {
        try {
            Map<String, Object> json =
                    SpellJsonLoader.getFullSpellJson(spellId, manager);
            return ((Number) json.getOrDefault("RequiredLevel", 0)).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getSpellDescription(int spellId, ResourceManager manager) {
        try {
            Map<String, Object> json = SpellJsonLoader.getFullSpellJson(spellId, manager);
            // Using "Description" as the key to match your JSON structure
            return (String) json.getOrDefault("Description", "No description available.");
        } catch (Exception e) {
            return "No description available.";
        }
    }


    public static Set<Integer> getAllSpellIds() {
        return SPELLS.keySet();
    }

}
