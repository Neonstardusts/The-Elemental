package com.teamneon.theelemental.item.property;


import com.teamneon.theelemental.store.ModComponents;
import com.teamneon.theelemental.store.RuneData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

//@OnlyIn(Dist.CLIENT)
public record SpellVariantProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<SpellVariantProperty> MAP_CODEC = MapCodec.unit(new SpellVariantProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        // We look directly at the stack's components; the owner doesn't matter for this
        RuneData data = stack.get(ModComponents.rune.value());

        // Logic: Return 0, 1, 2, or 3 based on spellId
        return data != null ? (float) (data.spellId() % 4) : 0.0f;
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
        return MAP_CODEC;
    }
}

