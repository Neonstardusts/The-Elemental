package com.teamneon.theelemental.client.model;

import com.teamneon.theelemental.client.renderer.WaterSpellRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class WaterSpellModel extends Model<WaterSpellRenderState> {

    public WaterSpellModel(ModelPart root) {
        // We pass the root part and the standard render type function
        super(root, RenderTypes::entityTranslucent);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("main",
                CubeListBuilder.create()
                        .texOffs(0, 0) // This is the anchor point on the texture
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);

        // This MUST match your .png dimensions exactly
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(WaterSpellRenderState renderState) {
        super.setupAnim(renderState);
        // ageInTicks is built into EntityRenderState
        float rotation = renderState.ageInTicks * 0.1f;
        this.root().getChild("main").yRot = rotation;
        this.root().getChild("main").xRot = rotation * 0.5f;
    }


}