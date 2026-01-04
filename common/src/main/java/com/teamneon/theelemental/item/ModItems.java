package com.teamneon.theelemental.item;

import com.teamneon.theelemental.magic.base.Spell;
import net.blay09.mods.balm.world.item.BalmCreativeModeTabRegistrar;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import com.teamneon.theelemental.Theelemental;

import static com.teamneon.theelemental.Theelemental.id;

public class ModItems {
    public static DeferredItem BLANK_RUNE;
    public static DeferredItem ELEMENT_RUNE;
    public static DeferredItem SPELL_RUNE;
    public static DeferredItem MAGIC_TEST_ITEM;
    public static DeferredItem KINGDOM_CORE_ITEM;

    public static void initialize(BalmItemRegistrar items) {
        BLANK_RUNE = items.register("rune_blank", BlankRune::new).asDeferredItem();
        ELEMENT_RUNE = items.register("rune_element", ElementRune::new).asDeferredItem();
        SPELL_RUNE= items.register("rune_spell", SpellRune::new).asDeferredItem();

        MAGIC_TEST_ITEM = items.register("magic_test_item", MagicTestItem::new).asDeferredItem();
        KINGDOM_CORE_ITEM = items.register("kingdom_starter", KingdomCoreItem::new).asDeferredItem();
    }

    public static void initialize(BalmCreativeModeTabRegistrar creativeModeTabs) {
        creativeModeTabs.register(Theelemental.MOD_ID, builder ->
                builder.title(Component.translatable(id(Theelemental.MOD_ID).toLanguageKey("itemGroup")))
                        .icon(() -> ModItems.BLANK_RUNE.createStack())
                        .displayItems((displayParameters, output) -> {
                            output.accept(ModItems.BLANK_RUNE);
                        })
        );
    }

}
