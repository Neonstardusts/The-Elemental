package com.teamneon.theelemental.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamneon.theelemental.client.model.CosmeticModel;
import com.teamneon.theelemental.client.cosmetics.CosmeticRegistry;
import com.teamneon.theelemental.client.renderer.CosmeticRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CosmeticRenderLayer<S extends AvatarRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {

    public CosmeticRenderLayer(RenderLayerParent<S, M> renderer, EntityRendererProvider.Context context) {
        super(renderer);
        // BAKE THE WINGS RIGHT HERE
        // This takes the "blueprint" from your ModelLayers and builds the 3D object
        CosmeticRegistry.init(context);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, S avatarState, float limbSwing, float limbSwingAmount) {
        // Now avatarState is of type S, which is safe
        if (avatarState.ageInTicks % 200 == 0) {
            System.out.println("[Elemental Debug] Layer Submitting: Found Avatar " + avatarState.entityType);
        }
        renderSingleCosmetic("wings", poseStack, nodeCollector, packedLight, avatarState);
    }

    private void renderSingleCosmetic(String cosmeticId, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, S avatarState) {
        CosmeticRegistry.CosmeticEntry entry = CosmeticRegistry.get(cosmeticId);
        if (entry == null) {
            if (avatarState.ageInTicks % 200 == 0) {
                System.err.println("[Elemental Debug] Registry Error: ID '" + cosmeticId + "' not found!");
            }
            return;
        }



        CosmeticModel model = entry.model();
        CosmeticRenderState state = new CosmeticRenderState();
        state.ageInTicks = avatarState.ageInTicks;
        state.packedLight = packedLight;

        poseStack.pushPose();

        // Use the model provided by the generic M type
        this.getParentModel().body.translateAndRotate(poseStack);

        model.setupAnim(state);

        nodeCollector.submitModel(
                model,
                state,
                poseStack,
                RenderTypes.entityTranslucent(model.getTexture(), false),
                packedLight,
                10,
                -1,
                null
        );

        poseStack.scale(1.1f, 1.1f, 1.1f);

        poseStack.popPose();



    }
}