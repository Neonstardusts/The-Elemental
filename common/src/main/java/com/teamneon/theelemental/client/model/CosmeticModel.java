package com.teamneon.theelemental.client.model;

import com.teamneon.theelemental.client.renderer.CosmeticRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import java.util.function.Function;

public abstract class CosmeticModel extends Model<CosmeticRenderState> {

    public CosmeticModel(ModelPart root) {
        // Default to translucent, but allow overrides
        super(root, RenderTypes::entityTranslucent);
    }

    public CosmeticModel(ModelPart root, Function<Identifier, RenderType> renderType) {
        super(root, renderType);
    }

    // Every cosmetic must provide its own texture
    public abstract Identifier getTexture();
}