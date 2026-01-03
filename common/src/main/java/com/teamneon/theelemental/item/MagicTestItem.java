package com.teamneon.theelemental.item;

import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import net.minecraft.network.chat.Component;
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

            // 3. Set FIRST active slot to random spell
            data.setSlot(0, 1);
            data.setSlot(4, 8001);
            data.setSlot(3, 6001);
            data.setSlot(2, 3001);

            // 4. Sync + Save
            ElementalDataHandler.syncToClient(player);
            ElementalDataHandler.save(player);


            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}