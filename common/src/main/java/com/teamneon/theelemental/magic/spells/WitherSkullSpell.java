package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WitherSkullSpell extends Spell {

    public WitherSkullSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (!level.isClientSide()) { // Only spawn on server
            // Get the direction the player is looking
            Vec3 look = player.getLookAngle();

            // Create the Wither Skull projectile
            WitherSkull skull = new WitherSkull(level, player, look);

            // Set it as "dangerous" so it behaves like the usual hurtful Wither Skull
            skull.setDangerous(true);

            // Position it at the player's eye level
            skull.setPos(player.getX() + look.x, player.getEyeY() + look.y, player.getZ() + look.z);

            // Spawn the projectile in the world
            level.addFreshEntity(skull);
        }

        return SpellCastResult.success();
    }


}