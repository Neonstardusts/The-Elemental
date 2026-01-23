package com.teamneon.theelemental.client.model;

import com.teamneon.theelemental.client.renderer.CosmeticRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
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

        float verticalPos = -2.0F;

        root.addOrReplaceChild("left_wing", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(0.0F, 0.0F, 0.0F, 14.0F, 10.0F, 0.0F),
                PartPose.offset(1.0F, verticalPos, 2.0F));

        root.addOrReplaceChild("right_wing", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(-14.0F, 0.0F, 0.0F, 14.0F, 10.0F, 0.0F),
                PartPose.offset(-1.0F, verticalPos, 2.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(CosmeticRenderState state) {
        if (state.isInAir || state.flapSpeed > 0.01f) {

            // 1. LIMIT THE SPEED (The "Speed Ceiling")
            // We clamp flapSpeed so it never exceeds a reasonable value (0.3).
            // This stops the "blur" effect during high-velocity falls or sprints.
            float limitedFlapSpeed = Math.min(state.flapSpeed, 0.3f);

            // 2. SLOW DOWN THE BASE MATH
            // We reduce the multipliers significantly.
            // 0.1f is a calm base, and adding a small fraction of movement speed.
            float frequency = 0.3f + (limitedFlapSpeed * 0.2f);

            // 3. RENDER THE FLAP
            float flap = Mth.sin(state.ageInTicks * frequency) * 0.5f;

            this.leftWing.yRot = -0.5f - flap;
            this.rightWing.yRot = 0.5f + flap;

            this.leftWing.zRot = 0.25f;
            this.rightWing.zRot = -0.25f;
        } else {
            // IDLE: Folded and breathing slowly
            float idleSway = Mth.sin(state.ageInTicks * 0.04f) * 0.03f;

            this.leftWing.yRot = -0.2f - idleSway;
            this.rightWing.yRot = 0.2f + idleSway;
            this.leftWing.zRot = 0.12f;
            this.rightWing.zRot = -0.12f;
        }
    }

    @Override
    public Identifier getTexture() { return TEXTURE; }
}