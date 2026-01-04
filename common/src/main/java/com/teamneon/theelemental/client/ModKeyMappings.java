    package com.teamneon.theelemental.client;

    import com.mojang.blaze3d.platform.InputConstants;
    import com.teamneon.theelemental.helpers.SpellJsonLoader;
    import com.teamneon.theelemental.magic.base.Spell;
    import com.teamneon.theelemental.magic.base.SpellCastResult;
    import com.teamneon.theelemental.magic.network.CanCastSpellPacket;
    import com.teamneon.theelemental.magic.network.SpellCastPacket;
    import com.teamneon.theelemental.magic.base.SpellRegistry;
    import net.blay09.mods.balm.Balm;
    import net.blay09.mods.kuma.api.InputBinding;
    import net.blay09.mods.kuma.api.Kuma;
    import net.blay09.mods.kuma.api.ManagedKeyMapping;
    import com.teamneon.theelemental.Theelemental;
    import net.minecraft.client.Minecraft;

    import java.util.Map;

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

                        // 1️⃣ Ask server if the spell can be cast
                        Balm.networking().sendToServer(new CanCastSpellPacket(spellId, slotIndex));

                        // 2️⃣ Do NOT start cooldown here — wait for server reply
                        return true;
                    })
                    .build();


        }
    }