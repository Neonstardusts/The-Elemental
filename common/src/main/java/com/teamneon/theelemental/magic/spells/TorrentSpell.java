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


    public TorrentSpell(int manaCost, int cooldownTicks,  String name, long durationTicks) {
        super(manaCost, cooldownTicks, name, durationTicks);
    }

    @Override
    public void tick(Level level, Player player) {
        // this is called after the spell si exectueed every tick for its duraiton so logic here
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