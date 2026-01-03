package com.teamneon.theelemental.api;

import java.lang.reflect.InvocationTargetException;

public class TheelementalAPI {

    public static final String MOD_ID = "theelemental";

    private static final InternalMethods __internalMethods;

    static {
        try {
            __internalMethods = (InternalMethods) Class.forName("com.teamneon.theelemental.InternalMethodsImpl").getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
