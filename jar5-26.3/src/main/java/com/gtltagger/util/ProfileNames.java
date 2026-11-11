package com.gtltagger.util;

import java.lang.reflect.Method;

/**
 * Reads a player's name out of an authlib GameProfile regardless of
 * Minecraft version. authlib turned GameProfile into a record and
 * renamed getName() to name() partway through 1.21.x, so calling either
 * directly would crash on the other side of that change. authlib is not
 * remapped by Fabric, so plain reflection on these names is safe.
 */
public final class ProfileNames {

    private static volatile Method nameMethod;

    private ProfileNames() {
    }

    /** The profile's name, or null if it can't be read. */
    public static String of(Object gameProfile) {
        if (gameProfile == null) {
            return null;
        }
        try {
            Method m = nameMethod;
            if (m == null || !m.getDeclaringClass().isInstance(gameProfile)) {
                m = find(gameProfile.getClass());
                nameMethod = m;
            }
            Object value = m.invoke(gameProfile);
            return value instanceof String s ? s : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static Method find(Class<?> type) throws NoSuchMethodException {
        try {
            return type.getMethod("name");
        } catch (NoSuchMethodException e) {
            return type.getMethod("getName");
        }
    }
}
