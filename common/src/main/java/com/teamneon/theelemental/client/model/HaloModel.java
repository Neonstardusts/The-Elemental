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

        // Base height to clear the player's head
        float pivotHeight = -36.0F;

        PartDefinition ring = root.addOrReplaceChild("ring", CubeListBuilder.create(),
                PartPose.offset(0.0F, pivotHeight, 0.0F));

        // --- WHITE INNER RING (The Core) ---
        // Standard positive dimensions
        CubeListBuilder whiteBars = CubeListBuilder.create().texOffs(0, 0);
        ring.addOrReplaceChild("white_n", whiteBars.addBox(-4.0F, 0.0F, -4.0F, 8.0F, 1.0F, 1.0F), PartPose.ZERO);
        ring.addOrReplaceChild("white_s", whiteBars.addBox(-4.0F, 0.0F, 3.0F, 8.0F, 1.0F, 1.0F), PartPose.ZERO);
        ring.addOrReplaceChild("white_e", whiteBars.addBox(-4.0F, 0.0F, -3.0F, 1.0F, 1.0F, 6.0F), PartPose.ZERO);
        ring.addOrReplaceChild("white_w", whiteBars.addBox(3.0F, 0.0F, -3.0F, 1.0F, 1.0F, 6.0F), PartPose.ZERO);

        // --- INVERTED LIME OUTER RING ---
        // We use negative dimensions (-9.0F, -1.2F, -1.0F) to flip the faces.
        // We offset the starting point to compensate for the negative growth direction.
        CubeListBuilder limeBars = CubeListBuilder.create().texOffs(0, 8);

        // North bar: starts at 4.5, ends at -4.5 because of -9.0 width
        ring.addOrReplaceChild("lime_n", limeBars.addBox(4.5F, 1.1F, -3.5F, -9.0F, -1.2F, -1.0F), PartPose.ZERO);
        // South bar
        ring.addOrReplaceChild("lime_s", limeBars.addBox(4.5F, 1.1F, 4.5F, -9.0F, -1.2F, -1.0F), PartPose.ZERO);
        // East bar
        ring.addOrReplaceChild("lime_e", limeBars.addBox(-3.5F, 1.1F, 3.5F, -1.0F, -1.2F, -7.0F), PartPose.ZERO);
        // West bar
        ring.addOrReplaceChild("lime_w", limeBars.addBox(4.5F, 1.1F, 3.5F, -1.0F, -1.2F, -7.0F), PartPose.ZERO);

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(CosmeticRenderState state) {
        // Subtle floating and rotating effect
        this.ring.y = -8 + Mth.sin(state.ageInTicks * 0.1f) * 0.75f;
        this.ring.yRot = state.ageInTicks * 0.02f;
    }

    @Override
    public Identifier getTexture() { return TEXTURE; }
}