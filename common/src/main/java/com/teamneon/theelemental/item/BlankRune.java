package com.teamneon.theelemental.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class BlankRune extends Item {

    public BlankRune(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        tooltipAdder.accept(Component.literal("An elemental item ready to transform to any element, use at a kingdom heart to research a recipe.").withStyle(ChatFormatting.DARK_GRAY));

    }
}
