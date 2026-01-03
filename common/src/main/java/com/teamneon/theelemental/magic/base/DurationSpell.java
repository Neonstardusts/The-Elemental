package com.teamneon.theelemental.magic.base;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

public abstract class DurationSpell extends Spell {

    protected final long durationTicks; // how long the spell lasts in ticks
    protected long ticksElapsed = 0; // increment each tick

    public DurationSpell(long durationTicks) {
        this.durationTicks = durationTicks;
    }

    /** Called every tick while spell is active */
    public abstract void tick(Level level, Player player);

    /** Increment the tick counter (call each tick in ActiveSpellManager) */
    public void onTick() {
        ticksElapsed++;
    }

    /** Returns true if the spell is finished */
    public boolean isExpired() {
        return ticksElapsed >= durationTicks;
    }
}

