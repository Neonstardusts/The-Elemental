package com.teamneon.theelemental.block;

import com.teamneon.theelemental.block.entity.KingdomCoreBlockEntity;
import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.helpers.SpellJsonLoader;
import com.teamneon.theelemental.item.ModItems;
import com.teamneon.theelemental.kingdoms.KingdomSavedData;
import com.teamneon.theelemental.magic.base.SpellRegistry;
import com.teamneon.theelemental.menu.ModMenuTypes;
import com.teamneon.theelemental.menu.SoulForgeMenu;
import com.teamneon.theelemental.store.ModComponents;
import com.teamneon.theelemental.store.RuneData;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.world.BalmMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.teamneon.theelemental.helpers.UtilityHelper.teleportToNearestSafeSpot;

public class KingdomCoreInteraction {

    // Track shift-right-clicks per player
    private static final Map<UUID, ClickTracker> shiftClickTracker = new HashMap<>();

    private static class ClickTracker {
        long lastClickTime;
        int clickCount;
    }

    /**
     * Handles all right-clicks (with item or empty hand)
     */
    public static InteractionResult handleWithItem(Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (!(world.getBlockEntity(pos) instanceof KingdomCoreBlockEntity core)) {
            return InteractionResult.PASS;
        }

        if (world.isClientSide()) return InteractionResult.SUCCESS;

        // --- Check if the item is amethyst ---
        if (stack.getItem() == Items.AMETHYST_SHARD) {
            // Get player's elemental data
            ElementalData data = ElementalDataHandler.get(player);

            // Element check
            if (data.getElement() != core.getElement()) {
                player.displayClientMessage(Component.literal("Your element does not match this Kingdom Core!"), true);
                return InteractionResult.FAIL;
            }

            // Increase mana
            data.setCurrentMana(data.getCurrentMana() + 10f);

            // Consume 1 amethyst
            stack.shrink(1);

            player.displayClientMessage(Component.literal("Your mana increased by 10!"), true);
            return InteractionResult.CONSUME;
        } else if (stack.getItem() == ModItems.BLANK_RUNE.asItem()) {
            // Get player's elemental data
            ElementalData data = ElementalDataHandler.get(player);

            // Element of the Kingdom Core
            int elementId = core.getElement();

            // 1. Generate a random SpellID for this element
            int baseId = elementId * 1000; // e.g., Fire = 1000, Water = 2000
            int maxSpells = SpellRegistry.getSpellCountForElement(elementId); // total number of spells for this element

            if (maxSpells <= 0) {
                player.displayClientMessage(Component.literal("No spells registered for this element!"), true);
                return InteractionResult.FAIL;
            }

            int randomOffset = (int) (Math.random() * maxSpells) + 1; // +1 so 1001 instead of 1000
            int spellId = baseId + randomOffset;

            // 2. Lookup the RecipeItems JSON for this spell
            Map<String, Integer> recipeItems;
            try {
                recipeItems = SpellJsonLoader.getRecipeForSpell(spellId, world.getServer().getResourceManager());
            } catch (Exception e) {
                player.displayClientMessage(Component.literal("Failed to load recipe for spell " + spellId), true);
                return InteractionResult.FAIL;
            }

            if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                // Get the ResourceManager
                var manager = serverPlayer.level().getServer().getResourceManager();

                // 3. Transform Blank Rune into Element Rune
                ItemStack elementRune = new ItemStack(ModItems.ELEMENT_RUNE.asItem());

                try {
                    // Read JSON for the spell
                    Map<String, Object> spellJson = SpellJsonLoader.getFullSpellJson(spellId, manager);
                    String spellName = (String) spellJson.getOrDefault("SpellName", "Unknown Spell");
                    Number manaNum = (Number) spellJson.getOrDefault("ManaCost", 0);
                    int manaCost = manaNum.intValue();
                    Number cooldownNum = (Number) spellJson.getOrDefault("Cooldown", 0);
                    int cooldown = cooldownNum.intValue();
                    String description = (String) spellJson.getOrDefault("Description", "No description");

                    // Create RuneData with spellName
                    RuneData runeData = new RuneData(elementId, spellId, spellName, recipeItems, manaCost, cooldown, description);

                    // Attach component to ItemStack
                    elementRune.set(ModComponents.rune.value(), runeData);

                    // Give player the Element Rune
                    stack.shrink(1); // remove Blank Rune
                    if (!player.getInventory().add(elementRune)) {
                        player.drop(elementRune, false);
                    }

                    // Give player the Element Rune
                    stack.shrink(1); // remove Blank Rune
                    if (!player.getInventory().add(elementRune)) {
                        player.drop(elementRune, false);
                    }

                    player.displayClientMessage(Component.literal("Your Rune has been attuned! Spell: " + spellName), true);

                } catch (Exception e) {
                    // Handle missing/invalid JSON
                    player.displayClientMessage(Component.literal("Failed to load spell JSON for spell " + spellId), true);
                    e.printStackTrace(); // Log for debugging
                }
            }


            player.displayClientMessage(Component.literal("Your Blank Rune has been attuned! SpellID: " + spellId), true);
            return InteractionResult.CONSUME;
        }

