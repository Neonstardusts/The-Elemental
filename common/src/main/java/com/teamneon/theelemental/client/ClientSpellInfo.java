package com.teamneon.theelemental.client;

public class ClientSpellInfo {
    public final int spellId;
    public final String name;
    public final String description;
    public final int manaCost;
    public final int cooldownTicks;
    public final int durationTicks; // Add this
    public final int requiredLevel;

    public ClientSpellInfo(int spellId, String name, String description, int manaCost, int cooldownTicks, int durationTicks, int requiredLevel) {
        this.spellId = spellId;
        this.name = name;
        this.description = description;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
        this.durationTicks = durationTicks; // Initialize
        this.requiredLevel = requiredLevel;
    }
}