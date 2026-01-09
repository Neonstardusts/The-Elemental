package com.teamneon.theelemental.menu;

import com.teamneon.theelemental.Theelemental;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.helpers.UtilityHelper;
import com.teamneon.theelemental.item.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

public record C2SChooseElementPacket(int elementId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<C2SChooseElementPacket> TYPE =
            new CustomPacketPayload.Type<>(Theelemental.id("choose_element"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SChooseElementPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, C2SChooseElementPacket::elementId,
                    C2SChooseElementPacket::new
            );

    public static void handle(Player player, C2SChooseElementPacket message) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;

        int element = message.elementId();

        // 1. Existing Logic: Set and Sync Data
        ElementalDataHandler.get(serverPlayer).setElement(message.elementId());
        ElementalDataHandler.syncToClient(serverPlayer);
        ElementalDataHandler.save(serverPlayer);

        // 2. Play Sound (To the player and people nearby)
        // Parameters: Sound, Source, X, Y, Z, Volume, Pitch
        serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.BEACON_POWER_SELECT,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 2.0f);

        // 3. Send Action Bar Message
        // The 'true' boolean at the end specifies it should go to the Action Bar (above hotbar)
        String elementName = ElementRegistry.getName(element);
        String elementRace = ElementRegistry.getRace(element);
        int color = ElementRegistry.getColor(element);

        // Create styled components
        Component playerName = serverPlayer.getName();

        // 3. Prepare the " has become a " part (Standard white text)
        Component hasBecome = Component.literal(" has become a ").withStyle(s -> s.withColor(0xFFFFFF));
        Component prefix = Component.literal("You have became a ").withStyle(style -> style.withColor(0xFFFFFF)); // white
        Component kingdom = Component.literal(elementName + " " + elementRace + "!").withStyle(style -> style.withColor(color));

        // Combine them
        Component messageDisplay = Component.empty().append(prefix).append(kingdom);

        // Send to player as action bar
        player.displayClientMessage(messageDisplay, true);

        Component globalMessage = Component.empty()
                .append(playerName)
                .append(hasBecome)
                .append(kingdom);



        // Spawn Particles
        // We cast to ServerLevel to use the sendParticles method which ensures everyone nearby sees them
        if (serverPlayer.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            UtilityHelper.spawnDust(serverLevel, serverPlayer.getX(), serverPlayer.getY()+1, serverPlayer.getZ(), color, 50,0.5, 0.75, 0.5, 0.02, 3);
            net.minecraft.server.MinecraftServer server = serverLevel.getServer();
            if (server != null) {
                server.getPlayerList().broadcastSystemMessage(globalMessage, false);
            }
        }

        boolean added = serverPlayer.getInventory().add(ModItems.KINGDOM_CORE_ITEM.createStack());

        // If inventory is full, drop it on the ground
        if (!added) {
            serverPlayer.drop(ModItems.KINGDOM_CORE_ITEM.createStack(), false);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
