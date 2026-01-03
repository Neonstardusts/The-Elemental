package com.teamneon.theelemental.api.client;

import java.lang.reflect.InvocationTargetException;

public class TheelementalClientAPI {

    private static final InternalClientMethods __internalMethods;

    static {
        try {
            __internalMethods = (InternalClientMethods) Class.forName("com.teamneon.theelemental.client.InternalClientMethodsImpl").getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
