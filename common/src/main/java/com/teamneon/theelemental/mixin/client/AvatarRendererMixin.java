package com.teamneon.theelemental.mixin.client;

import com.teamneon.theelemental.client.renderer.CosmeticRenderLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin<T extends LivingEntity, S extends AvatarRenderState, M extends HumanoidModel<S>>
        extends LivingEntityRenderer<T, S, M> {

    protected AvatarRendererMixin(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    // UPDATED: Parameters changed to (Context, boolean, CallbackInfo)
    @Inject(method = "<init>", at = @At("TAIL"))
    private void theelemental$addCosmeticLayer(EntityRendererProvider.Context context, boolean useSlimModel, CallbackInfo ci) {
        // We pass 'context' into the Layer so it can bake the wings
        this.addLayer(new CosmeticRenderLayer<>((RenderLayerParent<S, M>) (Object) this, context));
    }
}