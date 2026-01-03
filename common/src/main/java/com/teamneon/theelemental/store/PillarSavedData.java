package com.teamneon.theelemental.world;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * Global saved data for the spawn pillar
 * All data stored as strings
 */
public class PillarSavedData extends SavedData {

    private static final String NAME = "theelemental_pillar";

    // All data stored as strings
    private final Map<String, String> data;

    public PillarSavedData() {
        this.data = new HashMap<>();
    }

    private PillarSavedData(Map<String, String> data) {
        this.data = new HashMap<>(data);
    }

    /** Codec: saves/loads map of strings */
    public static final Codec<PillarSavedData> CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.STRING)
                    .xmap(PillarSavedData::new, d -> d.data);

    public static final SavedDataType<PillarSavedData> TYPE =
            new SavedDataType<>(NAME, PillarSavedData::new, CODEC, DataFixTypes.LEVEL);

    /* ---------------- API ---------------- */

    public static PillarSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    /** Check if pillar has been generated */
    public boolean isGenerated() {
        return "true".equals(data.get("generated"));
    }

    /** Mark pillar as generated and store its position as string "x,y,z" */
    public void markGenerated(int x, int y, int z) {
        data.put("generated", "true");
        data.put("pos", x + "," + y + "," + z);
        setDirty();
    }

    /** Get pillar position if stored, else null */
    public int[] getPillarPos() {
        String pos = data.get("pos");
        if (pos == null) return null;
        String[] split = pos.split(",");
        return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
    }
}
