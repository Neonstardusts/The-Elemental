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
import com.teamneon.theelemental.store.ManaData;
import com.teamneon.theelemental.store.ModComponents;
import com.teamneon.theelemental.store.RuneData;
import com.teamneon.theelemental.store.SpellInfoServerHelper;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.world.BalmMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.teamneon.theelemental.helpers.UtilityHelper.teleportToNearestSafeSpot;

public class KingdomCoreInteraction {

    /**
     * Handles all right-clicks (with item or empty hand)
     */
    public static InteractionResult handleWithItem(
            Level world,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            BlockHitResult hitResult
    ) {
        if (!(world.getBlockEntity(pos) instanceof KingdomCoreBlockEntity core)) {
            return InteractionResult.PASS;
        }

        if (world.isClientSide()) return InteractionResult.SUCCESS;

        Direction face = hitResult.getDirection();

        // 1. Handle "Public" interactions first (Teleport)
        if (face == Direction.NORTH) {
            return handleTeleport(world, player, core);
        }

        // 2. Ignore top & bottom
        if (face == Direction.UP || face == Direction.DOWN) {
            return InteractionResult.PASS;
        }

        // 3. Permission Guard for "Private" interactions (Forge, Mana, Rune Cutter)
        ElementalData playerData = ElementalDataHandler.get(player);
        boolean isOwner = player.getUUID().equals(core.getOwner());
        boolean isMember = core.getMembers().contains(player.getUUID());
        boolean matchesElement = playerData.getElement() == core.getElement();

        if (!isOwner && !isMember && !matchesElement) {
            player.displayClientMessage(Component.literal("This is not your elemental kingdom!"), true);
            return InteractionResult.FAIL;
        }

        // 4. Handle protected interactions
        return switch (face) {
            case EAST -> openSoulForge(world, player, core);
            case SOUTH -> handleManaRefill(world, player, stack, core);
            case WEST -> openRuneCutter(world, player, core);
            default -> InteractionResult.PASS;
        };
    }

    private static InteractionResult handleTeleport(Level world, Player player, KingdomCoreBlockEntity core) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;


        KingdomSavedData globalData = KingdomSavedData.get((ServerLevel) world);
        BlockPos altarPos = globalData.getCorePos(-1);

        if (altarPos != null) {
            teleportToNearestSafeSpot(
                    player,
                    altarPos.getX(),
                    altarPos.getY(),
                    altarPos.getZ(),
                    ElementRegistry.getColor(core.getElement())
            );
        } else {
            player.displayClientMessage(Component.literal("No Elemental Altar found."), true);
        }

