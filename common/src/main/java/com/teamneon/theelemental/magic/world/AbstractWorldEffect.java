package com.teamneon.theelemental.magic.world;

import net.minecraft.world.level.Level;

import java.util.UUID;

public abstract class AbstractWorldEffect implements WorldEffect {

    protected final UUID owner;
    protected int age = 0;
    protected final int maxAge;

    protected AbstractWorldEffect(UUID owner, int maxAge) {
        this.owner = owner;
        this.maxAge = maxAge;
    }

    @Override
    public void tick(Level level) {
        age++;
        tickEffect(level);
    }

    protected abstract void tickEffect(Level level);

    @Override
    public boolean isExpired() {
        return age >= maxAge;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }
}
