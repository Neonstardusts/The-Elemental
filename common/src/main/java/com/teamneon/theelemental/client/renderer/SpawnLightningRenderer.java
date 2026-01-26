package com.teamneon.theelemental.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.teamneon.theelemental.entity.SpawnLightningEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

public class SpawnLightningRenderer extends EntityRenderer<SpawnLightningEntity, SpawnLightningRenderState> {

    public SpawnLightningRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SpawnLightningRenderState createRenderState() {
        return new SpawnLightningRenderState();
    }

    @Override
    public void extractRenderState(SpawnLightningEntity entity, SpawnLightningRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.targetPos.set(entity.getTarget());
        state.entityId = entity.getId();
        state.age = entity.tickCount;
        state.renderFromSource = entity.shouldRenderFromSource(); // Extract boolean

        if (state.renderFromSource) {
            // Bolt starts at Player/Source
            Entity source = entity.level().getEntity(entity.getSourceId());
            if (source != null) {
                Vec3 p = source.getEyePosition(partialTick);
                state.sourcePos.set((float)p.x, (float)p.y, (float)p.z);
            }
        } else {
            // Bolt starts at the Cloud (Entity Position)
            double x = net.minecraft.util.Mth.lerp(partialTick, entity.xo, entity.getX());
            double y = net.minecraft.util.Mth.lerp(partialTick, entity.yo, entity.getY());
            double z = net.minecraft.util.Mth.lerp(partialTick, entity.zo, entity.getZ());
            state.sourcePos.set((float)x, (float)y, (float)z);
        }
    }

    @Override
    protected boolean affectedByCulling(SpawnLightningEntity entity) {
        // This tells the engine to ALWAYS run the submit() method,
        // even if the entity's hitbox is behind the player or off-camera.
        return false;
    }

    @Override
    public void submit(SpawnLightningRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        super.submit(state, poseStack, nodeCollector, cameraRenderState);

        nodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();

            // Calculate Start and End relative to the Entity position
            Vector3f relStart = new Vector3f(state.sourcePos).sub((float)state.x, (float)state.y, (float)state.z);
            Vector3f relEnd = new Vector3f(state.targetPos).sub((float)state.x, (float)state.y, (float)state.z);
            Vector3f fullPath = new Vector3f(relEnd).sub(relStart);

            // Change shape every 2 ticks
            long seed = (long) state.entityId + (state.age / 2);
            Random random = new Random(seed);

            // 1. Aqua Glow (Outer)
            renderBolt(consumer, matrix, relStart, fullPath, 0.12f, 0x55FFFF, 0.4f, random);

            // 2. White Core (Inner)
            random.setSeed(seed);
            renderBolt(consumer, matrix, relStart, fullPath, 0.04f, 0xFFFFFF, 0.0f, random);
        });
    }

    private void renderBolt(VertexConsumer consumer, Matrix4f matrix, Vector3f start, Vector3f path, float thickness, int color, float branchChance, Random random) {
        Vector3f currentPos = new Vector3f(start);
        int segments = 12;
        float jaggedness = 0.5f;

        for (int i = 1; i <= segments; i++) {
            float progress = (float) i / segments;
            Vector3f nextPos = new Vector3f(path).mul(progress).add(start);

            if (i < segments) {
                nextPos.add((random.nextFloat() - 0.5f) * jaggedness, (random.nextFloat() - 0.5f) * jaggedness, (random.nextFloat() - 0.5f) * jaggedness);
            }

            drawThickLine(consumer, matrix, currentPos, nextPos, thickness, color);

            if (random.nextFloat() < branchChance * 0.15f) {
                Vector3f branchEnd = new Vector3f(nextPos).add((random.nextFloat() - 0.5f) * 1.5f, (random.nextFloat() - 0.5f) * 1.5f, (random.nextFloat() - 0.5f) * 1.5f);
                drawThickLine(consumer, matrix, nextPos, branchEnd, thickness * 0.5f, color);
            }
            currentPos.set(nextPos);
        }
    }

    private void drawThickLine(VertexConsumer consumer, Matrix4f matrix, Vector3f s, Vector3f e, float width, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        for (float off = -width; off <= width; off += width) {
            consumer.addVertex(matrix, s.x, s.y + off, s.z).setColor(r, g, b, 255).setLight(15728880);
            consumer.addVertex(matrix, e.x, e.y + off, e.z).setColor(r, g, b, 255).setLight(15728880);
            consumer.addVertex(matrix, s.x + off, s.y, s.z).setColor(r, g, b, 255).setLight(15728880);
            consumer.addVertex(matrix, e.x + off, e.y, e.z).setColor(r, g, b, 255).setLight(15728880);
        }
    }
}