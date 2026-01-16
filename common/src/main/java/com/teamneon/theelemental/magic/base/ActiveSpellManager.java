package com.teamneon.theelemental.magic.base;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActiveSpellManager {

    private static final List<DurationSpellInstance> activeSpells = new ArrayList<>();

    public static void addSpell(Player player, DurationSpell spell) {
        activeSpells.add(new DurationSpellInstance(player, spell));
    }

    public static void tickAllGlobal(MinecraftServer server) {
        Iterator<DurationSpellInstance> iterator = activeSpells.iterator();
        while (iterator.hasNext()) {
            DurationSpellInstance instance = iterator.next();

            // Use the player's current level to process the spell tick
            Level level = instance.player.level();

            if (instance.spell.isExpired()) {
                // End the spell using the player's current level
                instance.spell.onDurationEnd(level, instance.player);
                iterator.remove();
            } else {
                // Tick the spell logic and the internal counter
                instance.spell.tick(level, instance.player);
                instance.spell.onTick();
            }
        }
    }

    private static class DurationSpellInstance {
        final Player player;
        final DurationSpell spell;

        DurationSpellInstance(Player player, DurationSpell spell) {
            this.player = player;
            this.spell = spell;
        }
    }
}
