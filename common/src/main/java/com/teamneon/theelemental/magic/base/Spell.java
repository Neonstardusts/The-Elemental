package com.teamneon.theelemental.magic.base;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class Spell {

    protected final int manaCost;
    protected final int cooldownTicks;
    protected final String name;

    protected Spell(int manaCost, int cooldownTicks, String name) {
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getManaCost() {
        return manaCost;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public SpellCastResult checkConditions(Player player, Level level) {
        return SpellCastResult.success();
    }

    public SpellCastResult checkCast(Player player, Level level, int currentMana, boolean onCooldown) {
        if (onCooldown) return SpellCastResult.fail("Spell is on cooldown!");
        if (currentMana < manaCost) return SpellCastResult.fail("Not enough mana!");

        SpellCastResult conditionResult = checkConditions(player, level);
        if (!conditionResult.isSuccess()) return conditionResult;

        return SpellCastResult.success();
    }

    public abstract SpellCastResult execute(Level level, Player player);
}
