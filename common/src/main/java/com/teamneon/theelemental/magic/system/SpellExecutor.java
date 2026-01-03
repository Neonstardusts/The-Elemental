package com.teamneon.theelemental.magic.system;

import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellCastResult;
import com.teamneon.theelemental.magic.base.SpellRegistry;
import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.Theelemental;
import net.minecraft.server.level.ServerPlayer;

public class SpellExecutor {

    public static void tryCast(ServerPlayer player, int slotIndex) {
        ElementalData data = ElementalDataHandler.get(player);
        if (data == null) {
            Theelemental.logger.error("EXECUTION FAILED: ElementalData is NULL for player " + player.getName().getString());
            return;
        }

        // 1. Identify which spell is in the selected slot
        if (slotIndex < 0 || slotIndex >= data.getActiveSlots().size()) return;
        int spellId = data.getActiveSlots().get(slotIndex);

        // 2. Lookup the Spell logic
        Spell spell = SpellRegistry.getSpell(spellId);
        if (spellId <= 0 || spell == null) return;

        long currentTime = player.level().getGameTime();
        boolean onCooldown = data.isSpellOnCooldown(spellId, currentTime);
        int currentMana = (int) data.getCurrentMana();

        // 3. Check all conditions (mana, cooldown, spell-specific)
        SpellCastResult check = spell.checkCast(player, player.level(), currentMana, onCooldown);
        if (!check.isSuccess()) {
            if (check.getReason() != null) {
                Theelemental.logger.warn("Spell cast blocked: " + check.getReason());
            } else {
                Theelemental.logger.warn("Spell cast blocked (no reason provided).");
            }
            return; // blocked, no cooldown applied
        }

        // 4. Execute the spell
        SpellCastResult result = spell.execute(player.level(), player);
        if (result.isSuccess()) {
            // 5. Apply costs and cooldown
            data.setCurrentMana(currentMana - spell.getManaCost());
            data.setSpellCooldown(spellId, spell.getCooldownTicks(), currentTime);

            // 6. Sync updates to client
            ElementalDataHandler.syncToClient(player);
        } else {
            // Execution failed (e.g., teleport blocked)
            if (result.getReason() != null) {
                Theelemental.logger.warn("Spell execution failed: " + result.getReason());
            } else {
                Theelemental.logger.warn("Spell execution failed (no reason provided).");
            }
        }
    }
}
