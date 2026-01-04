package com.teamneon.theelemental.menu;

import com.teamneon.theelemental.Theelemental;
import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static com.teamneon.theelemental.Theelemental.id;

public record C2SAssignSpellsPacket(List<Integer> spellIds) implements CustomPacketPayload {

    public static final Type<C2SAssignSpellsPacket> TYPE = new Type<>(id("assign_spells"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SAssignSpellsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
            C2SAssignSpellsPacket::spellIds,
            C2SAssignSpellsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(Player player) {
        ElementalData data = ElementalDataHandler.get(player);

        // Update the RAM cache
        for (int i = 0; i < spellIds.size(); i++) {
            // Safety check: ensure we don't overwrite locked slots (-1)
            if (data.getActiveSlots().get(i) != -1) {
                data.setSlot(i, spellIds.get(i));
            }
        }

        // Push to NBT and sync back to client
        ElementalDataHandler.save(player);
        ElementalDataHandler.syncToClient(player);
    }
}