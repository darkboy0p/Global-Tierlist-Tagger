package com.gtltagger.gamemode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

/**
 * Gamemode registry.
 *
 * {@link #CORE} is the fixed set of kits that ship with a bundled icon
 * (and a glyph in assets/gtltagger/font/icons.json). Every OTHER
 * gamemode the Global Tierlist API returns is discovered at runtime
 * (see {@link #registerDiscovered}) and becomes usable everywhere -
 * TAB, nametags, HUD, search, settings cyclers, kit detection - just
 * without a custom icon (the tier text is shown on its own).
 *
 * Use {@link #all()} for the full, current list; never cache it.
 */
public final class Gamemodes {

    /** Kits with a bundled icon, in cycle order. Do not mutate. */
    public static final List<String> CORE = List.of(
            "NethPot", "Pot", "Crystal", "Sword", "UHC", "Axe", "SMP", "Mace");

    /** This mod's core name -> the API's name (API's "Diapot" is this mod's "Pot"). */
    private static final Map<String, String> CORE_TO_API = Map.of(
            "NethPot", "Nethpot",
            "Pot", "Diapot",
            "Crystal", "Crystal",
            "Sword", "Sword",
            "UHC", "Uhc",
            "Axe", "Axe",
            "SMP", "Smp",
            "Mace", "Mace");

    /** Extra gamemodes seen from the API, display name -> API name. Sorted, case-insensitive. */
    private static final Map<String, String> EXTRA_TO_API =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static volatile List<String> allCache = CORE;

    private Gamemodes() {
    }

    /** Every known gamemode: {@link #CORE} first, then discovered ones alphabetically. */
    public static List<String> all() {
        return allCache;
    }

    /** Maps an API gamemode name to this mod's display name (core name if known, else the API name itself). */
    public static String fromApiName(String apiName) {
        if (apiName == null) {
            return null;
        }
        String trimmed = apiName.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> e : CORE_TO_API.entrySet()) {
            if (e.getValue().equalsIgnoreCase(trimmed) || e.getKey().equalsIgnoreCase(trimmed)) {
                return e.getKey();
            }
        }
        return trimmed;
    }

    /** Maps a display name back to the API's name. */
    public static String toApiName(String display) {
        String core = CORE_TO_API.get(display);
        if (core != null) {
            return core;
        }
        return EXTRA_TO_API.getOrDefault(display, display);
    }

    /**
     * Records every gamemode seen in an API response so non-core ones
     * show up in {@link #all()}. Safe to call from the refresh thread.
     */
    public static void registerDiscovered(Collection<String> displayNames) {
        boolean changed = false;
        for (String name : displayNames) {
            if (name != null && !CORE.contains(name) && EXTRA_TO_API.putIfAbsent(name, name) == null) {
                changed = true;
            }
        }
        if (changed) {
            TreeSet<String> sorted = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            sorted.addAll(EXTRA_TO_API.keySet());
            List<String> list = new ArrayList<>(CORE);
            list.addAll(sorted);
            allCache = List.copyOf(list);
        }
    }

    /** Returns the next gamemode after {@code current} in {@link #all()} order. */
    public static String next(String current) {
        List<String> list = all();
        int idx = list.indexOf(current);
        if (idx < 0) {
            return list.get(0);
        }
        return list.get((idx + 1) % list.size());
    }

    public static boolean isKnown(String gamemode) {
        return gamemode != null && all().contains(gamemode);
    }

    /**
     * Chat-text icon token for this kit, e.g. "NethPot" -> "nethpoticon"
     * (used in "[:nethpoticon:LT5]" tags). Unknown kits fall back to the
     * generic "icon".
     */
    public static String iconToken(String kit) {
        if (kit == null || !isKnown(kit)) {
            return "icon";
        }
        return kit.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "") + "icon";
    }
}
