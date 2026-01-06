package com.teamneon.theelemental.item;

import com.teamneon.theelemental.client.tooltip.RuneRecipeTooltipComponentData;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.store.ModComponents;
import com.teamneon.theelemental.store.RuneData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.teamneon.theelemental.helpers.UtilityHelper.*;

public class ElementRune extends Item {

    public ElementRune(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        RuneData data = stack.get(ModComponents.rune.value());
        if (data == null) {
            return super.getName(stack);
        }

        return Component.translatable("item.theelemental.broken_prefix")
                .append(" ")
                .append(Component.translatable(
                        switch (data.elementId()) {
                            case 1 -> "item.theelemental.fire_rune";
                            case 2 -> "item.theelemental.light_scroll";
                            case 3 -> "item.theelemental.sorcery_tome";
                            case 4 -> "item.theelemental.earth_tablet";
                            case 5 -> "item.theelemental.storm_core";
                            case 6 -> "item.theelemental.ocean_conch";
                            case 7 -> "item.theelemental.dark_codex";
                            case 8 -> "item.theelemental.warped_shard";
                            case 9 -> "item.theelemental.wind_feather";
                            default -> "item.theelemental.unknown";
                        }
                ))
                .withStyle(ChatFormatting.GRAY);

    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        RuneData runeData = stack.get(ModComponents.rune.value());
        if (runeData == null) runeData = RuneData.EMPTY;

        int color = ElementRegistry.getColor(runeData.elementId());
        float hueAmount = randomFromStringRange(runeData.spellName() + "AAAA", -0.075f, 0.075f);
        int color2 = hueShiftColor(color, hueAmount);



        tooltipAdder.accept(Component.empty()
                .append(getGradientComponent((runeData.spellName()), (color), (color2)))
                .append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(toSmallCaps(ElementRegistry.getName(runeData.elementId()) + " spell")).withStyle(style -> style.withColor(color))));

        tooltipAdder.accept(Component.empty());

        // 1. Top instruction
        tooltipAdder.accept(Component.literal("→ ʀɪᴛᴜᴀʟ ɪɴᴄᴏᴍᴘʟᴇᴛᴇ:").withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(Component.literal("ᴏꜰꜰᴇʀ ᴛʜᴇ ʀᴇᴀɢᴇɴᴛꜱ ʙᴇʟᴏᴡ ᴀᴛ ᴛʜᴇ ᴡᴏʀʟᴅ ᴀʟᴛᴀʀ ᴛᴏ ᴍᴀɴɪꜰᴇꜱᴛ ᴛʜᴇ ꜱᴘᴇʟʟ.").withStyle(ChatFormatting.DARK_GRAY));

        // 2. A blank line for spacing
        tooltipAdder.accept(Component.empty());

        // 3. The header for the items
        if (!runeData.recipeItems().isEmpty()) {
            tooltipAdder.accept(Component.literal("ʀɪᴛᴜᴀʟ ʀᴇᴀɢᴇɴᴛꜱ:").withStyle(ChatFormatting.GRAY));
        }

        tooltipAdder.accept(Component.empty());
        tooltipAdder.accept(Component.empty());
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        RuneData runeData = stack.get(ModComponents.rune.value());
        if (runeData == null || runeData.recipeItems().isEmpty()) return Optional.empty();

        List<ItemStack> stacks = runeData.recipeItems().entrySet().stream()
                .map(e -> {
                    // 1. Parse the ID and get the Optional Holder
                    return BuiltInRegistries.ITEM.get(Identifier.tryParse(e.getKey()))
                            // 2. If present, create the stack using the holder and the count
                            .map(itemHolder -> new ItemStack(itemHolder, e.getValue()))
                            // 3. If the item ID doesn't exist, return null (to be filtered out)
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();

        return Optional.of(new RuneRecipeTooltipComponentData(stacks));
    }
}