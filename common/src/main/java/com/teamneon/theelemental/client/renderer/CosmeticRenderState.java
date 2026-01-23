package com.teamneon.theelemental.client.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class CosmeticRenderState extends EntityRenderState {
    public float ageInTicks;
    public boolean isSlim;
    public int packedLight;

    // You can add more variables here that models might need,
    // like 'isFlying' or 'isCrouching'
}