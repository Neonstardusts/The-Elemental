package com.teamneon.theelemental.events;

import com.teamneon.theelemental.block.ModBlocks;
import com.teamneon.theelemental.client.ClientSpellInfo;
import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.TempBlock;
import com.teamneon.theelemental.item.ModItems;
import com.teamneon.theelemental.magic.base.ActiveSpellManager;
import com.teamneon.theelemental.magic.base.SpellRegistry;
import com.teamneon.theelemental.magic.network.SyncSpellInfoPacket;
import com.teamneon.theelemental.magic.world.WorldEffectManager;
import com.teamneon.theelemental.store.SpellInfoServerHelper;
import com.teamneon.theelemental.world.spawn.PillarGenerator;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.event.callback.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static net.minecraft.world.level.Level.OVERWORLD;

public class ElementalEvents {

    public static void register() {

        //spawn in geode loot
        LivingEntityCallback.Death.Before.EVENT.register((entity, damageSource) -> {
            // 1. Only run on server
            if (!entity.level().isClientSide()) {

                // 2. 0.5% chance (1 in 200)
                float chance = 0.005f;

                if (entity.level().random.nextFloat() < chance) {
                    // 3. Create the item stack
                    ItemStack stack = new ItemStack(ModItems.ELEMENTAL_UPGRADES.asItem());

                    // 4. Create and spawn the ItemEntity
                    ItemEntity itemEntity = new ItemEntity(
                            entity.level(),
                            entity.getX(),
                            entity.getY(),
                            entity.getZ(),
                            stack
                    );

                    entity.level().addFreshEntity(itemEntity);
                }
            }

            // 5. Return true to ALLOW the death to continue
            return true;
        });

        // --- 1. Join Event (Load Data) ---
        // We register a lambda to the official ServerPlayerCallback.Join.EVENT
        ServerPlayerCallback.Join.EVENT.register(player -> {

            // --- 1️⃣ Load player data ---
            ElementalDataHandler.load(player);

            // --- 2️⃣ Sync player data (HUD etc.) ---
            ElementalDataHandler.syncToClient(player);

            // --- 3️⃣ Generate pillars (existing code) ---
            PillarGenerator.generate(player);

            // --- 4️⃣ Send SpellInfo metadata to client ---
            ResourceManager manager = player.level().getServer().getResourceManager();

            // Loop through all spells in your registry
            for (int spellId : SpellRegistry.getAllSpellIds()) {
                // Skip ID 0 (empty spell)
                if (spellId <= 0) continue;

                // Load the spell JSON and extract metadata
                ClientSpellInfo info = SpellInfoServerHelper.loadSpellInfo(spellId, manager);

// Send each field individually
                Balm.networking().sendTo(player, new SyncSpellInfoPacket(
                        info.spellId,
                        info.name,
                        info.description,
                        info.manaCost,
                        info.cooldownTicks,
                        info.durationTicks,
                        info.requiredLevel
                ));

            }
        });


        // --- 2. Leave Event (Save Data) ---
        // We register a lambda to the official ServerPlayerCallback.Leave.EVENT
        ServerPlayerCallback.Leave.EVENT.register(player -> {
            // Note: ServerPlayerCallback events are already server-side,
            // but the check remains a safe practice.
            if (!player.level().isClientSide()) {
                // Your Save Logic: RAM -> NBT
                ElementalDataHandler.save(player);
            }
        });

        // --- 3. Optional: Respawn (For persistence during respawn) ---
        ServerPlayerCallback.Respawn.EVENT.register((oldPlayer, newPlayer) -> {
            // Respawn often resets player objects. We must re-sync data to the new player object.
            ElementalDataHandler.syncToClient(newPlayer);
        });

        ServerTickCallback.ServerPlayerTick.AFTER.register(player -> {
            if (player.tickCount % 100 == 0) {
                ElementalDataHandler.broadcastElement(player);
            }

            if (player.tickCount % 10 != 0) return;

            ElementalData data = ElementalDataHandler.get(player);
            if (data == null) return;

            // OPTIONAL: still regenerate mana
            data.tickMana();
            // Sync every tick (testing only)
            ElementalDataHandler.syncToClient(player);


        });



// --- 4. Dimension-specific Ticking ---
        // Runs once for every loaded dimension (Overworld, Nether, End)
        ServerTickCallback.ServerLevelTick.AFTER.register(level -> {
            // Sigils and Ground effects are tied to a specific world
            WorldEffectManager.tickAll(level);
        });

        // --- 5. Global Server Ticking ---
        // Runs exactly ONCE per game tick, regardless of dimensions
        ServerTickCallback.AFTER.register(server -> {
            // Player spells follow the server heartbeat
            ActiveSpellManager.tickAllGlobal(server);

            // If TempBlock changes blocks in the world, it might need
            // to be moved to ServerLevelTick instead, depending on how it's written.
            TempBlock.tickAll();
        });


    }
}