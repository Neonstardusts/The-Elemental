package com.teamneon.theelemental.client;

import com.teamneon.theelemental.block.ElementalAltar;
import com.teamneon.theelemental.block.KingdomAnchor;
import com.teamneon.theelemental.block.WorldCrafter;
import com.teamneon.theelemental.client.tooltip.InfoTooltipComponent;
import com.teamneon.theelemental.client.tooltip.InfoTooltipComponentData;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.helpers.UtilityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class BlockInfoHUD {

    // Helper record to hold our Title + Tooltip Data
    private record BlockHUDResult(Component title, InfoTooltipComponentData tooltipData) {}

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!(mc.hitResult instanceof BlockHitResult hit)) return;

        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);

        // 1. Get the specific HUD result
        BlockHUDResult result = getBlockHUDData(state);
        if (result == null) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        boolean shift = mc.options.keyShift.isDown();

        // 2. Setup Position (Centered)
        int x = width / 2 - mc.font.width(result.title()) / 2;
        int y = height - 100;

        // 3. Render Title Background & Text
        int bgWidth = mc.font.width(result.title()) + 8;
        guiGraphics.fill(x - 4, y - 4, x + bgWidth - 4, y + 12, 0x90000000);
        guiGraphics.drawString(mc.font, result.title(), x, y, 0xFFFFFFFF, true);

        // 4. Handle Shift logic
        if (!shift) {
            renderShiftHint(guiGraphics, mc, width, y);
        } else {
            // Render the separate lines/images below the title
            renderFullTooltip(guiGraphics, mc, result.tooltipData(), width, y);
        }
    }

    private static BlockHUDResult getBlockHUDData(BlockState state) {
        // --- KINGDOM ANCHOR ---
        if (state.getBlock() instanceof KingdomAnchor) {
            int element = state.hasProperty(KingdomAnchor.ELEMENT) ? state.getValue(KingdomAnchor.ELEMENT) : 0;
            int color = ElementRegistry.getColor(element);
            String kingdomTitle = ElementRegistry.getName(element);

            Component title = Component.literal("Kingdom Anchor").withStyle(s -> s.withColor(color));

            InfoTooltipComponentData tooltip = new InfoTooltipComponentData(
                    List.of("steve_teleport.png"),
                    List.of(
                            Component.literal("Interact to teleport"),
                            Component.empty()
                                    .append(Component.literal("to the "))
                                    .append(Component.literal(kingdomTitle+" Kingdom").withStyle(style -> style.withColor(color)))
                                    .append(Component.literal("!"))

                    )
            );

            return new BlockHUDResult(title, tooltip);
        }
        if (state.getBlock() instanceof WorldCrafter) {

            int rainbow = UtilityHelper.getRainbowColor(5000);

            Component title = Component.literal("World Crafter").withStyle(s -> s.withColor(rainbow));

            InfoTooltipComponentData tooltip = new InfoTooltipComponentData(
                    List.of("steve_place.png", "steve_hold.png"),
                    List.of(
                            Component.literal("Place the reagents in the surrounding pillars."),
                            Component.literal("And the spell your trying to craft in the center."),
                            Component.literal("[Right-Click] to place and take items."),
                            Component.literal("[Shift-Right-Click] to craft the spell.")
                    )
            );

            return new BlockHUDResult(title, tooltip);
        }
        if (state.getBlock() instanceof ElementalAltar) {

            int rainbow = UtilityHelper.getRainbowColor(5000);

            Component title = Component.literal("Elemental Altar").withStyle(s -> s.withColor(rainbow));

            InfoTooltipComponentData tooltip = new InfoTooltipComponentData(
                    List.of("steve_place.png"),
                    List.of(
                            Component.literal("Contains the magic of the world, used to select element"),
                            Component.literal("[Right-Click] to open element chooser.")
                    )
            );

            return new BlockHUDResult(title, tooltip);
        }



        return null;
    }

    private static void renderShiftHint(GuiGraphics guiGraphics, Minecraft mc, int width, int y) {
        guiGraphics.pose().pushMatrix();
        float scale = 0.75f;
        guiGraphics.pose().scale(scale, scale);
        String hintText = "ℹ ꜱʜɪꜰᴛ";
        int hintX = (int) ((width / scale / 2) - (mc.font.width(hintText) / 2));
        int hintY = (int) ((y + mc.font.lineHeight + 2) / scale);
        guiGraphics.drawString(mc.font, hintText, hintX, hintY, 0xFF888888, true);
        guiGraphics.pose().popMatrix();
    }

    private static void renderFullTooltip(GuiGraphics guiGraphics, Minecraft mc, InfoTooltipComponentData data, int width, int y) {
        InfoTooltipComponent tooltip = new InfoTooltipComponent(data);

        // Calculate dimensions
        int tooltipWidth = tooltip.getWidth(mc.font);
        int tooltipHeight = tooltip.getHeight(mc.font);

        // Position it below the title
        int tooltipX = width / 2 - tooltipWidth / 2;
        int tooltipY = y + mc.font.lineHeight + 6;

        // --- DRAW BACKGROUND FOR INFO BIT ---
        // We add a small 4px padding around the tooltip content
        guiGraphics.fill(
                tooltipX - 4,
                tooltipY - 4,
                tooltipX + tooltipWidth + 4,
                tooltipY + tooltipHeight + 4,
                0x90000000 // Same semi-transparent black as the title
        );

        // Render the actual content (images and text)
        tooltip.renderImage(
                mc.font,
                tooltipX,
                tooltipY,
                tooltipWidth,
                tooltipHeight,
                guiGraphics
        );
    }
}