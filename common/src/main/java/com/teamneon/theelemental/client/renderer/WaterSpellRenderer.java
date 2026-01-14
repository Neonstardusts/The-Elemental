package com.teamneon.theelemental.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamneon.theelemental.client.model.ModModelLayers;
import com.teamneon.theelemental.client.model.WaterSpellModel;
import com.teamneon.theelemental.entity.WaterSpellEntity;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;

import static com.teamneon.theelemental.Theelemental.id;

public class WaterSpellRenderer extends EntityRenderer<WaterSpellEntity, WaterSpellRenderState> {
    private static final Identifier TEXTURE = id("textures/entity/water_spell.png");
    private final WaterSpellModel model;

    public WaterSpellRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WaterSpellModel(context.bakeLayer(ModModelLayers.WATER_SPELL));
    }

    @Override
    public WaterSpellRenderState createRenderState() {
        return new WaterSpellRenderState();
    }

    @Override
    public void extractRenderState(WaterSpellEntity entity, WaterSpellRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.rotX = entity.getXRot();
        state.rotY = entity.getYRot();
        state.scale = 0.8f;
        state.packedLight = this.getPackedLightCoords(entity, partialTick);

        // ADDED: Gets the smoothed water color from the biome at the entity's position
        state.waterColor = BiomeColors.getAverageWaterColor(entity.level(), entity.blockPosition());
    }

    @Override
    public void submit(WaterSpellRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        super.submit(state, poseStack, nodeCollector, cameraRenderState);

        poseStack.pushPose();
        poseStack.scale(state.scale, state.scale, state.scale);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(state.rotY));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-state.rotX));

        nodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityTranslucent(TEXTURE, false),
                state.packedLight,
                state.overlay,
                state.waterColor, // CHANGED: Replaced -1 with state.waterColor
                null
        );

        poseStack.popPose();
    }
}