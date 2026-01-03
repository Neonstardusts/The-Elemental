    package com.teamneon.theelemental.client;

    import com.mojang.blaze3d.platform.InputConstants;
    import com.teamneon.theelemental.magic.base.Spell;
    import com.teamneon.theelemental.magic.base.SpellCastResult;
    import com.teamneon.theelemental.magic.network.SpellCastPacket;
    import com.teamneon.theelemental.magic.base.SpellRegistry;
    import net.blay09.mods.balm.Balm;
    import net.blay09.mods.kuma.api.InputBinding;
    import net.blay09.mods.kuma.api.Kuma;
    import net.blay09.mods.kuma.api.ManagedKeyMapping;
    import com.teamneon.theelemental.Theelemental;
    import net.minecraft.client.Minecraft;

    import static com.teamneon.theelemental.Theelemental.id;

    public class ModKeyMappings {

        public static ManagedKeyMapping cycleSlotKey;
        public static ManagedKeyMapping castSpellKey;

        public static void initialize() {
            // --- Cycle Slot Key (B) ---
            cycleSlotKey = Kuma.createKeyMapping(id("cycle_slot"))
                    .withDefault(InputBinding.key(InputConstants.KEY_B))
                    .handleWorldInput(event -> {
                        ClientElementalData.nextSlot();
                        return true;
                    })
                    .build();

            // --- Cast Spell Key (N) ---
            castSpellKey = Kuma.createKeyMapping(id("cast_spell"))
                    .withDefault(InputBinding.key(InputConstants.KEY_N))
                    .handleWorldInput(event -> {
                int slotIndex = ClientElementalData.getCurrentSlot();
                int spellId = ClientElementalData.get().getActiveSlots().get(slotIndex);

                if (spellId <= 0) return false;

                Spell spell = SpellRegistry.getSpell(spellId);
                int currentMana = (int) ClientElementalData.get().getCurrentMana();
                boolean onCooldown = ClientElementalData.isSpellOnCooldown(spellId);

                // 1. Test all conditions
                SpellCastResult result = spell.checkCast(Minecraft.getInstance().player, Minecraft.getInstance().level, currentMana, onCooldown);

                if (!result.isSuccess()) {
                    if (result.getReason() != null) {
                        Minecraft.getInstance().player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal(result.getReason()), true
                        );
                    }
                    return false;
                }

                // 2. Start local cooldown
                int duration = spell.getCooldownTicks();
                long time = Minecraft.getInstance().level.getGameTime();
                ClientElementalData.setLocalCooldown(spellId, duration, time);

                // 3. Send packet to server
                Balm.networking().sendToServer(new SpellCastPacket(slotIndex));
                return true;
            }).build();
        }
    }