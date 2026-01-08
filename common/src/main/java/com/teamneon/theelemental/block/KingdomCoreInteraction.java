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

        // Ignore top & bottom
        if (face == Direction.UP || face == Direction.DOWN) {
            return InteractionResult.PASS;
        }

        return switch (face) {
            case NORTH -> handleTeleport(world, player, core);
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

        if (player.getUUID().equals(core.getOwner()) || core.getMembers().contains(player.getUUID())) {
            player.displayClientMessage(Component.literal("You opened the Kingdom Core!"), true);

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
        } else {
            player.displayClientMessage(Component.literal("You are not in this kingdom!"), true);
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

        if (stack.getItem() == Items.AMETHYST_SHARD) {
            ElementalData data = ElementalDataHandler.get(player);

            if (data.getElement() != core.getElement()) {
                player.displayClientMessage(
                        Component.literal("Your element does not match this Kingdom Core!"),
                        true
                );
                return InteractionResult.FAIL;
            }

            data.setCurrentMana(data.getCurrentMana() + 10f);
            stack.shrink(1);

            player.displayClientMessage(Component.literal("Your mana increased by 10!"), true);
            return InteractionResult.CONSUME;
        }

        if (stack.getItem() == Items.DIAMOND) {
            float newRadius = Math.min(core.getRadius() + 1.0f, 128.0f);
            core.setRadius(newRadius);
            stack.shrink(1);

            player.displayClientMessage(
                    Component.literal("The Kingdom Core radius increased to " + (int) newRadius),
                    true
            );
            return InteractionResult.CONSUME;
        }

        return InteractionResult.CONSUME;
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
