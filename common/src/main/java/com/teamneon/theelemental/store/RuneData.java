package com.teamneon.theelemental.store;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

public record RuneData(
        int elementId,
        int spellId,
        String spellName,
        Map<String, Integer> recipeItems,
        int manaCost,
        int cooldown,
        String description
) {
    public static final RuneData EMPTY = new RuneData(0, 0, "Unknown Spell", new HashMap<>(), 0, 0, "No description");

    public static final Codec<RuneData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("elementId").forGetter(RuneData::elementId),
                    Codec.INT.fieldOf("spellId").forGetter(RuneData::spellId),
                    Codec.STRING.fieldOf("spellName").forGetter(RuneData::spellName),
                    Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("recipeItems").forGetter(RuneData::recipeItems),
                    Codec.INT.fieldOf("manaCost").forGetter(RuneData::manaCost),
                    Codec.INT.fieldOf("cooldown").forGetter(RuneData::cooldown),
                    Codec.STRING.fieldOf("description").forGetter(RuneData::description)
            ).apply(instance, RuneData::new)
    );
}
