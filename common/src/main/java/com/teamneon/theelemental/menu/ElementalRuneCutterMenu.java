package com.teamneon.theelemental.menu;

import com.teamneon.theelemental.client.ClientSpellRegistry;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.SpellJsonLoader;
import com.teamneon.theelemental.item.ModItems;
import com.teamneon.theelemental.magic.base.SpellDefinition;
import com.teamneon.theelemental.magic.base.SpellRegistry;
import com.teamneon.theelemental.store.ModComponents;
import com.teamneon.theelemental.store.RuneData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

public class ElementalRuneCutterMenu extends AbstractContainerMenu {

    public static final int INPUT_SLOT = 0;
    public static final int RESULT_SLOT = 1;

    private final int element;
    private final ContainerLevelAccess access;
    private final Player player;

    private final Container inputContainer;
    private final ResultContainer resultContainer;

    private final Slot inputSlot;
    private final Slot resultSlot;

    private final DataSlot selectedSpellIndex = DataSlot.standalone();
    private List<SpellDefinition> availableSpells = List.of();

    public ElementalRuneCutterMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access, int element) {
        super(ModMenuTypes.RUNE_CUTTER_MENU.value(), containerId);
        this.access = access;
        this.player = playerInventory.player;
        this.element = element;


        this.inputContainer = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                ElementalRuneCutterMenu.this.slotsChanged(this);
            }
        };

        this.resultContainer = new ResultContainer();

        this.inputSlot = this.addSlot(new Slot(inputContainer, INPUT_SLOT, 20, 33));

        this.resultSlot = this.addSlot(new Slot(resultContainer, RESULT_SLOT, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }
            @Override
            public boolean mayPickup(Player player) {
                int idx = selectedSpellIndex.get();
                boolean canPickup = idx >= 0 && idx < availableSpells.size()
                        && ElementalDataHandler.get(player).getLevel() >= availableSpells.get(idx).requiredLevel();
                return canPickup;
            }
            @Override
            public void onTake(Player player, ItemStack stack) {
                inputSlot.remove(1);
                super.onTake(player, stack);
            }
        });

        this.addStandardInventorySlots(playerInventory, 8, 84);
        this.addDataSlot(selectedSpellIndex);

    }

    @Override
    public void slotsChanged(Container container) {
        ItemStack stack = inputSlot.getItem();
        setupSpellList(stack);
    }

    private void setupSpellList(ItemStack stack) {
        selectedSpellIndex.set(-1);
        resultSlot.set(ItemStack.EMPTY);


        if (!stack.is(ModItems.BLANK_RUNE.asItem())) {
            availableSpells = List.of();

            return;
        }

        // Use client-side registry if on client
        if (player.level().isClientSide()) {
            availableSpells = ClientSpellRegistry.getSpellsForElement(element);

        } else {
            var manager = player.level().getServer().getResourceManager();
            availableSpells = SpellRegistry.getAllSpellIds().stream()
                    .filter(id -> id / 1000 == element)
                    .sorted()
                    .map(id -> new SpellDefinition(id, SpellRegistry.getRequiredLevel(id, manager)))
                    .toList();

        }

        if (!availableSpells.isEmpty()) {
            selectedSpellIndex.set(0);
            setupResultSlot(0);
        }
    }


    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= availableSpells.size()) return false;
        selectedSpellIndex.set(id);
        setupResultSlot(id);
        return true;
    }

    private void setupResultSlot(int index) {

        if (index < 0 || index >= availableSpells.size()) {
            resultSlot.set(ItemStack.EMPTY);

            return;
        }

        SpellDefinition spell = availableSpells.get(index);
        int level = ElementalDataHandler.get(player).getLevel();
        if (level < spell.requiredLevel()) {
            resultSlot.set(ItemStack.EMPTY);
            return;
        }

        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            try {
                var manager = serverPlayer.level().getServer().getResourceManager();
                Map<String, Object> spellJson = SpellJsonLoader.getFullSpellJson(spell.spellId(), manager);
                String spellName = (String) spellJson.getOrDefault("SpellName", "Unknown Spell");
                int manaCost = ((Number) spellJson.getOrDefault("ManaCost", 0)).intValue();
                int cooldown = ((Number) spellJson.getOrDefault("Cooldown", 0)).intValue();
                String description = (String) spellJson.getOrDefault("Description", "No description");
                int durationTicks = ((Number) spellJson.getOrDefault("Duration", 0)).intValue();
                Map<String, Integer> recipeItems = SpellJsonLoader.getRecipeForSpell(spell.spellId(), manager);

                RuneData runeData = new RuneData(element, spell.spellId(), spellName, recipeItems, manaCost, cooldown, description, durationTicks);
                ItemStack elementRune = new ItemStack(ModItems.ELEMENT_RUNE.asItem());
                elementRune.set(ModComponents.rune.value(), runeData);
                resultSlot.set(elementRune);
            } catch (Exception e) {
                e.printStackTrace();
                resultSlot.set(ItemStack.EMPTY);
            }
        } else {
            resultSlot.set(ItemStack.EMPTY);
        }

        broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // copy logic from StonecutterMenu
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == RESULT_SLOT) {
                if (!this.moveItemStackTo(stack, 2, 38, true)) return ItemStack.EMPTY;
            } else if (index == INPUT_SLOT) {
                if (!this.moveItemStackTo(stack, 2, 38, false)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) return ItemStack.EMPTY;

            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            slot.setChanged();
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // This ensures the item in the input slot is returned to the player or dropped
        this.access.execute((level, pos) -> {
            this.clearContainer(player, this.inputContainer);
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Blocks.STONECUTTER);
    }

    @Override
    public MenuType<?> getType() {
        return ModMenuTypes.RUNE_CUTTER_MENU.value();
    }

    // Inside ElementalRuneCutterMenu
    public List<SpellDefinition> getAvailableSpells() {
        return availableSpells;
    }

    public int getSelectedSpellIndex() {
        return selectedSpellIndex.get();
    }

}
