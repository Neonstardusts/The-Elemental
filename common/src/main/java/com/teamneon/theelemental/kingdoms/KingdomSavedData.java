package com.teamneon.theelemental.kingdoms;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * Global saved data for elemental kingdoms (Overworld only)
 */
public class KingdomSavedData extends SavedData {

    private static final String NAME = "elemental_kingdoms";
    private final Map<String, KingdomCoreEntry> cores;

    public KingdomSavedData() {
        this.cores = new HashMap<>();
        System.out.println("[KingdomSavedData DEBUG] Initialized empty map");
    }

    private KingdomSavedData(Map<String, KingdomCoreEntry> cores) {
        this.cores = new HashMap<>(cores);
    }

    /** Codec uses strings for both keys and values */
    public static final Codec<KingdomSavedData> CODEC =
            Codec.unboundedMap(Codec.STRING, KingdomCoreEntry.CODEC)
                    .xmap(KingdomSavedData::new, data -> data.cores);

    public static final SavedDataType<KingdomSavedData> TYPE =
            new SavedDataType<>(NAME, KingdomSavedData::new, CODEC, DataFixTypes.LEVEL);

    /* ---------------- API ---------------- */

    public void removeCore(int elementId) {
        String key = "E" + elementId;
        if (cores.containsKey(key)) {
            cores.remove(key);
            setDirty(); // marks data to save next tick
        }
    }

    public static KingdomSavedData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        KingdomSavedData data = overworld.getDataStorage().computeIfAbsent(TYPE);
        System.out.println("[KingdomSavedData DEBUG] Accessed global data, contains " + data.cores.size() + " entries");
        return data;
    }

    public BlockPos getCorePos(int element) {
        String key = "E" + element;
        KingdomCoreEntry entry = cores.get(key);
        if (entry == null) {
            System.out.println("[KingdomSavedData DEBUG] getCorePos: No kingdom found for element " + element);
            return null;
        }
        return entry.getPos();
    }

    public void registerCore(int element, BlockPos pos) {
        String key = "E" + element;
        System.out.println("[KingdomSavedData DEBUG] registerCore: Creating kingdom " + key);
        cores.put(key, KingdomCoreEntry.of(element, pos));
        setDirty();
        System.out.println("[KingdomSavedData DEBUG] registerCore: Marked dirty, total entries: " + cores.size());
    }
}
