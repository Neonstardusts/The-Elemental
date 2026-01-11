package com.teamneon.theelemental.magic.base;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class HeldSpell extends Spell {

    protected int heldTicks = 0;

    protected HeldSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    /** Called once when player starts holding the key */
    public void onStart(Level level, Player player) {
        heldTicks = 0;
    }

    /** Called every tick while the key is held */
    public abstract void onHeldTick(Level level, Player player);

    /** Called when the key is released */
    public void onRelease(Level level, Player player) {
        // Optional override
    }

    /** Internal ticking helper (can be used by ActiveSpellManager) */
    public void tick(Level level, Player player) {
        heldTicks++;
        onHeldTick(level, player);
    }

    public int getHeldTicks() {
        return heldTicks;
    }
}
