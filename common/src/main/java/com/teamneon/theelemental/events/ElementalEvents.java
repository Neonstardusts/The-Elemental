package com.teamneon.theelemental.events;

import com.teamneon.theelemental.block.ModBlocks;
import com.teamneon.theelemental.client.ClientSpellInfo;
import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.TempBlock;
import com.teamneon.theelemental.magic.base.ActiveSpellManager;
import com.teamneon.theelemental.magic.base.SpellRegistry;
import com.teamneon.theelemental.magic.network.SyncSpellInfoPacket;
import com.teamneon.theelemental.magic.world.WorldEffectManager;
import com.teamneon.theelemental.store.SpellInfoServerHelper;
import com.teamneon.theelemental.world.spawn.PillarGenerator;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.event.callback.ServerLifecycleCallback;
import net.blay09.mods.balm.platform.event.callback.ServerPlayerCallback; // Import the structure you found
import net.blay09.mods.balm.platform.event.callback.ServerTickCallback;
import net.blay09.mods.balm.platform.event.callback.LevelCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;

import static net.minecraft.world.level.Level.OVERWORLD;

public class ElementalEvents {

    public static void register() {
        // --- 1. Join Event (Load Data) ---
        // We register a lambda to the official ServerPlayerCallback.Join.EVENT
        ServerPlayerCallback.Join.EVENT.register(player -> {
            System.out.println("!!!! [BALM OFFICIAL] Player Joined: " + player.getName().getString());

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
                System.out.println("!!!! [BALM OFFICIAL] Player Left: " + player.getName().getString());

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
            if (player.tickCount % 5 != 0) return;

            ElementalData data = ElementalDataHandler.get(player);
            if (data == null) return;

            // OPTIONAL: still regenerate mana
            data.tickMana();
            // Sync every tick (testing only)
            ElementalDataHandler.syncToClient(player);
        });

        ServerTickCallback.ServerLevelTick.AFTER.register(level -> {
            TempBlock.tickAll();
            ActiveSpellManager.tickAll(level);
            WorldEffectManager.tickAll(level);
        });


        System.out.println("!!!! [BALM OFFICIAL] All ServerPlayerCallbacks are active.");
    }
}