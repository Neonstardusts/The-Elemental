package com.teamneon.theelemental.magic.world;

import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WorldEffectManager {

    private static final List<WorldEffect> effects = new ArrayList<>();

    public static void add(WorldEffect effect) {
        effects.add(effect);
    }

    public static void tickAll(ServerLevel level) {
        Iterator<WorldEffect> it = effects.iterator();
        while (it.hasNext()) {
            WorldEffect effect = it.next();

            // FIX: If this effect belongs to the Overworld, and the Nether is ticking, skip it.
            if (effect.getLevel() != level) {
                continue;
            }

            effect.tick(level);

            if (effect.isExpired()) {
                it.remove();
            }
        }
    }
}
