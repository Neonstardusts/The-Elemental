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

                "steve_use_core.png",

                "steve_use_core.png"
        );

        List<Component> lines = List.of(
                Component.literal("Use on a desired location"),
                Component.literal("creating the core of your kingdom."),

                Component.literal("Double {Shift+Right Click}"),
                Component.literal("on the kingdom core, to teleport to spawn."),

                Component.literal("{Right Click} to open the soul forge"),
                Component.literal("the place where you assign spells.")
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

        // Only elemental creatures can create kingdoms
        int playerElement = ElementalDataHandler.get(player).getElement();
        if (playerElement <= 0) {
            player.displayClientMessage(Component.literal("Only an elemental creature can create a kingdom!"), true);
            return InteractionResult.FAIL;
        }

        BlockPos corePos = clickedPos.above();
        KingdomSavedData globalData = KingdomSavedData.get(serverLevel);
        BlockPos existingCorePos = globalData.getCorePos(playerElement);

        // Otherwise, check restrictions
        Component tooCloseMessage = getTooCloseMessage(corePos, serverLevel, globalData);

        if (tooCloseMessage != null) {
            player.displayClientMessage(tooCloseMessage, true);
            return InteractionResult.FAIL;
        }


        // Kingdom exists? Teleport player to it
        if (existingCorePos != null) {
            if (serverLevel.getBlockEntity(existingCorePos) instanceof KingdomCoreBlockEntity core) {

                UUID playerId = player.getUUID();

                // Check if player is already a member
                if (core.getMembers().contains(playerId)) {
                    player.displayClientMessage(
                            Component.literal("You are already a member of this Kingdom!"),
                            true
                    );
                    return InteractionResult.FAIL; // Stop here if already a member
                }

                // Player is not a member yet, add them
                core.addMember(playerId);

                player.displayClientMessage(
                        Component.literal("Kingdom already exists! Teleporting and joining..."),
                        true
                );

                serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal(
                                player.getName().getString()
                                        + " has joined the "
                                        + ElementRegistry.getName(playerElement)
                                        + " Kingdom!"
                        ),
                        false
                );


                if (player instanceof ServerPlayer serverPlayer) {
                    teleportToNearestSafeSpot(serverPlayer, existingCorePos.getX(), existingCorePos.getY(), existingCorePos.getZ(), ElementRegistry.getColor(playerElement));
                }
//updated
                context.getItemInHand().shrink(1);
                return InteractionResult.SUCCESS;
            }
        }


        if (!serverLevel.dimension().equals(Level.OVERWORLD)) {
            player.displayClientMessage(Component.literal("Kingdoms can only be created in the Overworld!"), true);
            return InteractionResult.FAIL;
        }


        serverLevel.setBlock(
                corePos,
                ModBlocks.KINGDOM_CORE
                        .defaultBlockState()
                        .setValue(KingdomCoreBlock.ELEMENTCore, playerElement),
                3
        );
        globalData.registerCore(playerElement, corePos);
        BlockPos anchorPos = KingdomAnchorHelper.getAnchorPos(serverLevel, playerElement);
        if (anchorPos != null) {
            serverLevel.setBlock(
                    anchorPos,
                    ModBlocks.KINGDOM_ANCHOR
                            .defaultBlockState()
                            .setValue(KingdomAnchor.ELEMENT, playerElement),
                    3
            );

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

        // Check spawn
        BlockPos spawn = globalData.getCorePos(-1);
        if (pos.distSqr(spawn) < maxRadius * maxRadius) {
            return Component.literal("You cannot create a Kingdom too close to the ")
                    .append(Component.literal("World Altar").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal("!"));
        }

        // Check all other kingdoms
        for (int element = 1; element <= 9; element++) {
            BlockPos corePos = globalData.getCorePos(element);
            if (corePos != null && pos.distSqr(corePos) < maxRadius * maxRadius) {
                String elementName = ElementRegistry.getName(element); // name of kingdom
                int color = ElementRegistry.getColor(element); // color

                return Component.literal("You cannot create a Kingdom too close to the ")
                        .append(Component.literal(elementName + " Kingdom!").withStyle(style -> style.withColor(color)));
            }
        }

        return null; // safe
    }


}
