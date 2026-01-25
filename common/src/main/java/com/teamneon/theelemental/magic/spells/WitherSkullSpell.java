package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WitherSkullSpell extends Spell {

    public WitherSkullSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (!level.isClientSide()) {
            Vec3 look = player.getLookAngle();

            // 1. Create the skull
            WitherSkull skull = new WitherSkull(level, player, Vec3.ZERO);

            // 2. Manually set the acceleration power field
            skull.accelerationPower = 0.5;

            // 3. MANUAL IMPLEMENTATION of the private 'assignDirectionalMovement'
            // We normalize the look vector and scale it by our power
            skull.setDeltaMovement(look.normalize().scale(skull.accelerationPower));

            // This ensures the server tells the client the entity is moving
            skull.hurtMarked = true;

            // 4. Position and state
            skull.setPos(player.getX() + look.x * 1.2, player.getEyeY() + look.y * 1.2, player.getZ() + look.z * 1.2);
            skull.setDangerous(true);

            // 5. Sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0f, 1.3f);

            level.addFreshEntity(skull);
        }

        return SpellCastResult.success();
    }
}