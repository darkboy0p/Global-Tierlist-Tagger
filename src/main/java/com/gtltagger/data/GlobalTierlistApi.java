package com.gtltagger.data;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.gtltagger.GTLTaggerMod;
import com.gtltagger.gamemode.Gamemodes;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Fetches player tier data from the GlobalTierlist API
 * (https://globaltierlist-api.vercel.app), replacing the old
 * config/gtltagger/players.json local-file source entirely.
 *
 * See API_DOCUMENTATION.md: GET /api/players returns every ranked
 * player with a "gamemodes" array of {gamemode, rank, peak_tier}.
 * This mod only reads that one bulk endpoint — it's cheaper than one
 * request per player and gives us every gamemode in a single call.
 */
public final class GlobalTierlistApi {

    private static final String PLAYERS_URL = "https://globaltierlist-api.vercel.app/api/players";
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private GlobalTierlistApi() {
    }

    /** One entry of the API's "gamemodes" array for a player. */
    private static class ApiGamemodeEntry {
        String gamemode;
        String rank;
        @SerializedName("peak_tier")
        String peakTier;
    }

    /** One player object from GET /api/players. */
    private static class ApiPlayer {
        String ign;
        List<ApiGamemodeEntry> gamemodes;
    }

    /**
     * Fetches and parses GET /api/players. Blocks the calling thread on
     * network I/O — callers must run this off the client thread (see
     * {@link TierDatabase}'s scheduled executor).
     *
     * @throws IOException on a network failure, non-200 response, or
     *                      unparsable body.
     */
    static Map<String, Map<String, TierEntry>> fetchAll() throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(PLAYERS_URL))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while fetching " + PLAYERS_URL, e);
        }

        if (response.statusCode() != 200) {
            throw new IOException("GlobalTierlist API returned HTTP " + response.statusCode());
        }

        List<ApiPlayer> players;
        try {
            Type type = new TypeToken<List<ApiPlayer>>() {
            }.getType();
            players = GSON.fromJson(response.body(), type);
        } catch (RuntimeException e) {
            throw new IOException("Failed to parse GlobalTierlist API response", e);
        }

        Map<String, Map<String, TierEntry>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (players == null) {
            return result;
        }

        for (ApiPlayer player : players) {
            if (player.ign == null || player.gamemodes == null) {
                continue;
            }
            Map<String, TierEntry> modes = new HashMap<>();
            for (ApiGamemodeEntry entry : player.gamemodes) {
                if (entry.gamemode == null || entry.rank == null) {
                    continue;
                }
                String modName = Gamemodes.FROM_API_NAME.get(entry.gamemode);
                if (modName == null) {
                    // Unknown/new gamemode on the API side — skip rather
                    // than guess at a mapping (see GTLTaggerMod.LOGGER
                    // warning in TierDatabase for visibility).
                    continue;
                }
                modes.put(modName, new TierEntry(entry.rank, entry.peakTier));
            }
            if (!modes.isEmpty()) {
                result.put(player.ign, modes);
            }
        }
        return result;
    }
}
