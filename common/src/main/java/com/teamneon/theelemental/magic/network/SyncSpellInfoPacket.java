package com.teamneon.theelemental.magic.network;

import com.teamneon.theelemental.Theelemental;
import com.teamneon.theelemental.client.ClientSpellInfo;
import com.teamneon.theelemental.client.ClientSpellRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record SyncSpellInfoPacket(
        int spellId,
        String name,
        String description, // Added field
        int manaCost,
        int cooldownTicks,
        int durationTicks,
        int requiredLevel
) implements CustomPacketPayload {

    public static final Type<SyncSpellInfoPacket> TYPE =
            new Type<>(Theelemental.id("sync_spell_info"));

    // Updated STREAM_CODEC to include the String description
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSpellInfoPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SyncSpellInfoPacket::spellId,
                    ByteBufCodecs.STRING_UTF8, SyncSpellInfoPacket::name,
                    ByteBufCodecs.STRING_UTF8, SyncSpellInfoPacket::description, // Added codec
                    ByteBufCodecs.INT, SyncSpellInfoPacket::manaCost,
                    ByteBufCodecs.INT, SyncSpellInfoPacket::cooldownTicks,
                    ByteBufCodecs.INT, SyncSpellInfoPacket::durationTicks,
                    ByteBufCodecs.INT, SyncSpellInfoPacket::requiredLevel,
                    SyncSpellInfoPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Handle packet on the CLIENT */
    public static void handle(Player player, SyncSpellInfoPacket message) {
        Minecraft.getInstance().execute(() -> {
            // Construct the ClientSpellInfo object using data from the packet
            ClientSpellInfo info = new ClientSpellInfo(
                    message.spellId(),
                    message.name(),
                    message.description(), // Use the description from the message
                    message.manaCost(),
                    message.cooldownTicks(),
                    message.durationTicks(),
                    message.requiredLevel()
            );

            // Register it in the client cache
            ClientSpellRegistry.registerSpell(info);
        });
    }
}