package com.teamneon.theelemental.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class KingdomCoreRenderState extends BlockEntityRenderState {
    public float animationTime;
    public int color;
    public BlockPos worldPosition;
    public Level level;      // store level for surface queries
    public float radius;     // store radius for rendering the ring
}