        return InteractionResult.CONSUME;

    }

    private static InteractionResult openSoulForge(Level world, Player player, KingdomCoreBlockEntity core) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        if (player instanceof ServerPlayer serverPlayer) {
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
                    return new ModMenuTypes.SoulForgeData();
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, ModMenuTypes.SoulForgeData> getScreenStreamCodec() {
                    return ModMenuTypes.SoulForgeData.STREAM_CODEC;
                }
            };

            Balm.networking().openMenu(serverPlayer, soulForgeProvider);
        }
        return InteractionResult.CONSUME;
    }


    private static InteractionResult handleManaRefill(
            Level world,
            Player player,
            ItemStack stack,
            KingdomCoreBlockEntity core
    ) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        Item item = stack.getItem();
        ElementalData data = ElementalDataHandler.get(player);


        if (data.getElement() != core.getElement()) {
            player.displayClientMessage(
                    Component.literal("Your element does not match this Kingdom Core!"),
                    true
            );
            return InteractionResult.FAIL;
        }

        // 1. MANA REFILL (Amethyst Shard)
        if (item == ModItems.MANA_STORING_CRYSTAL.asItem()) {
            // 1. Get current data
            ManaData currentData = stack.getOrDefault(ModComponents.mana_storage.value(), ManaData.EMPTY);

            // 2. Check if the crystal is already at the maximum capacity
            if (currentData.mana() >= 500f) {
                player.displayClientMessage(Component.literal("This crystal is already at maximum capacity!"), true);
                return InteractionResult.FAIL;
            }

            // 3. Check if the player has any Magic Crystals
            int crystalSlot = player.getInventory().findSlotMatchingItem(new ItemStack(ModItems.MAGIC_CRYSTAL.asItem()));
            if (crystalSlot == -1) {
                player.displayClientMessage(Component.literal("You have no Magic Crystals!"), true);
                return InteractionResult.FAIL;
            }

            // 4. Calculate new value with the 500 cap
            // Math.min picks the lower of the two values
            float newManaValue = Math.min(currentData.mana() + 10f, 500f);

            // 5. Update the item stack
            stack.set(ModComponents.mana_storage.value(), new ManaData(newManaValue));

            // 6. Consume the resource and provide feedback
            player.getInventory().getItem(crystalSlot).shrink(1);
            player.displayClientMessage(
                    Component.literal("Infused Mana! Current: " + (int)newManaValue + "/500"),
                    true
            );

            return InteractionResult.SUCCESS; // Changed to SUCCESS to trigger hand animation
        }

        if (item == ModItems.MAGIC_CRYSTAL.asItem()) {
            // 1. Get player mana data
            ElementalData playerLevelData = ElementalDataHandler.get(player);
            float currentPlayerMana = playerLevelData.getCurrentMana();
            float playerMax = playerLevelData.getMaxMana();

            // 2. Check if player is already full
            if (currentPlayerMana >= playerMax) {
                player.displayClientMessage(Component.literal("Your Mana is already full!"), true);
                return InteractionResult.FAIL;
            }

            // 3. Perform the logic on the Server
            if (!player.level().isClientSide()) {
                // Calculate how much to add (10 mana, but don't exceed max)
                float amountToAdd = Math.min(10f, playerMax - currentPlayerMana);
                float newManaValue = currentPlayerMana + amountToAdd;

                // Update player data
                playerLevelData.setCurrentMana(newManaValue);

                // Consume 1 crystal from the hand
                stack.shrink(1);

                // Save and Sync to ensure the client-side UI updates
                ElementalDataHandler.save(player);
                ElementalDataHandler.syncToClient(player);

                player.displayClientMessage(
                        Component.literal("Infused " + (int)amountToAdd + " Mana, " + (int)currentPlayerMana+"/"+(int)playerMax+"!"),
                        true
                );
            }

            return InteractionResult.SUCCESS;
        }

        // 2. LEVEL UP (LevelCrystal)
        if (item == ModItems.LEVEL_CRYSTAL.asItem()) {
            data.setLevel(data.getLevel() + 1);
            player.displayClientMessage(Component.literal("Your level has increased to " + data.getLevel() + ", and max mana to " + data.getMaxMana() + "!"), true);


            ElementalDataHandler.syncToClient(player);
            ElementalDataHandler.save(player);
            return InteractionResult.SUCCESS;
        }


        // 3. RADIUS INCREASE (RadiusCrystal)
        if (item == ModItems.RADIUS_CRYSTAL.asItem()) {
            // Check if we are already at or above the limit
            if (core.getRadius() >= 128.0f) {
                player.displayClientMessage(
                        Component.literal("The Kingdom Core has reached its maximum radius (128)!"),
                        true
                );
                return InteractionResult.FAIL; // Stops the process and doesn't shrink the stack
            }

            // Increase radius but clamp it at 128.0f just to be safe
            float newRadius = Math.min(core.getRadius() + 1.0f, 128.0f);
            core.setRadius(newRadius);

            // Crucial: Tell Minecraft the BlockEntity data has changed so it saves to NBT
            core.setChanged();

            stack.shrink(1);

            player.displayClientMessage(
                    Component.literal("The Kingdom Core radius increased to " + (int) newRadius),
                    true
            );
            return InteractionResult.CONSUME;
        }

        // 4. SLOT INCREASE (SlotCrystal)
        if (item == ModItems.SLOT_CRYSTAL.asItem()) {
            List<Integer> slots = data.getActiveSlots();
            int unlockedIndex = -1;

            // Find the first -1 in the list
            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i) == -1) {
                    slots.set(i, 0); // Change the first -1 to 0
                    unlockedIndex = i;
                    break; // Stop after unlocking one slot
                }
            }

            // Validation: Check if a slot was actually available to unlock
            if (unlockedIndex == -1) {
                player.displayClientMessage(
                        Component.literal("All slots are already unlocked!"),
                        true
                );
                return InteractionResult.FAIL;
            }

            // Success logic
            stack.shrink(1);

            // Using (unlockedIndex + 1) makes it "Slot 1" instead of "Slot 0" for the user
            player.displayClientMessage(
                    Component.literal("You have unlocked Slot " + (unlockedIndex + 1) + "!"),
                    true
            );

            ElementalDataHandler.syncToClient(player);
            ElementalDataHandler.save(player);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static InteractionResult openRuneCutter(Level world, Player player, KingdomCoreBlockEntity core) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;

        if (player instanceof ServerPlayer serverPlayer) {
            ResourceManager manager = serverPlayer.level().getServer().getResourceManager();

            for (int spellId : SpellRegistry.getAllSpellIds()) {
                if (spellId <= 0) continue;

                ClientSpellInfo info = SpellInfoServerHelper.loadSpellInfo(spellId, manager);

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

            BalmMenuProvider<ModMenuTypes.RuneCutterData> runeCutterProvider =
                    new BalmMenuProvider<>() {

                        @Override
                        public Component getDisplayName() {
                            return Component.translatable("container.theelemental.rune_cutter");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player p) {
                            return new ElementalRuneCutterMenu(
                                    windowId,
                                    inv,
                                    ContainerLevelAccess.NULL,
                                    core.getElement()
                            );
                        }

                        @Override
                        public ModMenuTypes.RuneCutterData getScreenOpeningData(ServerPlayer player) {
                            return new ModMenuTypes.RuneCutterData(core.getElement());
                        }

                        @Override
                        public StreamCodec<RegistryFriendlyByteBuf, ModMenuTypes.RuneCutterData>
                        getScreenStreamCodec() {
                            return ModMenuTypes.RuneCutterData.STREAM_CODEC;
                        }
                    };

            Balm.networking().openMenu(serverPlayer, runeCutterProvider);
        }

        return InteractionResult.CONSUME;
    }
}
