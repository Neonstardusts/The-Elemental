package com.teamneon.theelemental.magic.spells;

import com.teamneon.theelemental.entity.WaterSpellEntity;
import com.teamneon.theelemental.helpers.TempBlock;
import com.teamneon.theelemental.magic.base.DurationSpell;
import com.teamneon.theelemental.magic.base.ActiveSpellManager;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TorrentSpell extends DurationSpell {

    public TorrentSpell(int manaCost, int cooldownTicks, String name, long durationTicks) {
        super(manaCost, cooldownTicks, name, durationTicks);
    }

    @Override
    public void tick(Level level, Player player) {
        //NONE
    }

    @Override
    public SpellCastResult execute(Level level, Player player) {
        if (!level.isClientSide()) {
            int projectileCount = 6;

            for (int i = 0; i < projectileCount; i++) {
                WaterSpellEntity water = new WaterSpellEntity(level, player.getX(), player.getY(), player.getZ());
                water.setOwner(player);

                water.setMode(WaterSpellEntity.MODE_ORBIT);
                // Use the durationTicks from this spell instance
                water.setMaxLife((int) this.durationTicks);

                float startAngle = (float) (i * (Math.PI * 2 / projectileCount));
                water.setOrbitAngle(startAngle);

                level.addFreshEntity(water);
            }

            ActiveSpellManager.addSpell(player, this);
            return SpellCastResult.success();
        }
        return SpellCastResult.fail();
    }
}