package com.gtltagger.gamemode;

import java.util.List;

/**
 * The fixed gamemode cycle order from spec section 3.
 * Matches the kit set mctiers.io / pvptiers.com currently use
 * (icons for each of these ship in assets/gtltagger/textures/icon/).
 * Edit this list if your server's kit list differs.
 */
public final class Gamemodes {
    public static final List<String> ORDER = List.of(
            "NethPot",
            "Pot",
            "Crystal",
            "Sword",
            "UHC",
            "Axe",
            "SMP",
            "Mace"
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
