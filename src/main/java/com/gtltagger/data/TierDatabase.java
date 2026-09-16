package com.gtltagger.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import com.gtltagger.GTLTaggerMod;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Local tier database, read from <config>/gtltagger/players.json:
 *
 * {
 *   "PlayerName": {
 *     "NethPot": { "tier": "LT3", "peak": "HT3" },
 *     "Crystal": { "tier": "HT2" }
 *   }
 * }
 *
 * This mod does not assume any particular tier-list backend/API —
 * populate this file however fits your server (export script, a
 * companion plugin writing to it, or by hand). IGN lookups are
 * case-insensitive. Reload in-game with "/gtltagger reload".
 */
public class TierDatabase {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("gtltagger").resolve("players.json");

    private static Map<String, Map<String, TierEntry>> data = new HashMap<>();

    private TierDatabase() {
    }

    public static void load() {
        try {
            if (!Files.exists(PATH)) {
                writeExample();
            }
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                Type type = new TypeToken<Map<String, Map<String, TierEntry>>>() {}.getType();
                Map<String, Map<String, TierEntry>> loaded = GSON.fromJson(reader, type);
                Map<String, Map<String, TierEntry>> normalized = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                if (loaded != null) {
                    normalized.putAll(loaded);
                }
                data = normalized;
                GTLTaggerMod.LOGGER.info("GTLTagger: loaded tier data for {} player(s)", data.size());
            }
        } catch (IOException | RuntimeException e) {
            GTLTaggerMod.LOGGER.warn("Failed to load players.json, tier lookups will be empty", e);
            data = new HashMap<>();
        }
    }

    private static void writeExample() throws IOException {
        Files.createDirectories(PATH.getParent());
        Map<String, Map<String, TierEntry>> example = new HashMap<>();
        Map<String, TierEntry> exampleModes = new HashMap<>();
        exampleModes.put("NethPot", new TierEntry("LT3", "HT3"));
        exampleModes.put("Crystal", new TierEntry("HT2", null));
        example.put("ExamplePlayer", exampleModes);
        try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
            GSON.toJson(example, writer);
        }
    }

    /** Returns the entry for {@code ign} + {@code gamemode}, or null if untested. */
    public static TierEntry get(String ign, String gamemode) {
        if (ign == null || gamemode == null) return null;
        Map<String, TierEntry> modes = data.get(ign);
        if (modes == null) return null;
        return modes.get(gamemode);
    }

    /** All gamemodes this player has been tested in (empty if none/unknown player). */
    public static Map<String, TierEntry> getAll(String ign) {
        if (ign == null) return Map.of();
        return data.getOrDefault(ign, Map.of());
    }
}
