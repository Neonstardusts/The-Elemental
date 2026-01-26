package com.teamneon.theelemental.item;

import com.teamneon.theelemental.helpers.UtilityHelper;
import com.teamneon.theelemental.store.ManaData;
import com.teamneon.theelemental.store.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class LevelCrystal extends Item {

    public LevelCrystal(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        // 1. Calculate the current fade color
        // 2000 is the speed in ms (2 seconds per loop)
        int color = UtilityHelper.getMultiFadeColor(new int[]{0xc7a8ff, 0xFFFFFF}, 2000);
        // 2. Return the name with the dynamic color applied
        return Component.translatable("item.theelemental.level_crystal")
                .withStyle(style -> style.withColor(color));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {

        // 2. Add Stored Mana (Always present)
        tooltipAdder.accept(Component.literal("Kingdom Upgrade")
                .withStyle(style -> style.withColor(0x895ade)));

        // 3. Conditional replacement
        if (!Minecraft.getInstance().hasShiftDown()) {
            // Standard prompt
            tooltipAdder.accept(Component.literal("[Shift for ℹ]").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            // Replacement when shifting
            tooltipAdder.accept(Component.literal("ℹ Information").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.UNDERLINE));

            tooltipAdder.accept(Component.literal("• Use on kingdom core").withStyle(ChatFormatting.DARK_GRAY));
            tooltipAdder.accept(Component.literal("• Increases your level and maximum mana").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}