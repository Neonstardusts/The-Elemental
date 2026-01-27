package com.teamneon.theelemental.menu;

import com.teamneon.theelemental.client.ClientElementalData;
import com.teamneon.theelemental.client.ClientSpellInfo;
import com.teamneon.theelemental.client.ClientSpellRegistry;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.helpers.UtilityHelper;
import com.teamneon.theelemental.magic.base.SpellDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

import static com.teamneon.theelemental.Theelemental.id;

public class ElementalRuneCutterScreen extends AbstractContainerScreen<ElementalRuneCutterMenu> {

    private static final Identifier TEXTURE = id("textures/gui/runecutter.png");

    private static final int GRID_COLUMNS = 4;
    private static final int GRID_ROWS = 3;
    private static final int ENTRY_SIZE = 18;
    private static final int GRID_X = 48;
    private static final int GRID_Y = 16;

    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;

    public ElementalRuneCutterScreen(ElementalRuneCutterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.startIndex = 0;
        this.scrollOffs = 0.0F;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Draw background
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        renderSpellGrid(graphics, mouseX, mouseY);

        renderSpellDetails(graphics);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderSpellGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        List<SpellDefinition> spells = menu.getAvailableSpells();
        int playerLevel = ClientElementalData.get().getLevel();
        Minecraft mc = Minecraft.getInstance();

        int maxVisible = GRID_COLUMNS * GRID_ROWS;

        for (int i = 0; i < maxVisible; i++) {
            int spellIndex = startIndex + i;
            if (spellIndex >= spells.size()) break;

            SpellDefinition spell = spells.get(spellIndex);

            int x = leftPos + GRID_X + (i % GRID_COLUMNS) * ENTRY_SIZE+4;
            int y = topPos + GRID_Y + (i / GRID_COLUMNS) * ENTRY_SIZE-1;

            // Draw selection/background square like stonecutter
            boolean selected = menu.getSelectedSpellIndex() == spellIndex;
            boolean hovered = mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;

            if (hovered) {
                graphics.fill(x, y, x + 16, y + 16, 0x80FFFFFF);
            }

            if (selected) {
                graphics.fill(x - 1, y - 1, x + 17, y + 17, 0x80FFFFFF);
            }

            // Draw spell icon
            Identifier icon = id("textures/gui/spell_icons/" + spell.spellId() + ".png");
            graphics.blit(RenderPipelines.GUI_TEXTURED, icon, x, y, 0, 0, 16, 16, 16, 16);

            // Locked overlay
            if (playerLevel < spell.requiredLevel()) {
                graphics.fill(x, y, x + 16, y + 16, 0xAA000000);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (isOverScrollBar(mouseX, mouseY)) {
            scrolling = true;
            return true;
        }

        int index = getSpellIndexAt(mouseX, mouseY);
        if (index != -1) {
            //menu.setSelectedSpellIndex(index);
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        scrolling = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (scrolling) {
            int top = topPos + 15;
            int bottom = top + 54;
            scrollOffs = ((float) mouseY - top) / (bottom - top);
            scrollOffs = Mth.clamp(scrollOffs, 0.0F, 1.0F);
            updateScrollIndex();
            return true;
        }
        return super.mouseDragged(event, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int totalSpells = menu.getAvailableSpells().size();
        int maxScrollRows = Math.max(1, (totalSpells + GRID_COLUMNS - 1) / GRID_COLUMNS - GRID_ROWS);
        scrollOffs = Mth.clamp(scrollOffs - (float) scrollY / maxScrollRows, 0.0F, 1.0F);
        updateScrollIndex();
        return true;
    }

    private void updateScrollIndex() {
        int totalSpells = menu.getAvailableSpells().size();
        int max = Math.max(0, totalSpells - GRID_COLUMNS * GRID_ROWS);
        startIndex = (int) (scrollOffs * max + 0.5F);
    }

    private boolean isOverScrollBar(double mouseX, double mouseY) {
        int x = leftPos + 119;
        int y = topPos + 15;
        return mouseX >= x && mouseX < x + 12 && mouseY >= y && mouseY < y + 69;
    }

    private int getSpellIndexAt(double mouseX, double mouseY) {
        int mx = (int) mouseX - leftPos - GRID_X;
        int my = (int) mouseY - topPos - GRID_Y;
        if (mx < 0 || my < 0) return -1;

        int col = mx / ENTRY_SIZE;
        int row = my / ENTRY_SIZE;
        if (col >= GRID_COLUMNS || row >= GRID_ROWS) return -1;

        int index = row * GRID_COLUMNS + col + startIndex;
        return index < menu.getAvailableSpells().size() ? index : -1;
    }

    private void renderSpellDetails(GuiGraphics graphics) {
        int selectedIndex = menu.getSelectedSpellIndex();
        List<SpellDefinition> spells = menu.getAvailableSpells();

        // Only render if a valid spell is selected
        if (selectedIndex >= 0 && selectedIndex < spells.size()) {
            SpellDefinition spell = spells.get(selectedIndex);
            ClientSpellInfo info = ClientSpellRegistry.getSpell(spell.spellId());

            int elementId = info.spellId / 1000;
            int elementColor = ElementRegistry.getColor(elementId) | 0xFF000000;

            // Positioning: Right side of the main GUI with a small gap
            int x = this.leftPos + this.imageWidth;
            int y = this.topPos+10;
            int panelWidth = 122; // Adjust based on your needs
            // --- PRE-CALCULATE HEIGHT ---
            // 1. Fixed height for icon, name, and padding (roughly 55px)
            int calculatedHeight = 55;

            // 2. Add height for stats (Mana, CD, etc.)
            calculatedHeight += 12; // Mana
            calculatedHeight += 12; // Cooldown
            if (info.durationTicks > 0) calculatedHeight += 12;
            calculatedHeight += 4;  // Spacer
            calculatedHeight += 14; // Level Req

            // 3. Calculate Wrapped Description Height
            String description = info.description;
            // split returns a list of formatted lines based on the width
            var lines = this.font.split(Component.literal(description), panelWidth - 10);
            int descriptionHeight = lines.size() * this.font.lineHeight;

            // 4. Final Total Height (with 10px bottom padding)
            int finalHeight = calculatedHeight + descriptionHeight + 10;

            // 1. Draw a background for the sidebar (Optional)
            // You can use a generic dark semi-transparent fill or a custom texture
            graphics.fill(x, y, x + panelWidth, y + finalHeight, 0xAA000000);

            // 2. Draw Spell Icon (Large version)
            Identifier icon = id("textures/gui/spell_icons/" + spell.spellId() + ".png");
            graphics.blit(RenderPipelines.GUI_TEXTURED, icon, x + (panelWidth / 2) - 16, y + 10, 0, 0, 32, 32, 32, 32);

            // 3. Draw Spell Name
            graphics.drawCenteredString(this.font, info.name, x + (panelWidth / 2), y + 45, elementColor);

            int currentY = y + 55;
            // 4. Draw Requirements / Level
// 2. Mana Cost
            Component manaComp = Component.empty()
                    .append(Component.literal(" ★").withStyle(style -> style.withColor(0x6f9eb3)))
                    .append(Component.literal(" ᴍᴀɴᴀ ᴄᴏꜱᴛ →").withStyle(style -> style.withColor(0x8a8a8a)))
                    .append(Component.literal(" " + info.manaCost).withStyle(style -> style.withColor(0x6f9eb3)));
            graphics.drawString(this.font, manaComp, x + 5, currentY, 0xFFFFFFFF);
            currentY += 12;

            // 3. Cooldown
            double cooldownSeconds = Math.round(info.cooldownTicks / 20.0 * 10) / 10.0;
            Component cdComp = Component.empty()
                    .append(Component.literal(" ⌚").withStyle(style -> style.withColor(0xc7bb83)))
                    .append(Component.literal(" ᴄᴏᴏʟᴅᴏᴡɴ →").withStyle(style -> style.withColor(0x8a8a8a)))
                    .append(Component.literal(" " + cooldownSeconds + "s").withStyle(style -> style.withColor(0xc7bb83)));
            graphics.drawString(this.font, cdComp, x + 5, currentY, 0xFFFFFFFF);
            currentY += 12;

            // 4. Duration (Conditional)
            if (info.durationTicks > 0) {
                double durationSeconds = Math.round(info.durationTicks / 20.0 * 10) / 10.0;
                Component durComp = Component.empty()
                        .append(Component.literal(" ¤").withStyle(style -> style.withColor(0xb36fad)))
                        .append(Component.literal(" ᴅᴜʀᴀᴛɪᴏɴ →").withStyle(style -> style.withColor(0x8a8a8a)))
                        .append(Component.literal(" " + durationSeconds + "s").withStyle(style -> style.withColor(0xb36fad)));
                graphics.drawString(this.font, durComp, x + 5, currentY, 0xFFFFFFFF);
                currentY += 12;
            }

            currentY += 4;
// Get the current player's level
            int playerLevel = ClientElementalData.get().getLevel();
            boolean hasLevel = playerLevel >= info.requiredLevel;

// Determine the color: Lime (0x55FF55) if correct, Red (0xFF5555) if not
            int statusColor = hasLevel ? 0x55FF55 : 0xFF5555;

// Build the dynamic Level Requirement component
            Component levelReq = Component.empty()
                    // Emoji (Color based on status)
                    .append(Component.literal(" ✦").withStyle(style -> style.withColor(statusColor)))
                    // Label (Gray)
                    .append(Component.literal(" ꜱᴘᴇʟʟ ʟᴇᴠᴇʟ →").withStyle(style -> style.withColor(0x8a8a8a)))
                    // Fraction: Required / Current (Color based on status)
                    .append(Component.literal(" " + UtilityHelper.toSmallCaps(UtilityHelper.toRoman(info.requiredLevel + 1)))
                            .withStyle(style -> style.withColor(statusColor)));

// Draw the component
            graphics.drawString(this.font, levelReq, x + 5, currentY, 0xFFFFFFFF);
            currentY += 14; // Advance Y for the next element (description)



            String[] parts = description.split("\\. ");

// 1. Create a base component to hold all parts
            MutableComponent fullDescription = Component.empty();

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];

                // 2. Add the period back if it's not the very last character of the whole string
                if (!part.endsWith(".") && i < parts.length) {
                    part += ". ";
                }

                // 3. Append the styled part to the main component
                fullDescription.append(Component.literal(part).withStyle(style -> style.withColor(0x9a9a9a)));
            }

// 4. Draw the accumulated component
// Note: The color 0xFFAAAAAA will act as the default for any text WITHOUT a specific style
            graphics.drawWordWrap(this.font, fullDescription, x + 5, currentY, panelWidth - 10, 0xFFFFFFFF);
        }
    }

}
