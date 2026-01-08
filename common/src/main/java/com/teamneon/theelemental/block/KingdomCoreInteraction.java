package com.teamneon.theelemental.block;

import com.teamneon.theelemental.block.entity.KingdomCoreBlockEntity;
import com.teamneon.theelemental.client.ClientSpellInfo;
import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.helpers.SpellJsonLoader;
import com.teamneon.theelemental.item.ModItems;
import com.teamneon.theelemental.kingdoms.KingdomSavedData;
import com.teamneon.theelemental.magic.base.SpellRegistry;
import com.teamneon.theelemental.magic.network.SyncSpellInfoPacket;
import com.teamneon.theelemental.menu.ElementalRuneCutterMenu;
import com.teamneon.theelemental.menu.ModMenuTypes;
import com.teamneon.theelemental.menu.SoulForgeMenu;
import com.teamneon.theelemental.store.ModComponents;
import com.teamneon.theelemental.store.RuneData;
import com.teamneon.theelemental.store.SpellInfoServerHelper;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.world.BalmMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
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
        } else if (stack.getItem() == Items.DIAMOND) {
            // Increase the Kingdom Core radius by 1, max 128
            float newRadius = Math.min(core.getRadius() + 1.0f, 128.0f);
            core.setRadius(newRadius);

            // Consume the diamond
            stack.shrink(1);

            player.displayClientMessage(Component.literal("The Kingdom Core radius increased to " + (int)newRadius) , true);
            return InteractionResult.CONSUME;
        }
        // Right-clicking with a Blank Rune
        if (stack.getItem() == ModItems.BLANK_RUNE.asItem()) {
            if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {

                // --- 4️⃣ Send SpellInfo metadata to client ---
                ResourceManager manager = serverPlayer.level().getServer().getResourceManager();

                // Loop through all spells in your registry
                for (int spellId : SpellRegistry.getAllSpellIds()) {
                    // Skip ID 0 (empty spell)
                    if (spellId <= 0) continue;

                    // Load the spell JSON and extract metadata
                    ClientSpellInfo info = SpellInfoServerHelper.loadSpellInfo(spellId, manager);

// Send each field individually
                    Balm.networking().sendTo(player, new SyncSpellInfoPacket(
                            info.spellId,
                            info.name,
                            info.description,
                            info.manaCost,
                            info.cooldownTicks,
                            info.durationTicks,
                            info.requiredLevel
                    ));

                }

                // Create the menu provider
                BalmMenuProvider<ModMenuTypes.RuneCutterData> runeCutterProvider = new BalmMenuProvider<>() {
                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("container.theelemental.rune_cutter");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player p) {
                        // Pass element = 0 or read from Kingdom Core / context if needed
                        return new ElementalRuneCutterMenu(windowId, inv, ContainerLevelAccess.NULL, core.getElement());
                    }

                    @Override
                    public ModMenuTypes.RuneCutterData getScreenOpeningData(ServerPlayer player) {
                        return new ModMenuTypes.RuneCutterData(core.getElement());
                    }

                    @Override
                    public StreamCodec<RegistryFriendlyByteBuf, ModMenuTypes.RuneCutterData> getScreenStreamCodec() {
                        return ModMenuTypes.RuneCutterData.STREAM_CODEC;
                    }
                };

                // Open the menu for the server player
                Balm.networking().openMenu(serverPlayer, runeCutterProvider);
            }

            return InteractionResult.SUCCESS; // client side will just return SUCCESS
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


            } else {
                player.displayClientMessage(Component.literal("You are not in this kingdom!"), true);
            }

            return InteractionResult.CONSUME;
        }
    }
}
