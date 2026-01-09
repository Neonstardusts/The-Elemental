package com.teamneon.theelemental.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ElementalAltarBlockEntity extends BlockEntity {

    public ElementalAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELEMENTAL_ALTAR_REG.asSupplier().get(), pos, state);
    }

}