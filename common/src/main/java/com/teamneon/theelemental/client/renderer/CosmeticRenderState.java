package com.teamneon.theelemental.client.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class CosmeticRenderState extends EntityRenderState {
    public float ageInTicks;
    public boolean isSlim;
    public int packedLight;

    // --- Added for Customization ---
    public int element = 0; // The element ID from ClientElementalData
}