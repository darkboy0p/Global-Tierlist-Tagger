package com.gtltagger.gamemode;

import java.util.List;
import java.util.Map;

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

    /**
     * Maps this mod's gamemode names to the GlobalTierlist API's names
     * (see API_DOCUMENTATION.md: "Gamemodes: Nethpot, Sword, Uhc, Diapot,
     * Smp, Crystal, Axe, Mace"). The API's "Diapot" is this mod's "Pot";
     * everything else is just a casing difference.
     */
    public static final Map<String, String> TO_API_NAME = Map.of(
            "NethPot", "Nethpot",
            "Pot", "Diapot",
            "Crystal", "Crystal",
            "Sword", "Sword",
            "UHC", "Uhc",
            "Axe", "Axe",
            "SMP", "Smp",
            "Mace", "Mace"
    );

    /** Reverse of {@link #TO_API_NAME}, keyed by the API's gamemode name. */
    public static final Map<String, String> FROM_API_NAME = Map.of(
            "Nethpot", "NethPot",
            "Diapot", "Pot",
            "Crystal", "Crystal",
            "Sword", "Sword",
            "Uhc", "UHC",
            "Axe", "Axe",
            "Smp", "SMP",
            "Mace", "Mace"
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
