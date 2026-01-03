package com.teamneon.theelemental.network;

import com.teamneon.theelemental.Theelemental;
import com.teamneon.theelemental.data.ElementalDataHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public record SyncElementalDataPacket(
        float currentMana,
        int level,
        int element,
        List<Integer> unlockedSpells,
        List<Integer> activeSlots
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncElementalDataPacket> TYPE =
            new CustomPacketPayload.Type<>(Theelemental.id("sync_elemental_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncElementalDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SyncElementalDataPacket::currentMana,
            ByteBufCodecs.INT, SyncElementalDataPacket::level,
            ByteBufCodecs.INT, SyncElementalDataPacket::element,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), SyncElementalDataPacket::unlockedSpells,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), SyncElementalDataPacket::activeSlots,
            SyncElementalDataPacket::new
    );

    public static void handle(Player player, SyncElementalDataPacket message) {
        // Client-side only
        net.minecraft.client.Minecraft.getInstance().execute(() -> {
            com.teamneon.theelemental.client.ClientElementalData.update(
                    message.currentMana(),
                    message.level(),
                    message.element(),
                    message.unlockedSpells(),
                    message.activeSlots()
            );
        });
    }



    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}