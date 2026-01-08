package com.teamneon.theelemental.block;

import com.mojang.serialization.MapCodec;
import com.teamneon.theelemental.block.entity.KingdomCoreBlockEntity;
import com.teamneon.theelemental.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class KingdomCoreBlock extends BaseEntityBlock implements EntityBlock {

    public static final IntegerProperty ELEMENTCore =
            IntegerProperty.create("elementcore", 0, 9);

    public KingdomCoreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(ELEMENTCore, 0)
        );
    }

    public static final MapCodec<KingdomCoreBlock> CODEC =
            simpleCodec(KingdomCoreBlock::new);


    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ELEMENTCore);
    }

    @Override
    public KingdomCoreBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KingdomCoreBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return KingdomCoreInteraction.handleWithItem(world, pos, player, hand, stack, hitResult);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof KingdomCoreBlockEntity core) {
            core.delete(); // delete safely handles saved data + block removal
        }
        // Return the state (usually the same as super)
        return super.playerWillDestroy(level, pos, state, player);
    }


    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        if (level.isClientSide()) return;

        if (placer != null && level.getBlockEntity(pos) instanceof KingdomCoreBlockEntity core) {
            core.setOwner(placer.getUUID());
        }
    }


    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return level.isClientSide() ? null :
                createTickerHelper(
                        type,
                        ModBlockEntities.KINGDOM_CORE_REG.asSupplier().get(),
                        KingdomCoreBlockEntity::tick
                );
    }


}
