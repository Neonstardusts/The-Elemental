package com.teamneon.theelemental.client;

import com.teamneon.theelemental.block.entity.KingdomCoreBlockEntity;
import com.teamneon.theelemental.client.tooltip.InfoTooltipComponent;
import com.teamneon.theelemental.client.tooltip.InfoTooltipComponentData;
import com.teamneon.theelemental.helpers.ElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

import static com.teamneon.theelemental.Theelemental.id;

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

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int color = 0xFF000000 | ElementRegistry.getColor(core.getElement());

        boolean shift = mc.options.keyShift.isDown(); // Shift detection

        // --- Draw Face Name ---
        int x = width / 2 - mc.font.width(faceName) / 2;
        int y = height - 100;

        // Background for Face Name
        int bgWidth = mc.font.width(faceName) + 4;
        int bgHeight = 12; // Add extra if hint will be rendered
        guiGraphics.fill(x - 4, y - 4, x + bgWidth, y + bgHeight, 0x90000000);

        // Draw main face name
        guiGraphics.drawString(mc.font, faceName, x, y, color, true);

        if (!shift) {
            guiGraphics.pose().pushMatrix();
            float scale = 0.75f; // 75% size
            guiGraphics.pose().scale(scale, scale);

            String hintText = "ℹ ꜱʜɪꜰᴛ";

            // Calculate centered x/y with scale applied
            // Divide the full window width by scale, then subtract text width
            int hintX = (int) ((width / scale / 2) - (mc.font.width(hintText) / 2));
            int hintY = (int) ((y + mc.font.lineHeight + 2) / scale);

            guiGraphics.drawString(
                    mc.font,
                    hintText,
                    hintX,
                    hintY,
                    0xFF888888,
                    true
            );

            guiGraphics.pose().popMatrix();
        }

        else {
            // --- Draw full tooltip below face name ---
            InfoTooltipComponentData data = getTooltipForFace(face);
            if (data != null) {
                InfoTooltipComponent tooltip = new InfoTooltipComponent(data);
                int tooltipX = width / 2 - tooltip.getWidth(mc.font) / 2;
                int tooltipY = y + mc.font.lineHeight + 6; // a bit below face name
                tooltip.renderImage(
                        mc.font,
                        tooltipX,
                        tooltipY,
                        tooltip.getWidth(mc.font),
                        tooltip.getHeight(mc.font),
                        guiGraphics
                );
            }
        }
    }

    // --- Map face → tooltip data ---
    private static InfoTooltipComponentData getTooltipForFace(Direction face) {
        return switch (face) {
            case NORTH -> new InfoTooltipComponentData(
                    List.of("steve_teleport.png"),
                    List.of(
                            Component.literal("Warp Stone"),
                            Component.literal("Teleports you to the World Altar")
                    )
            );
            case EAST -> new InfoTooltipComponentData(
                    List.of("steve_use_core.png"),
                    List.of(
                            Component.literal("Soul Forge"),
                            Component.literal("Apply spells to player")
                    )
            );
            case SOUTH -> new InfoTooltipComponentData(
                    List.of("steve_ame_dia.png"),
                    List.of(
                            Component.literal("Crystal Chamber"),
                            Component.literal("Convert amethyst to mana, use diamonds to increase kingdom radius")
                    )
            );
            case WEST -> new InfoTooltipComponentData(
                    List.of("steve_rune.png"),
                    List.of(
                            Component.literal("Spell Inscriber"),
                            Component.literal("Imprint spells onto elemental amalgam")
                    )
            );
            default -> null;
        };
    }
}
