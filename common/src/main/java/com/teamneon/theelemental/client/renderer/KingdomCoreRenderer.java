package com.teamneon.theelemental.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.teamneon.theelemental.block.entity.KingdomCoreBlockEntity;
import com.teamneon.theelemental.helpers.ElementRegistry;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class KingdomCoreRenderer implements BlockEntityRenderer<KingdomCoreBlockEntity, KingdomCoreRenderState> {

    public KingdomCoreRenderer(BlockEntityRendererProvider.Context context) {
        // Nothing special needed
    }

    @Override
    public KingdomCoreRenderState createRenderState() {
        return new KingdomCoreRenderState();
    }

    @Override
    public void extractRenderState(KingdomCoreBlockEntity be, KingdomCoreRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);

        state.animationTime = be.getLevel() != null ? (float) Math.floorMod(be.getLevel().getGameTime(), 40) + partialTick : 0.0F;
        state.color = 0xFF000000 | ElementRegistry.getColor(be.getElement());
        state.worldPosition = be.getBlockPos();
        state.level = be.getLevel();
        state.radius = be.getRadius(); // ✅ copy radius from BE
    }


    @Override
    public void submit(KingdomCoreRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        // 1️⃣ Draw beacon beam
        BeaconRenderer.submitBeaconBeam(
                poseStack,
                collector,
                BeaconRenderer.BEAM_LOCATION,
                1.0F,                     // scale
                state.animationTime,
                0,                        // yOffset
                2048,                     // height
                state.color,
                0.35F,                    // inner radius
                0.4F                      // outer radius
        );

        // 2️⃣ Draw forcefield ring at player Y level
        collector.submitCustomGeometry(
                poseStack,
                net.minecraft.client.renderer.rendertype.RenderTypes.lines(),
                (pose, consumer) -> renderForcefieldRing(state, pose, consumer, cameraState)
        );
    }

    private static void renderForcefieldRing(KingdomCoreRenderState state, PoseStack.Pose pose, VertexConsumer consumer, CameraRenderState cameraState) {
        int segments = (int) state.radius + 8; // increase for smoother rings if needed

        float a = ((state.color >> 24) & 0xFF) / 255f;
        float r = ((state.color >> 16) & 0xFF) / 255f;
        float g = ((state.color >> 8) & 0xFF) / 255f;
        float b = (state.color & 0xFF) / 255f;

        a *= 0.6F + 0.4F * Mth.sin(state.animationTime * 0.15F);

        float radius = state.radius; // radius from the block entity

        // Track first and previous vertex
        float firstX = 0, firstY = 0, firstZ = 0;
        float prevX = 0, prevY = 0, prevZ = 0;

        for (int i = 0; i <= segments; i++) { // <= to loop back
            float angle = i * Mth.TWO_PI / segments;

            // Offset from core block
            float xOffset = Mth.cos(angle) * radius;
            float zOffset = Mth.sin(angle) * radius;

            // World coordinates of this vertex
            int worldX = state.worldPosition.getX() + Math.round(xOffset);
            int worldZ = state.worldPosition.getZ() + Math.round(zOffset);

            // Get height at this vertex
            float vertexY = state.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ) + 0.5F;
            float y = vertexY - state.worldPosition.getY(); // same offset as before

            // Local X/Z coordinates for rendering
            float x = xOffset + 0.5F;
            float z = zOffset + 0.5F;

            // Distance from camera
            Vec3 vertexPos = new Vec3(
                    state.worldPosition.getX() + x,
                    state.worldPosition.getY() + y,
                    state.worldPosition.getZ() + z
            );
            float distance = (float) cameraState.pos.distanceTo(vertexPos);

            // Scale line width based on distance (tweak 5f for desired thickness)
            float lineWidth = 100f / distance;

            if (i == 0) {
                firstX = x;
                firstY = y;
                firstZ = z;
            } else {
                consumer.addVertex(pose, prevX, prevY, prevZ)
                        .setColor(r, g, b, a)
                        .setNormal(pose, 0, 1, 0)
                        .setLineWidth(lineWidth);

                consumer.addVertex(pose, x, y, z)
                        .setColor(r, g, b, a)
                        .setNormal(pose, 0, 1, 0)
                        .setLineWidth(lineWidth);
            }

            prevX = x;
            prevY = y;
            prevZ = z;
        }

        // Connect last vertex back to first
        Vec3 firstVertexPos = new Vec3(
                state.worldPosition.getX() + firstX,
                state.worldPosition.getY() + firstY,
                state.worldPosition.getZ() + firstZ
        );
        float lineWidth = 100f / (float) cameraState.pos.distanceTo(firstVertexPos);

        consumer.addVertex(pose, prevX, prevY, prevZ)
                .setColor(r, g, b, a)
                .setNormal(pose, 0, 1, 0)
                .setLineWidth(lineWidth);

        consumer.addVertex(pose, firstX, firstY, firstZ)
                .setColor(r, g, b, a)
                .setNormal(pose, 0, 1, 0)
                .setLineWidth(lineWidth);
    }




    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
