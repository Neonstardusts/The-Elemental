package com.teamneon.theelemental.kingdoms;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record KingdomCoreEntry(String elementId, String posString) {

    /** Convert from BlockPos to string */
    public static String posToString(BlockPos pos) {
        return "X" + pos.getX() + "Y" + pos.getY() + "Z" + pos.getZ();
    }

    /** Convert from string to BlockPos */
    public static BlockPos stringToPos(String s) {
        try {
            int x = Integer.parseInt(s.split("X|Y|Z")[1]);
            int y = Integer.parseInt(s.split("X|Y|Z")[2]);
            int z = Integer.parseInt(s.split("X|Y|Z")[3]);
            return new BlockPos(x, y, z);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid pos string: " + s, e);
        }
    }

    /** Codec for saving/loading as all strings */
    public static final Codec<KingdomCoreEntry> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.STRING.fieldOf("element").forGetter(KingdomCoreEntry::elementId),
                    Codec.STRING.fieldOf("pos").forGetter(KingdomCoreEntry::posString)
            ).apply(inst, KingdomCoreEntry::new)
    );

    /** Get BlockPos from posString */
    public BlockPos getPos() {
        return stringToPos(posString);
    }

    /** Factory method from element int and BlockPos */
    public static KingdomCoreEntry of(int element, BlockPos pos) {
        return new KingdomCoreEntry("E" + element, posToString(pos));
    }
}
