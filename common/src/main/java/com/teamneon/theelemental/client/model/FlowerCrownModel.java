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

public class FlowerCrownModel extends CosmeticModel {
    private static final Identifier TEXTURE = id("textures/entity/cosmetic/flower_crown.png");
    private final ModelPart ring;

    public FlowerCrownModel(ModelPart root) {
        super(root);
        this.ring = root.getChild("ring");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Size 10.0f ensures we clear the 'Hat' layer of the player's skin.
        // Offset -5.0f centers a 10-unit wide box perfectly.
        root.addOrReplaceChild("ring",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0f, -11.0f, -5.0f, 10.0f, 4.0f, 10.0f),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 32); // Increased width to 64 for better UV room
    }

    @Override
    public void setupAnim(CosmeticRenderState state) {

    }

    @Override
    public Identifier getTexture() { return TEXTURE; }
}