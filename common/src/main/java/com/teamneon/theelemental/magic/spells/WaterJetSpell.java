package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.entity.WaterSpellEntity;
import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WaterJetSpell extends Spell {

    public WaterJetSpell(int manaCost, int cooldownTicks, String name) {
        super(manaCost, cooldownTicks, name);
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (!level.isClientSide()) {
            // 1. Spawn the entity slightly in front of the player's eyes
            // We use getEyeY() so it doesn't spawn at their feet
            WaterSpellEntity bolt = new WaterSpellEntity(level, player.getX(), player.getEyeY() - 0.1, player.getZ());

            // 2. Set the owner so the projectile doesn't hit the player who shot it
            bolt.setOwner(player);

            // 3. Set the mode to STRAIGHT (for shooting) and set a lifespan
            bolt.setMode(WaterSpellEntity.MODE_STRAIGHT);
            bolt.setMaxLife(100); // Disappears after 5 seconds if it hits nothing

            // 4. Calculate velocity based on where the player is looking
            // Increase 'speed' to make the projectile faster
            float speed = 1.5f;
            Vec3 lookDirection = player.getLookAngle();
            bolt.setDeltaMovement(lookDirection.x * speed, lookDirection.y * speed, lookDirection.z * speed);

            // 5. Add the entity to the world
            level.addFreshEntity(bolt);

            return SpellCastResult.success();
        }
        return SpellCastResult.fail();
    }
}