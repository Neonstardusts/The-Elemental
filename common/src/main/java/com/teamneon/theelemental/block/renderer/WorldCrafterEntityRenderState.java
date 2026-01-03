package com.teamneon.theelemental.block.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class WorldCrafterEntityRenderState extends BlockEntityRenderState {
    // This MUST be public and MUST be final so the object reference never changes
    public final ItemStackRenderState itemRenderState = new ItemStackRenderState();

    public float rotation;
    public int packedLight;
}