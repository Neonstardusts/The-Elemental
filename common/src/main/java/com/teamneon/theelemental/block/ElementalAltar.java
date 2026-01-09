package com.teamneon.theelemental.block;

import com.mojang.serialization.MapCodec;
import com.teamneon.theelemental.block.entity.ElementalAltarBlockEntity;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.menu.ElementChooserMenu;
import com.teamneon.theelemental.menu.ModMenuTypes;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.world.BalmMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ElementalAltar extends BaseEntityBlock {
    public static final MapCodec<ElementalAltar> CODEC = simpleCodec(ElementalAltar::new);

    public ElementalAltar(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElementalAltarBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Use MODEL so your custom JSON model still shows up
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        var data = ElementalDataHandler.get(player);
        int currentElement = data.getElement();

        // Sneak to reset element TOBEREMOVED FOR DEBUG
        if (currentElement > 0 && player.isShiftKeyDown()) {
            data.setElement(0);
            ElementalDataHandler.syncToClient(player);
            ElementalDataHandler.save(player);

            player.displayClientMessage(
                    Component.literal("Element cleared!").withStyle(style -> style.withBold(true)),
                    true
            );

            return InteractionResult.CONSUME;
        }

        if (currentElement > 0 && !player.isShiftKeyDown()) {

            player.displayClientMessage(
                    Component.literal("You are already an elemental creature!"),
                    true
            );

            return InteractionResult.CONSUME;
        }

        // Open ElementChooser if element == 0
        if (currentElement == 0 && !player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {

                BalmMenuProvider<ModMenuTypes.ElementChooserData> chooserProvider = new BalmMenuProvider<>() {
                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("container.theelemental.element_chooser");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player p) {
                        return new ElementChooserMenu(windowId, inv, p);
                    }

                    @Override
                    public ModMenuTypes.ElementChooserData getScreenOpeningData(ServerPlayer player) {
                        return new ModMenuTypes.ElementChooserData();
                    }

                    @Override
                    public StreamCodec<RegistryFriendlyByteBuf, ModMenuTypes.ElementChooserData> getScreenStreamCodec() {
                        return ModMenuTypes.ElementChooserData.STREAM_CODEC;
                    }
                };

                Balm.networking().openMenu(serverPlayer, chooserProvider);
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }


}

