package com.teamneon.theelemental.magic.network;

import com.teamneon.theelemental.Theelemental;
import com.teamneon.theelemental.magic.system.SpellExecutor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record SpellCastPacket(int spellId) implements CustomPacketPayload {

    public static final Type<SpellCastPacket> TYPE =
            new Type<>(Theelemental.id("spell_cast"));

    // We use RegistryFriendlyByteBuf to stay consistent with your sync packet
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellCastPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SpellCastPacket::spellId,
            SpellCastPacket::new
    );

    /**
     * Handled on the SERVER side.
     * Note: Balm uses (Message, ServerPlayer) for serverbound handlers.
     */
    /**
     * Corrected Handler Signature for Balm
     * Argument 1: The Packet Record
     * Argument 2: The ServerPlayer
     */
    public static void handle(SpellCastPacket message, ServerPlayer player) {
        // Run on server thread to avoid thread-safety issues
        player.level().getServer().execute(() -> {
            SpellExecutor.tryCast(player, message.spellId());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}