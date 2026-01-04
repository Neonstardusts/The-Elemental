package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BlinkSpell extends Spell {

    public BlinkSpell(int manaCost, int cooldownTicks) {
        super(manaCost, cooldownTicks);
    }

    // Spell-specific conditions
    @Override
    public SpellCastResult checkConditions(Player player, Level level) {
        Vec3 look = player.getLookAngle();
        Vec3 targetPos = player.position().add(look.scale(7.0));

        if (!level.getBlockState(BlockPos.containing(targetPos)).isAir()) {
            return SpellCastResult.fail("Target location is obstructed!");
        }

        return SpellCastResult.success();
    }

    // Spell execution
    @Override
    public SpellCastResult execute(Level level, Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 targetPos = player.position().add(look.scale(7.0));

        player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        return SpellCastResult.success();
    }
}
