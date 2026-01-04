package com.teamneon.theelemental.network;

import com.teamneon.theelemental.magic.network.SpellCastPacket;
import com.teamneon.theelemental.menu.C2SAssignSpellsPacket;
import net.blay09.mods.balm.network.BalmNetworking;

public class ModNetworking {

    public static void initialize(BalmNetworking networking) {
        // S2C: Syncing data to the client (Your existing packet)
        networking.registerClientboundPacket(
                SyncElementalDataPacket.TYPE,
                SyncElementalDataPacket.class,
                SyncElementalDataPacket.STREAM_CODEC,
                SyncElementalDataPacket::handle
        );

        // C2S: Explicitly defining the handler to avoid conversion errors
        networking.registerServerboundPacket(
                SpellCastPacket.TYPE,
                SpellCastPacket.class,
                SpellCastPacket.STREAM_CODEC,
                (player, message) -> SpellCastPacket.handle(message, player)
        );

        // C2S: Handle Soul Forge Assignment
        networking.registerServerboundPacket(
                C2SAssignSpellsPacket.TYPE,
                C2SAssignSpellsPacket.class,
                C2SAssignSpellsPacket.STREAM_CODEC,
                (player, message) -> message.handle(player)
        );
    }
}