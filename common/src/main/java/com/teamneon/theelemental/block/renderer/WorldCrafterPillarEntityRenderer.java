package com.teamneon.theelemental.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.teamneon.theelemental.block.entity.WorldCrafterPillarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WorldCrafterPillarEntityRenderer implements BlockEntityRenderer<WorldCrafterPillarEntity, WorldCrafterPillarEntityRenderState> {
    private final ItemModelResolver itemModelManager;

    public WorldCrafterPillarEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelManager = context.itemModelResolver();
    }

    @Override
    public WorldCrafterPillarEntityRenderState createRenderState() {
        return new WorldCrafterPillarEntityRenderState();
    }

    @Override
    public void extractRenderState(WorldCrafterPillarEntity blockEntity, WorldCrafterPillarEntityRenderState state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);

        ItemStack stack = blockEntity.inventory.getItem(0);


        if (blockEntity.getLevel() != null) {
            state.rotation = (blockEntity.getLevel().getGameTime() + partialTick) * 4.0f;
            state.packedLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above());
        }

        // Replace the previous itemModelManager.updateForNonLiving call with this:
        this.itemModelManager.updateForNonLiving(
                state.itemRenderState,
                stack,
                ItemDisplayContext.GROUND,
                Minecraft.getInstance().player // Pass the level directly if the method allows
        );

    }

    @Override
    public void submit(WorldCrafterPillarEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        // DEBUG 3: Why is submission skipping?
        if (state.itemRenderState.isEmpty()) {
            // We only print this if the extraction debug found an item but this is empty
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5f, 1.25f, 0.5f);
        poseStack.scale(1.0f, 1.0f, 1.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));

        state.itemRenderState.submit(poseStack, collector, 15728880, 0, 0);

        poseStack.popPose();
    }
}