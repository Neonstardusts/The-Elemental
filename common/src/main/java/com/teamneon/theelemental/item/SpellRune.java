package com.teamneon.theelemental.item;

import com.teamneon.theelemental.client.tooltip.SpellIconTooltipComponentData;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.store.ModComponents;
import com.teamneon.theelemental.store.RuneData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.Optional;
import java.util.function.Consumer;

import static com.teamneon.theelemental.helpers.UtilityHelper.*;

public class SpellRune extends Item {

    public SpellRune(Properties properties) {

        super(properties
                .stacksTo(1)
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        // Get the rune data
        RuneData runeData = stack.get(ModComponents.rune.value());
        if (runeData == null) runeData = RuneData.EMPTY;

        int color = ElementRegistry.getColor(runeData.elementId());
        float hueAmount = randomFromStringRange(runeData.spellName() + "AAAA", -0.075f, 0.075f);
        int color2 = hueShiftColor(color, hueAmount);

        tooltipAdder.accept(Component.empty()
                .append(getGradientComponent((runeData.spellName()), (color), (color2)))
                .append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(toSmallCaps(ElementRegistry.getName(runeData.elementId()) + " spell")).withStyle(style -> style.withColor(color)))
        );

        tooltipAdder.accept(Component.empty());

        tooltipAdder.accept(Component.empty()
                .append(Component.literal("          ★").withStyle(style -> style.withColor(0x6f9eb3)))
                .append(Component.literal(" ᴍᴀɴᴀ ᴄᴏꜱᴛ →").withStyle(style -> style.withColor(0x8a8a8a)))
                .append(Component.literal(" "+runeData.manaCost()).withStyle(style -> style.withColor(0x6f9eb3)))

        );

        double cooldownSeconds = Math.round(runeData.cooldown() / 20.0 * 10) / 10.0;
        tooltipAdder.accept(Component.empty()
                .append(Component.literal("          ⌚").withStyle(style -> style.withColor(0xc7bb83)))
                .append(Component.literal(" ᴄᴏᴏʟᴅᴏᴡɴ →").withStyle(style -> style.withColor(0x8a8a8a)))
                .append(Component.literal(" "+cooldownSeconds+"s").withStyle(style -> style.withColor(0xc7bb83)))

        );

        double durationSeconds = Math.round(runeData.durationTicks() / 20.0 * 10) / 10.0;
        if (runeData.durationTicks() > 0) {
            tooltipAdder.accept(Component.empty()
                    .append(Component.literal("          ♒").withStyle(style -> style.withColor(0xb36fad)))
                    .append(Component.literal(" ᴅᴜʀᴀᴛɪᴏɴ →").withStyle(style -> style.withColor(0x8a8a8a)))
                    .append(Component.literal(" "+durationSeconds+"s").withStyle(style -> style.withColor(0xb36fad)))

            );
        }

        tooltipAdder.accept(Component.empty());

        tooltipAdder.accept(Component.literal("ᴅᴇꜱᴄʀɪᴘᴛɪᴏɴ: ").withStyle(style -> style.withColor(0x8a8a8a)));
        tooltipAdder.accept(Component.literal(runeData.description()).withStyle(ChatFormatting.DARK_GRAY));

    }


    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        RuneData runeData = stack.get(ModComponents.rune.value());
        if (runeData == null || runeData.spellId() == 0) return Optional.empty();

        return Optional.of(new SpellIconTooltipComponentData(runeData.spellId()));
    }
}
