package com.teamneon.theelemental.magic.base;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class DurationSpell extends Spell {

    protected final long durationTicks;
    protected long ticksElapsed = 0;

    protected DurationSpell(int manaCost, int cooldownTicks, long durationTicks) {
        super(manaCost, cooldownTicks);
        this.durationTicks = durationTicks;
    }

    public abstract void tick(Level level, Player player);

    public void onTick() {
        ticksElapsed++;
    }

    public boolean isExpired() {
        return ticksElapsed >= durationTicks;
    }
}
