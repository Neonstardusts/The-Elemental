package com.teamneon.theelemental.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.teamneon.theelemental.client.renderer.CosmeticRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
// Fixed Imports: In 1.21.1, these are usually here:
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import java.util.function.Function;

public abstract class CosmeticModel extends Model<CosmeticRenderState> {
    // We don't need to store root locally if we can't override render.
    // The superclass Model handles it for us.

    public CosmeticModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
    }

    public CosmeticModel(ModelPart root, Function<Identifier, RenderType> renderType) {
        super(root, renderType);
    }

    // Every cosmetic must provide its own texture
    public abstract Identifier getTexture();

    // REMOVED: renderToBuffer override (it's final in your version)
}