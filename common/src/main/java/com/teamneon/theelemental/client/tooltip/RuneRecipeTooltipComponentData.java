package com.teamneon.theelemental.client.tooltip;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public record RuneRecipeTooltipComponentData(List<ItemStack> recipeItems) implements TooltipComponent, ObjectInfo {

    public static final MapCodec<RuneRecipeTooltipComponentData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ItemStack.CODEC.listOf().fieldOf("items").forGetter(RuneRecipeTooltipComponentData::recipeItems)
    ).apply(inst, RuneRecipeTooltipComponentData::new));

    @Override
    public MapCodec<? extends ObjectInfo> codec() { return CODEC; }

    @Override
    public String description() { return "Recipe Items"; }

    @Override
    public FontDescription fontDescription() {
        return new FontDescription() {
            @Override
            public String toString() { return "Recipe Items"; }
        };
    }
}