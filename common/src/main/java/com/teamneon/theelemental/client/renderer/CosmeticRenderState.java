package com.teamneon.theelemental.client.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class CosmeticRenderState extends EntityRenderState {
    public float ageInTicks;
    public boolean isSlim;
    public int packedLight;
    public int element = 0;
    public int color = 0xFFFFFFFF;

    // Derived from AvatarRenderState logic
    public boolean isInAir;
    public float flapSpeed;
}