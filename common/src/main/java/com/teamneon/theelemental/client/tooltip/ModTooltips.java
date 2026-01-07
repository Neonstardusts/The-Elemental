package com.teamneon.theelemental.client.tooltip;

import com.teamneon.theelemental.client.tooltip.RuneRecipeTooltipComponent;
import com.teamneon.theelemental.client.tooltip.RuneRecipeTooltipComponentData;
import net.blay09.mods.balm.client.BalmClientTooltipComponentRegistrar;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class ModTooltips {
    public static void initialize(BalmClientTooltipComponentRegistrar registrar) {
        registrar.register(
                RuneRecipeTooltipComponentData.class, // server/common TooltipComponent
                RuneRecipeTooltipComponent::new       // factory to client render component
        );

        registrar.register(
                SpellIconTooltipComponentData.class, // server/common TooltipComponent
                SpellIconTooltipComponent::new       // factory to client render component
        );

        registrar.register(
                InfoTooltipComponentData.class, // server/common TooltipComponent
                InfoTooltipComponent::new       // factory to client render component
        );

    }
}
