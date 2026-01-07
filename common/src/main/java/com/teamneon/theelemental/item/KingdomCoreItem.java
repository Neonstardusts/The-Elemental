package com.teamneon.theelemental.item;

import com.teamneon.theelemental.block.KingdomAnchor;
import com.teamneon.theelemental.block.KingdomCoreBlock;
import com.teamneon.theelemental.block.ModBlocks;
import com.teamneon.theelemental.block.entity.KingdomCoreBlockEntity;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.helpers.KingdomAnchorHelper;
import com.teamneon.theelemental.kingdoms.KingdomSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.UUID;

import static com.teamneon.theelemental.helpers.UtilityHelper.teleportToNearestSafeSpot;

public class KingdomCoreItem extends Item {

    public KingdomCoreItem(Properties properties) {
        super(properties.stacksTo(1));
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

        KingdomSavedData globalData = KingdomSavedData.get(serverLevel);
        BlockPos existingCorePos = globalData.getCorePos(playerElement);

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

        // Otherwise, create the kingdom
        BlockPos corePos = clickedPos.above();
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
}
