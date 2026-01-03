package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FireballSpell extends Spell {

    @Override
    public int getManaCost() {
        return 20;
    }

    @Override
    public int getCooldownTicks() {
        return 40;
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        // 1. Get direction
        Vec3 look = player.getLookAngle();

        // 2. Instantiate using the 1.21 signature:
        // Level, X, Y, Z, PowerVector (Vec3)
        SmallFireball fireball = new SmallFireball(
                level,
                player.getX(),
                player.getEyeY(),
                player.getZ(),
                look // This is the Vec3 p_480553_ it's looking for
        );

        // 3. Set the owner so the game knows who fired it (for kill credit/advancements)
        fireball.setOwner(player);

        // 4. Spawn it
        level.addFreshEntity(fireball);
        return SpellCastResult.success();
    }
}