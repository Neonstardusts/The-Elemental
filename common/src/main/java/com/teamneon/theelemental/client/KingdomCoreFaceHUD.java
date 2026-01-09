package com.teamneon.theelemental.client;

import com.teamneon.theelemental.block.KingdomCoreBlock;
import com.teamneon.theelemental.block.entity.KingdomCoreBlockEntity;
import com.teamneon.theelemental.client.tooltip.InfoTooltipComponent;
import com.teamneon.theelemental.client.tooltip.InfoTooltipComponentData;
import com.teamneon.theelemental.helpers.ElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

public class KingdomCoreFaceHUD {

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!(mc.hitResult instanceof BlockHitResult hit)) return;

        BlockPos pos = hit.getBlockPos();
        Direction face = hit.getDirection();

        if (!(mc.level.getBlockEntity(pos) instanceof KingdomCoreBlockEntity core)) return;

        String faceName = switch (face) {
            case NORTH -> "Warp Stone";
            case EAST -> "Soul Forge";
            case SOUTH -> "Crystal Chamber";
            case WEST -> "Spell Inscriber";
            default -> null;
        };

        if (faceName == null) return;

        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof KingdomCoreBlock)) return;

        int elementValue = state.hasProperty(KingdomCoreBlock.ELEMENTCore)
                ? state.getValue(KingdomCoreBlock.ELEMENTCore)
                : 0;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int color = 0xFF000000 | ElementRegistry.getColor(elementValue);

        boolean shift = mc.options.keyShift.isDown();

        // --- 1. Draw Face Name (Title) ---
        int x = width / 2 - mc.font.width(faceName) / 2;
        int y = height - 100;

        // Background for Face Name
        int bgWidth = mc.font.width(faceName) + 8;
        guiGraphics.fill(x - 4, y - 4, x + bgWidth - 4, y + 12, 0x90000000);
        guiGraphics.drawString(mc.font, faceName, x, y, color, true);

        if (!shift) {
            renderShiftHint(guiGraphics, mc, width, y);
        } else {
            // --- 2. Draw Wrapped Tooltip with Background ---
            InfoTooltipComponentData data = getTooltipForFace(mc, face);
            if (data != null) {
                InfoTooltipComponent tooltip = new InfoTooltipComponent(data);

                int tooltipWidth = tooltip.getWidth(mc.font);
                int tooltipHeight = tooltip.getHeight(mc.font);
                int tooltipX = width / 2 - tooltipWidth / 2;
                int tooltipY = y + mc.font.lineHeight + 6;

                // --- NEW BLACK BACKGROUND BOX ---
                guiGraphics.fill(
                        tooltipX - 4,
                        tooltipY - 4,
                        tooltipX + tooltipWidth + 4,
                        tooltipY + tooltipHeight + 4,
                        0x90000000
                );

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

    private static InfoTooltipComponentData getTooltipForFace(Minecraft mc, Direction face) {
        int wrapWidth = 200;
        List<Component> lines = new ArrayList<>();

        return switch (face) {
            case NORTH -> {
                addWrapped(lines, mc, "Teleports you to the World Altar", wrapWidth);
                yield new InfoTooltipComponentData(List.of("steve_teleport.png"), lines);
            }
            case EAST -> {
                addWrapped(lines, mc, "Apply spells to player", wrapWidth);
                yield new InfoTooltipComponentData(List.of("steve_use_core.png"), lines);
            }
            case SOUTH -> {
                addWrapped(lines, mc, "Convert amethyst to mana, use diamonds to increase kingdom radius", wrapWidth);
                yield new InfoTooltipComponentData(List.of("steve_ame_dia.png"), lines);
            }
            case WEST -> {
                addWrapped(lines, mc, "Imprint spells onto elemental amalgam", wrapWidth);
                yield new InfoTooltipComponentData(List.of("steve_rune.png"), lines);
            }
            default -> null;
        };
    }

    private static void addWrapped(List<Component> list, Minecraft mc, String text, int width) {
        mc.font.getSplitter().splitLines(text, width, net.minecraft.network.chat.Style.EMPTY)
                .forEach(line -> list.add(Component.literal(line.getString())));
    }
}