package com.teamneon.theelemental.item.property;


import com.mojang.serialization.MapCodec;
import com.teamneon.theelemental.store.ModComponents;
import com.teamneon.theelemental.store.RuneData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

//@OnlyIn(Dist.CLIENT)
public record ElementIdProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<ElementIdProperty> MAP_CODEC = MapCodec.unit(new ElementIdProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        RuneData data = stack.get(ModComponents.rune.value());
        // Returns the element ID (e.g., 1 for Fire, 2 for Water)
        return data != null ? (float) data.elementId() : 0.0f;
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return MAP_CODEC;
    }
}

