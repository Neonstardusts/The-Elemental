package com.teamneon.theelemental.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static com.teamneon.theelemental.Theelemental.id;

public class ModBlockTags {
    public static final TagKey<Block> YOUR_TAG = TagKey.create(Registries.BLOCK, id("your_tag"));
}
