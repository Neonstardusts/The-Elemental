package com.teamneon.theelemental.magic.base;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class ChargedSpell extends Spell {

    protected final int chargeTicks; // ticks required to fully charge
    protected int chargedFor = 0;    // ticks held so far

    protected ChargedSpell(int manaCost, int cooldownTicks, String name, int chargeTicks) {
        super(manaCost, cooldownTicks, name);
        this.chargeTicks = chargeTicks;
    }

    /** Called every tick while the player holds the key */
    public void onChargeTick(Level level, Player player) {
        chargedFor++;
    }

    /** Called when the key is released */
    public abstract SpellCastResult release(Level level, Player player, int chargedTicks);

    public boolean isFullyCharged() {
        return chargedFor >= chargeTicks;
    }

    public void resetCharge() {
        chargedFor = 0;
    }

    public int getChargedTicks() {
        return chargedFor;
    }
}
