package com.teamneon.theelemental.menu;

import com.teamneon.theelemental.helpers.ElementRegistry;
import net.blay09.mods.balm.Balm;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

import static com.teamneon.theelemental.Theelemental.id;

public class ElementChooserScreen extends AbstractContainerScreen<ElementChooserMenu> {

    private static final Identifier TEXTURE = id("textures/gui/element_chooser.png");
    private static final Identifier BOX_TEXTURE = id("textures/gui/box_chooser.png");

    // ============================================================
    // TWEAKABLE OFFSETS (Adjust these to move elements)
    // ============================================================
    private static final int BG_WIDTH = 176;
    private static final int BG_HEIGHT = 232;

    private static final int PREV_BTN_X = 30;
    private static final int PREV_BTN_Y = 207;

    private static final int NEXT_BTN_X = 126;
    private static final int NEXT_BTN_Y = 207;

    private static final int CHOOSE_BTN_X = 58;
    private static final int CHOOSE_BTN_Y = 207;

    private static final int CHOOSE_BOX_X = 22;
    private static final int CHOOSE_BOX_Y = 14;

    private static final int SELECTION_TEXT_X = 48; // Left alignment offset
    private static final int SELECTION_TEXT_Y = 22;

    private static final int MESSAGE_TEXT_X = 15;
    private static final int MESSAGE_TEXT_Y = 45;
    // ============================================================

    private int currentIndex = 0;
    private List<Integer> elementIds;

    public ElementChooserScreen(ElementChooserMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = BG_WIDTH;
        this.imageHeight = BG_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        elementIds = this.menu.getAvailableElements();

        int currentEl = this.menu.getCurrentElementId();
        currentIndex = elementIds.indexOf(currentEl);
        if (currentIndex < 0) currentIndex = 0;

        this.addRenderableWidget(Button.builder(Component.literal("<"), btn -> {
            currentIndex--;
            if (currentIndex < 0) currentIndex = elementIds.size() - 1;
        }).bounds(this.leftPos + PREV_BTN_X, this.topPos + PREV_BTN_Y, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal(">"), btn -> {
            currentIndex++;
            if (currentIndex >= elementIds.size()) currentIndex = 0;
        }).bounds(this.leftPos + NEXT_BTN_X, this.topPos + NEXT_BTN_Y, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Choose"), btn -> {
            if (!elementIds.isEmpty()) {
                int selectedId = elementIds.get(currentIndex);
                Balm.networking().sendToServer(new com.teamneon.theelemental.menu.C2SChooseElementPacket(selectedId));

                //close menu
                this.onClose();
            }
        }).bounds(this.leftPos + CHOOSE_BTN_X, this.topPos + CHOOSE_BTN_Y, 60, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int color = 0xFFFFFFFF;
        if (!elementIds.isEmpty()) {
            int elId = elementIds.get(currentIndex);
            color = 0xFF000000 | ElementRegistry.getColor(elId);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, BOX_TEXTURE, this.leftPos+ CHOOSE_BOX_X, this.topPos+ CHOOSE_BOX_Y, 0, 0, 135, 24, 135, 24, color);

    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!elementIds.isEmpty()) {
            int elId = elementIds.get(currentIndex);
            String name = ElementRegistry.getName(elId);
            String race = ElementRegistry.getRace(elId);
            String msg = ElementRegistry.getMessage(elId);
            int color = 0xFF000000 | ElementRegistry.getColor(elId);
            int wrapWidth = this.imageWidth - (MESSAGE_TEXT_X * 2);
            int currentY = MESSAGE_TEXT_Y;

            // 1. Header (Name + Race)
            String display = ElementRegistry.getName(elId) + " " + ElementRegistry.getRace(elId);
            graphics.drawString(this.font, display, SELECTION_TEXT_X, SELECTION_TEXT_Y, 0xFFFFFFFF, true);

            // 2. Icon
            int iconX = CHOOSE_BOX_X + 4; // 4 pixel padding inside the box
            int iconY = CHOOSE_BOX_Y + 4; // 4 pixel padding inside the box

            Identifier elementTexture = id("textures/gui/element_icons/element_" + elId + ".png");
            graphics.blit(RenderPipelines.GUI_TEXTURED, elementTexture, iconX, iconY, 0, 0, 16, 16, 16, 16, 0xFFFFFFFF);
            // 3. Description Section
            graphics.drawString(this.font, "ᴅᴇꜱᴄʀɪᴘᴛɪᴏɴ:", MESSAGE_TEXT_X, currentY, 0xFFc8c8c8);
            currentY += 10;
            graphics.drawWordWrap(this.font, Component.literal(ElementRegistry.getMessage(elId)), MESSAGE_TEXT_X, currentY, wrapWidth, 0xFFFFFFFF);

            // Calculate height of previous word-wrap (font height * number of lines)
            int descLines = this.font.split(Component.literal(ElementRegistry.getMessage(elId)), wrapWidth).size();
            currentY += (descLines * 9) + 25; // 9 is standard font height, 6 is padding

            // 4. Passives Section
            List<String> passives = ElementRegistry.getPassives(elId);
            if (!passives.isEmpty()) {
                graphics.drawString(this.font, "❤ ᴘᴀꜱꜱɪᴠᴇ:", MESSAGE_TEXT_X, currentY, 0xFF85e06e);
                currentY += 10;
                for (String p : passives) {
                    String text = "• " + p;
                    graphics.drawWordWrap(this.font, Component.literal(text), MESSAGE_TEXT_X, currentY, wrapWidth, 0xFFFFFFFF);
                    int lines = this.font.split(Component.literal(text), wrapWidth).size();
                    currentY += (lines * 9);
                }
                currentY += 6; // Spacing after section
            }

            // 5. Restrictions Section
            List<String> restrictions = ElementRegistry.getRestrictions(elId);
            if (!restrictions.isEmpty()) {
                graphics.drawString(this.font, "❌ ʀᴇꜱᴛʀɪᴄᴛɪᴏɴꜱ:", MESSAGE_TEXT_X, currentY, 0xFFeb6360);
                currentY += 10;
                for (String r : restrictions) {
                    String text = "• " + r;
                    graphics.drawWordWrap(this.font, Component.literal(text), MESSAGE_TEXT_X, currentY, wrapWidth, 0xFFFFFFFF);
                    int lines = this.font.split(Component.literal(text), wrapWidth).size();
                    currentY += (lines * 9);
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}