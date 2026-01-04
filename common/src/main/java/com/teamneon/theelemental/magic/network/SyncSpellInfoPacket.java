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
        int manaCost,
        int cooldownTicks
) implements CustomPacketPayload {

    public static final Type<SyncSpellInfoPacket> TYPE =
            new Type<>(Theelemental.id("sync_spell_info"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSpellInfoPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, SyncSpellInfoPacket::spellId,
                    ByteBufCodecs.STRING_UTF8, SyncSpellInfoPacket::name,
                    ByteBufCodecs.INT, SyncSpellInfoPacket::manaCost,
                    ByteBufCodecs.INT, SyncSpellInfoPacket::cooldownTicks,
                    SyncSpellInfoPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Handle packet on the CLIENT */
    public static void handle(Player player, SyncSpellInfoPacket message) {
        Minecraft.getInstance().execute(() -> {
            // Construct the ClientSpellInfo object
            ClientSpellInfo info = new ClientSpellInfo(
                    message.spellId(),
                    message.name(),
                    message.manaCost(),
                    message.cooldownTicks()
            );

            // Register it in the client cache
            ClientSpellRegistry.registerSpell(info);
        });
    }
}
