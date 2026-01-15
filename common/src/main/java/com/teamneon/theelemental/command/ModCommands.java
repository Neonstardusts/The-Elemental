package com.teamneon.theelemental.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.teamneon.theelemental.data.ElementalData;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.magic.base.SpellRegistry;
import com.teamneon.theelemental.magic.network.SyncSpellInfoPacket;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.commands.BalmCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;

public class ModCommands {

    public static void initialize() {
        Balm.commands().register(dispatcher -> {
            dispatcher.register(
                    Commands.literal("setspell")
                            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(4))))
                            .then(Commands.argument("slot", IntegerArgumentType.integer(1, 8))
                                    .then(Commands.argument("spell", IntegerArgumentType.integer(1001))
                                            .executes(ctx -> {
                                                ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                int slot = IntegerArgumentType.getInteger(ctx, "slot")-1;
                                                int spellId = IntegerArgumentType.getInteger(ctx, "spell");

                                                ElementalData data = ElementalDataHandler.get(player);
                                                data.setSlot(slot, spellId);

                                                if (spellId > 0) {
                                                    ResourceManager manager = player.level().getServer().getResourceManager();
                                                    var spell = SpellRegistry.getSpell(spellId, manager);
                                                    if (spell != null) {
                                                        Balm.networking().sendTo(player, new SyncSpellInfoPacket(
                                                                spellId,
                                                                spell.getName(),
                                                                SpellRegistry.getSpellDescription(spellId, manager),
                                                                spell.getManaCost(),
                                                                spell.getCooldownTicks(),
                                                                SpellRegistry.getSpellDuration(spellId, manager),
                                                                SpellRegistry.getRequiredLevel(spellId, manager)
                                                        ));
                                                    }
                                                }

                                                ElementalDataHandler.syncToClient(player);
                                                ElementalDataHandler.save(player);

                                                // ✅ Wrap in lambda to fix "Required type: Supplier<Component>" error
                                                ctx.getSource().sendSuccess(() -> Component.literal(
                                                        "Set slot " + slot + " to spell ID " + spellId
                                                ), false);

                                                return 1;
                                            })))
            );
        });
    }
}
