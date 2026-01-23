package com.teamneon.theelemental.client.cosmetics;

import com.teamneon.theelemental.client.model.CosmeticModel;
import com.teamneon.theelemental.client.model.ModModelLayers;
import com.teamneon.theelemental.client.model.WingCosmeticModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class CosmeticRegistry {
    private static final Map<String, CosmeticEntry> COSMETICS = new HashMap<>();

    public static void init(EntityRendererProvider.Context context) {
        // Register your cosmetics here
        register("wings", new CosmeticEntry(
                new WingCosmeticModel(context.bakeLayer(ModModelLayers.WINGS))
        ));

    }

    private static void register(String id, CosmeticEntry entry) {
        COSMETICS.put(id, entry);
    }

    public static CosmeticEntry get(String id) {
        return COSMETICS.get(id);
    }

    // A simple wrapper to hold the model and any extra metadata
    public record CosmeticEntry(CosmeticModel model) {}
}