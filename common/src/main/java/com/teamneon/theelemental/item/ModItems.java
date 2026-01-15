package com.teamneon.theelemental.item;

import com.teamneon.theelemental.client.ClientSpellInfo;
import com.teamneon.theelemental.client.ClientSpellRegistry;
import com.teamneon.theelemental.helpers.SpellJsonLoader;
import com.teamneon.theelemental.magic.base.Spell;
import com.teamneon.theelemental.magic.base.SpellRegistry;
import com.teamneon.theelemental.store.ModComponents;
import com.teamneon.theelemental.store.RuneData;
import net.blay09.mods.balm.tags.BalmItemTags;
import net.blay09.mods.balm.world.item.BalmCreativeModeTabRegistrar;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import com.teamneon.theelemental.Theelemental;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static com.teamneon.theelemental.Theelemental.id;

public class ModItems {
    public static DeferredItem BLANK_RUNE;
    public static DeferredItem ELEMENT_RUNE;
    public static DeferredItem SPELL_RUNE;
    public static DeferredItem KINGDOM_CORE_ITEM;
    public static DeferredItem LEVEL_CRYSTAL;
    public static DeferredItem RADIUS_CRYSTAL;
    public static DeferredItem SLOT_CRYSTAL;

    public static void initialize(BalmItemRegistrar items) {
        BLANK_RUNE = items.register("rune_blank", BlankRune::new).asDeferredItem();
        ELEMENT_RUNE = items.register("rune_element", ElementRune::new).asDeferredItem();
        SPELL_RUNE= items.register("rune_spell", SpellRune::new).asDeferredItem();

        KINGDOM_CORE_ITEM = items.register("kingdom_starter", KingdomCoreItem::new).asDeferredItem();

        LEVEL_CRYSTAL = items.register("level_crystal", KingdomCrystal::new).asDeferredItem();
        RADIUS_CRYSTAL = items.register("radius_crystal", KingdomCrystal::new).asDeferredItem();
        SLOT_CRYSTAL = items.register("slot_crystal", KingdomCrystal::new).asDeferredItem();

    }

    public static void initialize(BalmCreativeModeTabRegistrar creativeModeTabs) {
        creativeModeTabs.register(Theelemental.MOD_ID, builder ->
                builder.title(Component.translatable(id(Theelemental.MOD_ID).toLanguageKey("itemGroup")))
                        .icon(() -> ModItems.BLANK_RUNE.createStack())
                        .displayItems((displayParameters, output) -> {

                            // 1. Add standard items
                            output.accept(ModItems.BLANK_RUNE);
                            output.accept(ModItems.LEVEL_CRYSTAL);
                            output.accept(ModItems.RADIUS_CRYSTAL);
                            output.accept(ModItems.SLOT_CRYSTAL);
                            output.accept(ModItems.KINGDOM_CORE_ITEM);

                            // 2. Add Spells from the Client Registry (Sorted Numerically)
                            SpellRegistry.getAllSpellIds().stream()
                                    .sorted()
                                    .forEach(spellId -> {
                                        if (spellId == 0) return;

                                        ClientSpellInfo info = ClientSpellRegistry.getSpell(spellId);

                                        if (info != null) {
                                            RuneData runeData = new RuneData(
                                                    info.spellId / 1000,
                                                    info.spellId,
                                                    info.name,
                                                    new HashMap<>(),
                                                    info.manaCost,
                                                    info.cooldownTicks,
                                                    info.description,
                                                    info.durationTicks
                                            );

                                            ItemStack stack = new ItemStack(ModItems.SPELL_RUNE.asItem());
                                            stack.set(ModComponents.rune.value(), runeData);
                                            output.accept(stack);
                                        }
                                    });
                        })
        );
    }
}
