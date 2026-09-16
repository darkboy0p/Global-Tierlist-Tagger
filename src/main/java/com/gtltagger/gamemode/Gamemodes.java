package com.gtltagger.gamemode;

import java.util.List;

/**
 * The fixed gamemode cycle order from spec section 3.
 * Edit this list if your server's kit list differs.
 */
public final class Gamemodes {
    public static final List<String> ORDER = List.of(
            "NethPot",
            "Crystal",
            "Sword",
            "DiamondPot",
            "UHC",
            "Axe",
            "SMP"
    );

    private Gamemodes() {
    }

    /** Returns the next gamemode after {@code current} in the fixed cycle order. */
    public static String next(String current) {
        int idx = ORDER.indexOf(current);
        if (idx < 0) {
            return ORDER.get(0);
        }
        return ORDER.get((idx + 1) % ORDER.size());
    }

    public static boolean isKnown(String gamemode) {
        return ORDER.contains(gamemode);
    }
}
