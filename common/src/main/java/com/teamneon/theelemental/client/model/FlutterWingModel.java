package com.teamneon.theelemental.client.model;

import com.teamneon.theelemental.client.renderer.CosmeticRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import static com.teamneon.theelemental.Theelemental.id;

public class FlutterWingModel extends CosmeticModel {
    private static final Identifier TEXTURE = id("textures/entity/cosmetic/flutter_wings.png");
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public FlutterWingModel(ModelPart root) {
        super(root);
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Smaller boxes (8x10 instead of 12x16)
        root.addOrReplaceChild("left_wing", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 8.0F, 10.0F, 1.0F),
                PartPose.offset(1.0F, 4.0F, 2.0F));

        root.addOrReplaceChild("right_wing", CubeListBuilder.create()
                        .texOffs(0, 0).mirror().addBox(-8.0F, 0.0F, 0.0F, 8.0F, 10.0F, 1.0F),
                PartPose.offset(-1.0F, 4.0F, 2.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(CosmeticRenderState state) {
        // Fast flapping (multiplied by 0.6f instead of 0.2f)
        float flap = Mth.sin(state.ageInTicks * 0.6f) * 0.5f;
        this.leftWing.yRot = 0.3f + flap;
        this.rightWing.yRot = -0.3f - flap;
    }

    @Override
    public Identifier getTexture() { return TEXTURE; }
}