package com.teamneon.theelemental.network;

import com.teamneon.theelemental.magic.network.CanCastSpellPacket;
import com.teamneon.theelemental.magic.network.CanCastSpellResultPacket;
import com.teamneon.theelemental.magic.network.SpellCastPacket;
import com.teamneon.theelemental.magic.network.SyncSpellInfoPacket;
import com.teamneon.theelemental.menu.C2SAssignSpellsPacket;
import com.teamneon.theelemental.menu.C2SChooseElementPacket;
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

        // S2C: Sync spell metadata
        networking.registerClientboundPacket(
                SyncSpellInfoPacket.TYPE,
                SyncSpellInfoPacket.class,
                SyncSpellInfoPacket.STREAM_CODEC,
                SyncSpellInfoPacket::handle
        );

        // C2S: Client asks server “can I cast this spell?”
        networking.registerServerboundPacket(
                CanCastSpellPacket.TYPE,
                CanCastSpellPacket.class,
                CanCastSpellPacket.STREAM_CODEC,
                (player, message) -> CanCastSpellPacket.handle(message, player)
        );

// S2C: Server responds whether the cast is allowed
        networking.registerClientboundPacket(
                CanCastSpellResultPacket.TYPE,
                CanCastSpellResultPacket.class,
                CanCastSpellResultPacket.STREAM_CODEC,
                (player, message) -> CanCastSpellResultPacket.handle(player, message)
        );

        // C2S: ElementChooser packet
        networking.registerServerboundPacket(
                C2SChooseElementPacket.TYPE,
                C2SChooseElementPacket.class,
                C2SChooseElementPacket.STREAM_CODEC,
                (player, message) -> C2SChooseElementPacket.handle(player, message)
        );

    }
}