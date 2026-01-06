package com.teamneon.theelemental.magic.world;

import net.minecraft.world.level.Level;

import java.util.UUID;

public interface WorldEffect {
    void tick(Level level);
    boolean isExpired();
    UUID getOwner();
}
