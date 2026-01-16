package com.teamneon.theelemental.magic.base;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class DurationSpell extends Spell {

    protected final long durationTicks;
    protected long ticksElapsed = 0;

    protected DurationSpell(int manaCost, int cooldownTicks,  String name, long durationTicks) {
        super(manaCost, cooldownTicks, name);
        this.durationTicks = durationTicks;
    }

    public void onDurationEnd(Level level, Player player) {
        // Default implementation does nothing
    }


    public abstract void tick(Level level, Player player);

    public void onTick() {
        ticksElapsed++;
    }

    public boolean isExpired() {
        return ticksElapsed >= durationTicks;
    }
}
