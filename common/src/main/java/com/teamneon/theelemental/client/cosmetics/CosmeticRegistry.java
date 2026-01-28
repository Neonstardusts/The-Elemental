package com.teamneon.theelemental.client.cosmetics;

import com.teamneon.theelemental.client.model.*;
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

        register("flutter_wings", new CosmeticEntry(
                new FlutterWingModel(context.bakeLayer(ModModelLayers.FLUTTER_WINGS))
        ));

        register("halo", new CosmeticEntry(
                new HaloModel(context.bakeLayer(ModModelLayers.HALO))
        ));

        register("flower_crown", new CosmeticEntry(
                new FlowerCrownModel(context.bakeLayer(ModModelLayers.FLOWER_CROWN))
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