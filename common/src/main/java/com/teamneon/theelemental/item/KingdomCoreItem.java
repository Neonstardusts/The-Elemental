package com.teamneon.theelemental.item;

import com.teamneon.theelemental.block.KingdomAnchor;
import com.teamneon.theelemental.block.KingdomCoreBlock;
import com.teamneon.theelemental.block.ModBlocks;
import com.teamneon.theelemental.block.entity.KingdomCoreBlockEntity;
import com.teamneon.theelemental.client.tooltip.InfoTooltipComponentData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.helpers.KingdomAnchorHelper;
import com.teamneon.theelemental.kingdoms.KingdomSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.teamneon.theelemental.helpers.UtilityHelper.teleportToNearestSafeSpot;

public class KingdomCoreItem extends Item {

    public KingdomCoreItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    int maxRadius = 2 * 128; // 256 blocks


    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        // This will automatically handle the Shift logic in the InfoTooltipComponent
        List<String> textures = List.of(
                "steve_place_new.png",

                "steve_teleport.png"
        );

        List<Component> lines = List.of(
                Component.literal("Use on a desired location"),
                Component.literal("creating the core of your kingdom."),

                Component.literal("Using it on the elemental altar will randomly disperse you."),
                Component.literal("If your elements kingdom already exists you will teleport there.")


        );

        return Optional.of(new InfoTooltipComponentData(textures, lines));
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ServerLevel serverLevel = (ServerLevel) level;
        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        // 1. Requirement: Only elemental creatures
        int playerElement = ElementalDataHandler.get(player).getElement();
        if (playerElement <= 0) {
            player.displayClientMessage(Component.literal("Only an elemental creature can create a kingdom!"), true);
            return InteractionResult.FAIL;
        }

        // 2. Interaction: Elemental Altar (Spread Players)
        // Assuming the Altar is the "World Altar" registered with element -1 or a specific block
        if (clickedState.is(ModBlocks.ELEMENTAL_ALTAR.asBlock())) { // Replace with your actual Altar block check
            serverLevel.getServer().getCommands().performPrefixedCommand(
                    serverLevel.getServer().createCommandSourceStack(),
                    String.format("spreadplayers %d %d 500 2000 false @p", clickedPos.getX(), clickedPos.getZ())
            );
            return InteractionResult.SUCCESS;
        }

        KingdomSavedData globalData = KingdomSavedData.get(serverLevel);
        BlockPos existingCorePos = globalData.getCorePos(playerElement);

        // 3. Logic: Teleport to existing Kingdom (Skip proximity checks)
        if (existingCorePos != null) {
            if (serverLevel.getBlockEntity(existingCorePos) instanceof KingdomCoreBlockEntity core) {
                UUID playerId = player.getUUID();

                if (!core.getMembers().contains(playerId)) {
                    core.addMember(playerId);
                    serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                            Component.literal(player.getName().getString() + " has joined the " + ElementRegistry.getName(playerElement) + " Kingdom!"),
                            false
                    );
                }

                player.displayClientMessage(Component.literal("Teleporting to your Kingdom core..."), true);
                if (player instanceof ServerPlayer serverPlayer) {
                    teleportToNearestSafeSpot(serverPlayer, existingCorePos.getX(), existingCorePos.getY(), existingCorePos.getZ(), ElementRegistry.getColor(playerElement));
                }

                context.getItemInHand().shrink(1);
                return InteractionResult.SUCCESS;
            }
        }

        // 4. Placement logic: Only check distance if we are creating a NEW one
        BlockPos corePos = clickedPos.above();

        if (!serverLevel.dimension().equals(Level.OVERWORLD)) {
            player.displayClientMessage(Component.literal("Kingdoms can only be created in the Overworld!"), true);
            return InteractionResult.FAIL;
        }

        Component tooCloseMessage = getTooCloseMessage(corePos, serverLevel, globalData);
        if (tooCloseMessage != null) {
            player.displayClientMessage(tooCloseMessage, true);
            return InteractionResult.FAIL;
        }

        // 5. Create the Kingdom
        serverLevel.setBlock(corePos, ModBlocks.KINGDOM_CORE.asBlock().defaultBlockState().setValue(KingdomCoreBlock.ELEMENTCore, playerElement), 3);
        globalData.registerCore(playerElement, corePos);

        // Anchor Logic
        BlockPos anchorPos = KingdomAnchorHelper.getAnchorPos(serverLevel, playerElement);
        if (anchorPos != null) {
            serverLevel.setBlock(anchorPos, ModBlocks.KINGDOM_ANCHOR.asBlock().defaultBlockState().setValue(KingdomAnchor.ELEMENT, playerElement), 3);
        }

        if (serverLevel.getBlockEntity(corePos) instanceof KingdomCoreBlockEntity core) {
            core.setOwner(player.getUUID());
            core.setElement(playerElement);
        }

        serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal(player.getName().getString() + " has created the " + ElementRegistry.getName(playerElement) + " Kingdom!"),
                false
        );

        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    private Component getTooCloseMessage(BlockPos pos, ServerLevel level, KingdomSavedData globalData) {
        // Check World Altar
        BlockPos spawn = globalData.getCorePos(-1);
        if (spawn != null) {
            double dist = Math.sqrt(pos.distSqr(spawn));
            if (dist < maxRadius) {
                return Component.literal("Too close to the ")
                        .append(Component.literal("World Altar").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(String.format(" (%.0f/%d blocks)", dist, maxRadius)));
            }
        }

        // Check Other Kingdoms
        for (int element = 1; element <= 9; element++) {
            BlockPos otherPos = globalData.getCorePos(element);
            if (otherPos != null) {
                double dist = Math.sqrt(pos.distSqr(otherPos));
                if (dist < maxRadius) {
                    String elementName = ElementRegistry.getName(element);
                    int color = ElementRegistry.getColor(element);

                    return Component.literal("Too close to the ")
                            .append(Component.literal(elementName + " Kingdom!").withStyle(style -> style.withColor(color)))
                            .append(Component.literal(String.format(" (%.0f/%d blocks)", dist, maxRadius)));
                }
            }
        }
        return null;
    }


}
