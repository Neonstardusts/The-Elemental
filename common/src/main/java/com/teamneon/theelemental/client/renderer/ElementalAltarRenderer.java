package com.teamneon.theelemental.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.teamneon.theelemental.block.entity.ElementalAltarBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ElementalAltarRenderer implements BlockEntityRenderer<ElementalAltarBlockEntity, ElementalAltarRenderState> {

    private static final Identifier ICON_TEXTURE = Identifier.fromNamespaceAndPath("theelemental", "textures/entity/altar_rune.png");

    public ElementalAltarRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public ElementalAltarRenderState createRenderState() {
        return new ElementalAltarRenderState();
    }

    @Override
    public void extractRenderState(ElementalAltarBlockEntity be, ElementalAltarRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);

        if (be.getLevel() != null) {
            float gameTime = be.getLevel().getGameTime() + partialTick;
            state.animationTime = (float) Math.floorMod(be.getLevel().getGameTime(), 40) + partialTick;
            state.rotation = gameTime * 2.5F;
        }

        state.shouldRenderBeam = true;
        state.beamColor = 0xFFFFFFFF;
    }

    @Override
    public void submit(ElementalAltarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {

        // 1. Render Beam
        if (state.shouldRenderBeam) {
            BeaconRenderer.submitBeaconBeam(
                    poseStack, collector, BeaconRenderer.BEAM_LOCATION, 1.0F,
                    state.animationTime, 0, 2048, state.beamColor, 0.35F, 0.4F
            );
        }

        // 2. Render Flat Rotating Icon
        poseStack.pushPose();

        // INCREASED HEIGHT: Change 1.1 to 1.5 (or higher if you want it to float more)
        poseStack.translate(0.5, 2, 0.5);

        // Spin around the Y-axis
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));

        // INCREASED SIZE: Changed from 0.625F to 1.0F
        // Since the quad vertices are -1.0 to 1.0 (total width of 2.0),
        // a scale of 1.0F makes the rune exactly 2 blocks wide.
        poseStack.scale(1.3F, 1.3F, 1.3F);

        collector.submitCustomGeometry(
                poseStack,
                RenderTypes.entityTranslucent(ICON_TEXTURE),
                (pose, consumer) -> renderFlatIconQuad(pose, consumer)
        );

        poseStack.popPose();
    }

    private static void renderFlatIconQuad(PoseStack.Pose pose, VertexConsumer consumer) {
        Matrix4f matrix = pose.pose();
        int light = 15728880;
        int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

        // TOP FACE - Moved up by 0.001 to prevent Z-tearing with the bottom face
        float topY = 0.001f;
        consumer.addVertex(matrix, -1.0f, topY, -1.0f).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix,  1.0f, topY, -1.0f).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix,  1.0f, topY,  1.0f).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, -1.0f, topY,  1.0f).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);

        // BOTTOM FACE - Moved down by 0.001
        float bottomY = -0.001f;
        consumer.addVertex(matrix, -1.0f, bottomY,  1.0f).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        consumer.addVertex(matrix,  1.0f, bottomY,  1.0f).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        consumer.addVertex(matrix,  1.0f, bottomY, -1.0f).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
        consumer.addVertex(matrix, -1.0f, bottomY, -1.0f).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(pose, 0, -1, 0);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}