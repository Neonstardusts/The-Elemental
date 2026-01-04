package com.teamneon.theelemental.magic.base;

import java.util.Map;

@FunctionalInterface
public interface SpellFactory {
    Spell create(Map<String, Object> json);
}
