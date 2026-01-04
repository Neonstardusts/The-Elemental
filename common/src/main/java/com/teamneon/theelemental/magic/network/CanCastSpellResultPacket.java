package com.teamneon.theelemental.magic.network;

import com.teamneon.theelemental.Theelemental;
import com.teamneon.theelemental.client.ClientElementalData;
import com.teamneon.theelemental.client.ClientSpellRegistry;
import net.blay09.mods.balm.Balm;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record CanCastSpellResultPacket(
        int spellId,
        int successInt, // 0 = false, 1 = true
        String reason,
        int slotIndex // Add this!
) implements CustomPacketPayload {

    public static final Type<CanCastSpellResultPacket> TYPE =
            new Type<>(Theelemental.id("can_cast_spell_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CanCastSpellResultPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, CanCastSpellResultPacket::spellId,
                    ByteBufCodecs.INT, CanCastSpellResultPacket::successInt,
                    ByteBufCodecs.STRING_UTF8, CanCastSpellResultPacket::reason,
                    ByteBufCodecs.INT, CanCastSpellResultPacket::slotIndex,
                    (spellId, successInt, reason, slotIndex) -> new CanCastSpellResultPacket(spellId, successInt, reason, slotIndex)
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Client-side handler */
    public static void handle(Player player, CanCastSpellResultPacket message) {
        Minecraft.getInstance().execute(() -> {
            boolean success = message.successInt != 0;

            if (!success) {
                Minecraft.getInstance().player.displayClientMessage(
                        Component.literal(message.reason()), true
                );
                Theelemental.logger.info("[Client] Spell blocked: " + message.reason());
                return;
            }

            // ✅ Spell can cast — start local cooldown
            int cooldown = ClientSpellRegistry.getSpell(message.spellId()).cooldownTicks;
            long time = Minecraft.getInstance().level.getGameTime();
            ClientElementalData.setLocalCooldown(message.spellId(), cooldown, time);
            Theelemental.logger.info("[Client] Spell can cast, starting local cooldown for spellId: " + message.spellId());

            // ✅ Now actually request the server to cast
            Balm.networking().sendToServer(new SpellCastPacket(message.slotIndex()));
        });
    }
}
