package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.helpers.TempBlock;
import com.teamneon.theelemental.magic.base.DurationSpell;
import com.teamneon.theelemental.magic.base.ActiveSpellManager;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TorrentSpell extends DurationSpell {

    private static final int ORBIT_RADIUS = 3;
    private static final int ORBIT_POINTS = 16;
    private static final long WATER_DURATION_TICKS = 1; // how long each water block lasts

    private float angle = 0f;

    public TorrentSpell() {
        super(200); // spell lasts 200 ticks
    }

    @Override
    public int getManaCost() {
        return 10;
    }

    @Override
    public int getCooldownTicks() {
        return 40;
    }

    @Override
    public void tick(Level level, Player player) {
        // Debug tick
        System.out.println("[DEBUG] TorrentSpell tick for player: " + player.getName().getString());

        // Player position slightly above ground
        var playerPos = player.position().add(0, 1, 0);

        for (int i = 0; i < ORBIT_POINTS; i++) {
            double theta = angle + 2 * Math.PI * i / ORBIT_POINTS;
            double x = playerPos.x + ORBIT_RADIUS * Math.cos(theta);
            double z = playerPos.z + ORBIT_RADIUS * Math.sin(theta);
            BlockPos pos = new BlockPos((int) Math.floor(x), (int) Math.floor(playerPos.y), (int) Math.floor(z));

            // Only place water if air
            if (level.getBlockState(pos).isAir() && !TempBlock.isTempBlock(level, pos.below())) {
                TempBlock.placeAlmostFullWater(level, pos, WATER_DURATION_TICKS);
            }
        }

        angle += 0.1; // rotate orbit slightly each tick
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (!level.isClientSide()) {
            System.out.println("[DEBUG] Casting TorrentSpell for player: " + player.getName().getString());
            ActiveSpellManager.addSpell(player, new TorrentSpell());
            System.out.println("[DEBUG] TorrentSpell added to ActiveSpellManager");
            return SpellCastResult.success();
        } else {
            System.out.println("[DEBUG] Execute called on client side, ignoring");
            return SpellCastResult.fail();
        }
    }
}