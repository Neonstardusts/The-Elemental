package com.teamneon.theelemental.item;

import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.magic.base.SpellRegistry;
import com.teamneon.theelemental.magic.network.SyncSpellInfoPacket;
import net.blay09.mods.balm.Balm;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.List;

public class MagicTestItem extends Item {

    public MagicTestItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ElementalData data = ElementalDataHandler.get(player);

            //set mana to 0
            data.setCurrentMana(0);

            // Set active slots
            int[] spellIds = {1, 0, 3001, 6001, 8001}; // slots 0-4

            for (int i = 0; i < spellIds.length; i++) {
                data.setSlot(i, spellIds[i]);

                // 1️⃣ Sync spell info to client for each new spell
                if (spellIds[i] > 0) {
                    ResourceManager manager = player.level().getServer().getResourceManager();
                    var spell = SpellRegistry.getSpell(spellIds[i], manager); // server-side
                    if (spell != null) {
                        Balm.networking().sendTo(player, new SyncSpellInfoPacket(
                                spellIds[i],
                                spell.getName(),
                                spell.getManaCost(),
                                spell.getCooldownTicks()
                        ));
                    }
                }
            }

            // 2️⃣ Sync slots + save
            ElementalDataHandler.syncToClient(player);
            ElementalDataHandler.save(player);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}