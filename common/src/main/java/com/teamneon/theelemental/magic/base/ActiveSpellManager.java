package com.teamneon.theelemental.magic.base;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActiveSpellManager {

    private static final List<DurationSpellInstance> activeSpells = new ArrayList<>();

    public static void addSpell(Player player, DurationSpell spell) {
        activeSpells.add(new DurationSpellInstance(player, spell));
    }

    public static void tickAll(ServerLevel level) {
        Iterator<DurationSpellInstance> iterator = activeSpells.iterator();
        while (iterator.hasNext()) {
            DurationSpellInstance instance = iterator.next();
            if (instance.spell.isExpired()) {
                iterator.remove();
            } else {
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
