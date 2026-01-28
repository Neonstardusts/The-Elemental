package com.teamneon.theelemental.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamneon.theelemental.client.model.CosmeticModel;
import com.teamneon.theelemental.client.cosmetics.CosmeticRegistry;
import com.teamneon.theelemental.client.ClientElementalData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Pose;

public class CosmeticRenderLayer<S extends AvatarRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {

    // Track previous Y to detect vertical movement
    private double lastY = 0;

    public CosmeticRenderLayer(RenderLayerParent<S, M> renderer, EntityRendererProvider.Context context) {
        super(renderer);
        CosmeticRegistry.init(context);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, S avatarState, float limbSwing, float limbSwingAmount) {
        int entityElement = ClientElementalData.getElementForEntity(avatarState.id);
        String cosmeticId = switch (entityElement) {
            case 1 -> "wings";
            case 2 -> "flutter_wings";
            case 3 -> "halo";
            case 4 -> "flower_crown";
            default -> null;
        };

        if (cosmeticId != null) {
            renderSingleCosmetic(cosmeticId, poseStack, nodeCollector, packedLight, avatarState, entityElement);
        }

        // Update lastY at the end of the frame
        this.lastY = avatarState.y;
    }

    private void renderSingleCosmetic(String cosmeticId, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, S avatarState, int entityElement) {
        CosmeticRegistry.CosmeticEntry entry = CosmeticRegistry.get(cosmeticId);
        if (entry == null) return;

        CosmeticModel model = entry.model();
        CosmeticRenderState state = new CosmeticRenderState();

        state.ageInTicks = avatarState.ageInTicks;
        state.packedLight = packedLight;
        state.element = entityElement;

        // Detect vertical movement by comparing current Y to last frame's Y
        double deltaY = Math.abs(avatarState.y - lastY);
        boolean isMovingVertically = deltaY > 0.01;

        // Detection for Airborne state
        boolean isAirPose = avatarState.pose == Pose.FALL_FLYING || avatarState.isFallFlying || avatarState.pose == Pose.SWIMMING;
        state.isInAir = isAirPose || isMovingVertically;

        // --- FLAP SPEED TUNING ---
        // Reduced multipliers significantly to stop the "blur"
        if (state.isInAir) {
            // Flap speed scales slightly with how fast they are moving vertically
            state.flapSpeed = 0.3f + (float)(deltaY * 2.0);
        } else {
            // Horizontal flap is now much slower (0.4f multiplier)
            state.flapSpeed = avatarState.walkAnimationSpeed * 0.4f;
        }

        // Standard scaling logic
        float sizeScale = 0.8f + (Math.abs(avatarState.id % 41) / 100f);
        poseStack.pushPose();

        if (entityElement == 3) {
            this.getParentModel().head.translateAndRotate(poseStack);
            poseStack.translate(0, -0.4, 0);
        } else {
            this.getParentModel().body.translateAndRotate(poseStack);
        }

        poseStack.scale(sizeScale, sizeScale, sizeScale);
        model.setupAnim(state);

        nodeCollector.submitModel(
                model, state, poseStack,
                RenderTypes.entityNoOutline(model.getTexture()),
                packedLight, OverlayTexture.NO_OVERLAY, -1, null
        );

        poseStack.popPose();
    }
}