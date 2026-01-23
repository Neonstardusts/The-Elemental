package com.teamneon.theelemental.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamneon.theelemental.client.model.CosmeticModel;
import com.teamneon.theelemental.client.cosmetics.CosmeticRegistry;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.client.ClientElementalData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import java.awt.Color;

public class CosmeticRenderLayer<S extends AvatarRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {

    public CosmeticRenderLayer(RenderLayerParent<S, M> renderer, EntityRendererProvider.Context context) {
        super(renderer);
        // Ensure registry is initialized so models are baked
        CosmeticRegistry.init(context);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, S avatarState, float limbSwing, float limbSwingAmount) {
        // 1. Get the element ID for the entity currently being rendered
        int entityElement = ClientElementalData.getElementForEntity(avatarState.id);

        // 2. Identify which model key to pull from the registry
        String cosmeticId = switch (entityElement) {
            case 1 -> "wings";          // Element 1: Standard Wings
            case 2 -> "flutter_wings";  // Element 2: Flutter Wings
            case 3 -> "halo";           // Element 3: Halo
            default -> null;            // No model for other elements
        };

        // 3. Only proceed if we found a valid model for this element
        if (cosmeticId != null) {
            renderSingleCosmetic(cosmeticId, poseStack, nodeCollector, packedLight, avatarState, entityElement);
        }
    }

    private void renderSingleCosmetic(String cosmeticId, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, S avatarState, int entityElement) {
        CosmeticRegistry.CosmeticEntry entry = CosmeticRegistry.get(cosmeticId);
        if (entry == null) return;

        CosmeticModel model = entry.model();

        // Create a temporary state to pass animation data to the model
        CosmeticRenderState state = new CosmeticRenderState();
        state.ageInTicks = avatarState.ageInTicks;
        state.packedLight = packedLight;
        state.element = entityElement;

        // 1. Get Base Color from ElementRegistry (e.g., Fire = Red, Water = Blue)
        int baseColor = ElementRegistry.getColor(entityElement);

        // 2. Extract RGB components
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;

        // 3. Hue Shift logic
        // Convert to HSB so we can rotate the hue without losing saturation
        float[] hsb = Color.RGBtoHSB(r, g, b, null);

        // Shift hue slightly (+/- 5%) based on entity ID so different players look unique
        float hueShift = (float) (avatarState.id % 20) / 200f - 0.05f;
        float finalHue = (hsb[0] + hueShift + 1.0f) % 1.0f;

        // 4. Create fully saturated emissive tint (S=1.0, B=1.0)
        int playerTint = Color.HSBtoRGB(finalHue, 1.0f, 1.0f);

        // Scale based on entity ID to give players slightly different wing sizes
        float sizeScale = 0.8f + (Math.abs(avatarState.id % 41) / 100f);

        poseStack.pushPose();

        // Attach to specific body parts based on element type
        if (entityElement == 3) {
            // Halo attaches to the head
            this.getParentModel().head.translateAndRotate(poseStack);
            poseStack.translate(0, -0.4, 0); // Position above the scalp
        } else {
            // Wings attach to the torso
            this.getParentModel().body.translateAndRotate(poseStack);
        }

        poseStack.scale(sizeScale, sizeScale, sizeScale);

        // Update model rotations (flapping)
        model.setupAnim(state);

        // Submit to the render pipeline using an Emissive render type (glow-in-the-dark)
        nodeCollector.submitModel(
                model,
                state,
                poseStack,
                RenderTypes.entityTranslucentEmissive(model.getTexture(), false),
                packedLight,
                10,
                playerTint,
                null
        );

        poseStack.popPose();
    }

    /**
     * Retrieves the element for the specific entity currently being processed.
     */
    private int getElementForEntity(S avatarState) {
        // avatarState.id is the unique Minecraft Entity ID.
        // We use our static map in ClientElementalData to find the synced element for this ID.
        return ClientElementalData.getElementForEntity(avatarState.id);
    }
}