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

public class HaloModel extends CosmeticModel {
    private static final Identifier TEXTURE = id("textures/entity/cosmetic/halo.png");
    private final ModelPart ring;

    public HaloModel(ModelPart root) {
        super(root);
        this.ring = root.getChild("ring");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // A flat square ring above the head
        root.addOrReplaceChild("ring", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 1.0F, 8.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(CosmeticRenderState state) {
        // Subtle floating and rotating effect
        this.ring.y = Mth.sin(state.ageInTicks * 0.1f) * 1.5f;
        this.ring.yRot = state.ageInTicks * 0.05f;
    }

    @Override
    public Identifier getTexture() { return TEXTURE; }
}