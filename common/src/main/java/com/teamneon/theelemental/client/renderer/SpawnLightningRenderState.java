package com.teamneon.theelemental.client.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.joml.Vector3f;

public class SpawnLightningRenderState extends EntityRenderState {
    public Vector3f targetPos = new Vector3f();
    public Vector3f sourcePos = new Vector3f();
    public int entityId;
    public int age;
}