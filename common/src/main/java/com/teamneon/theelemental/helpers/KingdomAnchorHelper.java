package com.teamneon.theelemental.helpers;

import com.teamneon.theelemental.kingdoms.KingdomSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class KingdomAnchorHelper {

    private static final int NUM_PILLARS = 9;
    private static final double RADIUS = 8;

    /**
     * Returns the world position of the Kingdom Anchor for a given element.
     *
     * @param level   The server level
     * @param element The kingdom element (playerElement)
     * @return The BlockPos of the pillar/anchor, or null if altar not found
     */
    public static BlockPos getAnchorPos(ServerLevel level, int element) {
        KingdomSavedData globalData = KingdomSavedData.get(level);
        BlockPos altarPos = globalData.getCorePos(-1); // elemental altar

        if (altarPos == null) return null; // safety

        // Map element to pillar index (0-7)
        int pillarIndex = (element - 1) % NUM_PILLARS;

        // Circle angle
        double angle = 2 * Math.PI / NUM_PILLARS * pillarIndex;

        // Compute X/Z
        int x = altarPos.getX() + (int) Math.round(Math.cos(angle) * RADIUS);
        int z = altarPos.getZ() + (int) Math.round(Math.sin(angle) * RADIUS);

        // Y same as altar
        int y = altarPos.getY()-1;

        return new BlockPos(x, y, z);
    }

    /**
     * Reverse lookup: given a pillar position, returns which element it belongs to.
     *
     * @param level     The server level
     * @param pillarPos The world position of the pillar/anchor
     * @return The element index (1-8) or -1 if not a valid pillar
     */
    public static int getElementFromAnchor(ServerLevel level, BlockPos pillarPos) {
        KingdomSavedData globalData = KingdomSavedData.get(level);
        BlockPos altarPos = globalData.getCorePos(-1); // elemental altar

        if (altarPos == null) return -1;

        // Compute delta from altar
        double dx = pillarPos.getX() - altarPos.getX();
        double dz = pillarPos.getZ() - altarPos.getZ();

        // Compute angle
        double angle = Math.atan2(dz, dx);
        if (angle < 0) angle += 2 * Math.PI; // normalize 0..2π

        // Compute pillar index
        int pillarIndex = (int) Math.round(angle / (2 * Math.PI / NUM_PILLARS)) % NUM_PILLARS;

        // Convert to element index (1..NUM_PILLARS)
        return pillarIndex + 1;
    }
}
