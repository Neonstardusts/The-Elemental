package com.teamneon.theelemental.magic.base;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class Spell {

    public abstract int getManaCost();
    public abstract int getCooldownTicks();

    // Can be overridden for spell-specific conditions
    public SpellCastResult checkConditions(Player player, Level level) {
        // Default: always passes
        return SpellCastResult.success();
    }

    // Checks mana and cooldown plus spell-specific conditions
    public SpellCastResult checkCast(Player player, Level level, int currentMana, boolean onCooldown) {
        if (onCooldown) return SpellCastResult.fail("Spell is on cooldown!");
        if (currentMana < getManaCost()) return SpellCastResult.fail("Not enough mana!");

        SpellCastResult conditionResult = checkConditions(player, level);
        if (!conditionResult.isSuccess()) return conditionResult;

        return SpellCastResult.success();
    }

    // Actual spell effect
    public abstract SpellCastResult execute(Level level, Player player);
}
