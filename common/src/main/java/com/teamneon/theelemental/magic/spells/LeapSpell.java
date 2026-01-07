package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LeapSpell extends Spell {

    public LeapSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        // Make sure we're on the server side
        if (!level.isClientSide()) {
            // Get the player's current movement vector (direction they're facing + moving)
            Vec3 movement = player.getLookAngle();

            // Scale the launch velocity
            double launchStrength = 1.5; // adjust for desired force
            Vec3 launch = movement.normalize().scale(launchStrength);

            // Apply the velocity to the player
            player.setDeltaMovement(launch);
            player.hurtMarked = true;

            // Optional: reset fall distance so the player doesn't take fall damage
            player.fallDistance = 0f;
        }

        return SpellCastResult.success();
    }

}