        else {
            // --- Empty-hand or any other item logic ---

            // Only trigger teleport if player is sneaking
            if (player.isShiftKeyDown()) {
                UUID playerId = player.getUUID();
                long currentTime = world.getGameTime();

                ClickTracker tracker = shiftClickTracker.getOrDefault(playerId, new ClickTracker());

                // Reset click count if more than 20 ticks (~1 sec) since last
                if (currentTime - tracker.lastClickTime > 20) tracker.clickCount = 0;

                tracker.clickCount++;
                tracker.lastClickTime = currentTime;

                shiftClickTracker.put(playerId, tracker);

                if (tracker.clickCount >= 2) {
                    // Get global data
                    KingdomSavedData globalData = KingdomSavedData.get((ServerLevel) world);
                    BlockPos altarPos = globalData.getCorePos(-1); // -1 = elemental altar

                    if (altarPos != null) {
                        // Teleport player to the altar
                        teleportToNearestSafeSpot(player, altarPos.getX(), altarPos.getY(), altarPos.getZ(), ElementRegistry.getColor(core.getElement()));
                    } else {
                        // Fallback if altar not yet registered
                        player.displayClientMessage(Component.literal("No Elemental Altar found."), true);
                    }

                    // Reset click tracker
                    tracker.clickCount = 0;
                    shiftClickTracker.put(playerId, tracker);

                    return InteractionResult.CONSUME;
                }

                // Optional feedback for first click
                player.displayClientMessage(Component.literal("Shift-right-click again to teleport..."), true);
                return InteractionResult.CONSUME;
            }

            // Normal empty-hand interaction if member
            if (player.getUUID().equals(core.getOwner()) || core.getMembers().contains(player.getUUID())) {
                player.displayClientMessage(Component.literal("You opened the Kingdom Core!"), true);
                if (!player.level().isClientSide()) {
                    // Open the Menu using the Balm registration you created
                    MenuProvider soulForgeProvider = new BalmMenuProvider<ModMenuTypes.SoulForgeData>() {
                        @Override
                        public Component getDisplayName() {
                            return Component.translatable("container.theelemental.soul_forge");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                            return new SoulForgeMenu(id, inv, p);
                        }

                        @Override
                        public ModMenuTypes.SoulForgeData getScreenOpeningData(ServerPlayer player) {
                            // This is where you provide the data the client needs
                            return new ModMenuTypes.SoulForgeData();
                        }

                        @Override
                        public StreamCodec<RegistryFriendlyByteBuf, ModMenuTypes.SoulForgeData> getScreenStreamCodec() {
                            // Tell Balm how to encode the data above
                            return ModMenuTypes.SoulForgeData.STREAM_CODEC;
                        }
                    };
                    Balm.networking().openMenu(player, soulForgeProvider);
                }
// Now this call will NO LONGER CRASH on Fabric!


            } else {
                player.displayClientMessage(Component.literal("You are not in this kingdom!"), true);
            }

            return InteractionResult.CONSUME;
        }
    }
}
