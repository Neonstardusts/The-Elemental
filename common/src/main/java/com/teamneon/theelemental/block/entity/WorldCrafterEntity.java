package com.teamneon.theelemental.block.entity;

import com.teamneon.theelemental.Theelemental;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class WorldCrafterEntity extends BlockEntity {

    public final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            WorldCrafterEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private float rotation;

    public WorldCrafterEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WORLDCRAFTER_BE.asSupplier().get(), pos, blockState);
    }

    public float getRenderingRotation() {
        rotation += 0.5f;
        if (rotation >= 360) {
            rotation = 0;
        }
        return rotation;
    }

    public void clearContents() {
        this.inventory.clearContent();
    }

    public void drops() {
        if (this.level != null) {
            Containers.dropContents(this.level, this.worldPosition, this.inventory);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        drops();
        super.preRemoveSideEffects(pos, state);
    }

    // --- BALM NBT SYSTEM ---

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ValueOutput.ValueOutputList list = output.childrenList("Inventory");
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                ValueOutput slotTag = list.addChild();
                slotTag.putInt("Slot", i);

                // Use the ItemStack Codec to save EVERYTHING (ID, Count, and Components)
                slotTag.store("Item", ItemStack.CODEC, stack);
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.inventory.clearContent();
        input.childrenList("Inventory").ifPresent(list -> {
            for (ValueInput slotTag : list) {
                int slot = slotTag.getIntOr("Slot", 0);

                // Read the entire stack using the Codec
                slotTag.read("Item", ItemStack.CODEC).ifPresent(stack -> {
                    if (slot >= 0 && slot < inventory.getContainerSize()) {
                        inventory.setItem(slot, stack);
                    }
                });
            }
        });
    }

    // --- RENDER SYNC & MENU ---

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // Sends the current state to the client
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Use BALM's built-in tag creation helper
        return this.saveCustomOnly(registries);
    }

}