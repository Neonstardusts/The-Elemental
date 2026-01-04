package com.teamneon.theelemental.magic.network;

import com.teamneon.theelemental.Theelemental;
import com.teamneon.theelemental.magic.system.SpellExecutor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record CanCastSpellPacket(int spellId, int slotIndex) implements CustomPacketPayload {

    public static final Type<CanCastSpellPacket> TYPE =
            new Type<>(Theelemental.id("can_cast_spell"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CanCastSpellPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, CanCastSpellPacket::spellId,
                    ByteBufCodecs.INT, CanCastSpellPacket::slotIndex,
                    CanCastSpellPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Server-side handler */
    public static void handle(CanCastSpellPacket message, ServerPlayer player) {
        Theelemental.logger.info("[tryCanCast] Received request for spellId: " + message.spellId() + " slot: " + message.slotIndex() + " from player: " + player.getName().getString());
        player.level().getServer().execute(() -> {
            // Use your SpellExecutor helper
            SpellExecutor.tryCanCast(player, message.spellId(), message.slotIndex());
        });
    }
}
