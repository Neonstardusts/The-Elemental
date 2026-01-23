package com.teamneon.theelemental.client.model;

import com.teamneon.theelemental.client.renderer.CosmeticRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import static com.teamneon.theelemental.Theelemental.id;

public class WingCosmeticModel extends CosmeticModel {
    private static final Identifier TEXTURE = id("textures/entity/cosmetic/wings.png");
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public WingCosmeticModel(ModelPart root) {
        super(root);
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Y was 2.0F, moved to 0.0F to go UP 2 pixels.
        // Z-dimension changed from 1.0F to 0.0F to remove the "thickness" lip.

        // LEFT WING
        root.addOrReplaceChild("left_wing", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(0.0F, 0.0F, 0.0F, 12.0F, 16.0F, 0.0F),
                PartPose.offset(1.0F, 0.0F, 2.0F));

        // RIGHT WING (Mirrored for sagittal symmetry)
        root.addOrReplaceChild("right_wing", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(-12.0F, 0.0F, 0.0F, 12.0F, 16.0F, 0.0F),
                PartPose.offset(-1.0F, 0.0F, 2.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(CosmeticRenderState state) {
        // Flap math: Negative Y moves the wing backward (away from player face)
        float flap = Mth.sin(state.ageInTicks * 0.2f) * 0.3f;

        this.leftWing.yRot = -0.5f - flap;
        this.rightWing.yRot = 0.5f + flap;

        this.leftWing.zRot = 0.2f;
        this.rightWing.zRot = -0.2f;
    }

    @Override
    public Identifier getTexture() { return TEXTURE; }
}