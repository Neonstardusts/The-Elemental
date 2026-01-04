package com.teamneon.theelemental.client;

public class ClientSpellInfo {
    public final int spellId;
    public final String name;
    public final int manaCost;
    public final int cooldownTicks;

    public ClientSpellInfo(int spellId, String name, int manaCost, int cooldownTicks) {
        this.spellId = spellId;
        this.name = name;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
    }
}
