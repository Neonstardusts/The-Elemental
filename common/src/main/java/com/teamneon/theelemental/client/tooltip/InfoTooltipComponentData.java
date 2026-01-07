package com.teamneon.theelemental.client.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public record InfoTooltipComponentData(
        List<String> texturePaths, // relative paths like "spell_icons/1.png"
        List<Component> lines
) implements TooltipComponent {

}
