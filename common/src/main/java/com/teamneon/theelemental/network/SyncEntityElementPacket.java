package com.teamneon.theelemental.network;

import com.teamneon.theelemental.Theelemental;
import com.teamneon.theelemental.client.ClientElementalData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record SyncEntityElementPacket(int entityId, int elementId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncEntityElementPacket> TYPE =
            new CustomPacketPayload.Type<>(Theelemental.id("sync_entity_element"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityElementPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncEntityElementPacket::entityId,
            ByteBufCodecs.VAR_INT, SyncEntityElementPacket::elementId,
            SyncEntityElementPacket::new
    );

    public static void handle(Player player, SyncEntityElementPacket message) {
        // Run on client main thread
        net.minecraft.client.Minecraft.getInstance().execute(() -> {
            ClientElementalData.setEntityElement(message.entityId(), message.elementId());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}