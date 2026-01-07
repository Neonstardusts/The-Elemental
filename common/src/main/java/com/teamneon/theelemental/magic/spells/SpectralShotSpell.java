package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.ActiveSpellManager;
import com.teamneon.theelemental.magic.base.DurationSpell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class SpectralShotSpell extends DurationSpell {

    public SpectralShotSpell(int manaCost, int cooldownTicks, String name, long durationTicks) {
        super(manaCost, cooldownTicks, name, durationTicks);
    }

    @Override
    public void tick(Level level, Player player) {
        if (level.isClientSide()) return; // Only run on server

        // 1. Get the block the player is looking at (raycast)
        double reachDistance = 20.0; // how far the spell targets
        HitResult hitResult = player.pick(reachDistance, 0.0f, false);

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return; // No target block, exit
        }

        Vec3 targetPos = hitResult.getLocation();

        // 2. Launch multiple arrows from around/above the player
        int arrowCount = 5; // number of arrows in volley
        Random random = new Random();

        for (int i = 0; i < arrowCount; i++) {
            // Random spawn position above the player
            double offsetX = (random.nextDouble() - 0.5) * 2.0; // -1 to 1
            double offsetZ = (random.nextDouble() - 0.5) * 2.0; // -1 to 1
            double spawnY = player.getY() + 3.0 + random.nextDouble(); // 3-4 blocks above

            Vec3 spawnPos = player.position().add(offsetX, spawnY - player.getY(), offsetZ);

            // Direction toward target
            Vec3 direction = targetPos.subtract(spawnPos).normalize();


            Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), null);
            arrow.setOwner(player);
            arrow.setDeltaMovement(direction.scale(2.0)); // speed multiplier
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED; // prevents arrow from being picked up
            level.addFreshEntity(arrow);
        }
    }


    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (!level.isClientSide()) {
            ActiveSpellManager.addSpell(player, this);
            return SpellCastResult.success();
        }
        return SpellCastResult.fail();
    }
}
