package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.magic.base.ActiveSpellManager;
import com.teamneon.theelemental.magic.base.DurationSpell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AirSpoutSpell extends DurationSpell {

    public AirSpoutSpell(int manaCost, int cooldownTicks, String name, long durationTicks) {
        super(manaCost, cooldownTicks, name, durationTicks);
    }

    @Override
    public void tick(Level level, Player player) {
        // 1. Maintain Flight State
        if (!player.getAbilities().mayfly || !player.getAbilities().flying) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }

        // 2. Prevent Sprinting
        player.setSprinting(false);

        // 3. Visuals: Air Spout Particles (Server Side)
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            int groundY = getGroundY(level, player.blockPosition());

            // Ground "Dust" Effect (at the base of the spout)
            serverLevel.sendParticles(ParticleTypes.SPIT,
                    player.getX(), groundY + 0.1, player.getZ(),
                    3, 0.4, 0.1, 0.4, 0.05);

            // Vertical Cloud Spout (from ground to player)
            // Stepping by 2.0 blocks to keep it performance friendly
            for (double y = groundY + 1.0; y < player.getY(); y += 2.0) {
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        player.getX(), y, player.getZ(),
                        1, 0.1, 0.1, 0.1, 0.02);
            }

            // Subtle "Whirl" particles around the player's feet
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY(), player.getZ(),
                    2, 0.3, 0.1, 0.3, 0.01);
        }
    }

    @Override
    public void onDurationEnd(Level level, Player player) {
        // Cleanup flight when spell ends
        if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();

            // Safety: Reset fall distance so player doesn't die upon spell ending
            player.fallDistance = 0;
        }
    }

    private int getGroundY(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        // Look down up to 64 blocks for particles
        int maxSearch = 64;

        for (int i = 0; i < maxSearch; i++) {
            if (mutable.getY() <= level.getMinY()) break;
            BlockState state = level.getBlockState(mutable);
            if (!state.isAir() && !state.getCollisionShape(level, mutable).isEmpty()) {
                return mutable.getY() + 1;
            }
            mutable.move(0, -1, 0);
        }
        return (int) (pos.getY() - 10); // Fallback if no ground found
    }




    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (!level.isClientSide()) {
            // 1. Initial Launch Boost
            player.setDeltaMovement(player.getDeltaMovement().x, 0.65, player.getDeltaMovement().z);
            player.hurtMarked = true;

            // 2. Immediate flight activation
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();

            // 3. Register with ActiveSpellManager
            ActiveSpellManager.addSpell(player, this);
            return SpellCastResult.success();
        }
        return SpellCastResult.fail();
    }
}