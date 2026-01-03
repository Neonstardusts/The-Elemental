package com.teamneon.theelemental.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static com.teamneon.theelemental.Theelemental.id;

public class ModItemTags {
    public static final TagKey<Item> YOUR_TAG = TagKey.create(Registries.ITEM, id("your_tag"));
}
