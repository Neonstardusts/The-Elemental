package com.teamneon.theelemental.client.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class WaterSpellRenderState extends EntityRenderState {
    public float rotX;
    public float rotY;
    public float scale = 1.0f;

    public int packedLight;
    public int overlay = OverlayTexture.NO_OVERLAY;
    public int waterColor; // ADDED: Stores the ARGB tint from the biome
}