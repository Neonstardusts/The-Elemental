package com.teamneon.theelemental.item;

import com.mojang.blaze3d.platform.ScreenManager;
import com.teamneon.theelemental.client.tooltip.InfoTooltipComponentData;

import net.blay09.mods.balm.mixin.ScreenAccessor;
import net.blay09.mods.kuma.api.ScreenInputEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BlankRune extends Item {

    public BlankRune(Properties properties) {
        super(properties.stacksTo(1));
    }

}
