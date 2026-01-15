package com.teamneon.theelemental.store;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ManaData(float mana) {

    public static final ManaData EMPTY = new ManaData(0f);

    public static final Codec<ManaData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("mana").forGetter(ManaData::mana)
            ).apply(instance, ManaData::new)
    );
}