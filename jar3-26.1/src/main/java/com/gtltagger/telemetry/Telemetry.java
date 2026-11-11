package com.gtltagger.telemetry;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import com.gtltagger.GTLTaggerMod;
import com.gtltagger.config.GTLTaggerConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

/**
 * Optional, anonymous usage statistics - OFF by default.
 *
 * Nothing is ever sent unless the player turns on "Anonymous Usage Stats"
 * in the GTLTagger settings screen AND {@link #ENDPOINT} is set.
 *
 * What is sent (once per day at most) - exactly the fields built in
 * {@link #buildPayload}: a random install id, the mod / Minecraft /
 * Fabric Loader / Java versions, the OS family (windows / macos / linux),
 * and the mod's own settings (which gamemodes are selected, whether the
 * HUD and auto-detection are on, where tiers are shown).
 *
 * What is NEVER sent: Minecraft username or UUID, server addresses,
 * chat, world data, file paths, or anything about other players. The
 * receiving server inevitably sees the sender's IP address - it should
 * not log or store it (see telemetry-server-example/).
 *
 * Turning the toggle off deletes the install id. Every request is
 * logged at INFO with its full body so players can see what was sent.
 * All network work happens on a background thread, and any failure is
 * ignored - telemetry must never affect the game.
 */
public final class Telemetry {

    /**
     * Where reports are POSTed as JSON. Leave empty to disable the feature
     * entirely (the settings toggle then does nothing). Set this to your own
     * collector, e.g. "https://your-site.vercel.app/api/telemetry".
     */
    public static final String ENDPOINT = "";

    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static volatile boolean inFlight = false;

    private Telemetry() {
    }

    /** True when this build has somewhere to send reports. */
    public static boolean isAvailable() {
        return !ENDPOINT.isBlank();
    }

    /** Call once at startup: sends today's report if the player opted in and none was sent today. */
    public static void start(GTLTaggerConfig config) {
        if (!config.telemetryEnabled || !isAvailable() || inFlight) {
            return;
        }
        long today = LocalDate.now(ZoneOffset.UTC).toEpochDay();
        if (config.telemetryLastSentDay == today) {
            return;
        }
        if (config.telemetryInstallId == null || config.telemetryInstallId.isBlank()) {
            config.telemetryInstallId = UUID.randomUUID().toString();
            config.save();
        }

        String body = GSON.toJson(buildPayload(config));
        inFlight = true;
        Thread worker = new Thread(() -> send(config, body, today), "GTLTagger-Telemetry");
        worker.setDaemon(true);
        worker.start();
    }

    /** Applies the settings-screen toggle. Disabling also deletes the install id so nothing identifying remains. */
    public static void setEnabled(GTLTaggerConfig config, boolean enabled) {
        config.telemetryEnabled = enabled;
        if (!enabled) {
            config.telemetryInstallId = null;
            config.telemetryLastSentDay = 0L;
        }
        config.save();
        if (enabled) {
            start(config);
        }
    }

    /** The exact JSON that would be sent right now (used for the log line and available for a preview UI). */
    public static JsonObject buildPayload(GTLTaggerConfig config) {
        JsonObject json = new JsonObject();
        json.addProperty("schema", SCHEMA_VERSION);
        json.addProperty("installId", config.telemetryInstallId);
        json.addProperty("modVersion", ownVersion());
        json.addProperty("minecraftVersion", versionOf("minecraft"));
        json.addProperty("loaderVersion", versionOf("fabricloader"));
        json.addProperty("java", Runtime.version().feature());
        json.addProperty("os", osFamily());

        JsonObject settings = new JsonObject();
        settings.addProperty("leftGamemode", config.gamemode1);
        settings.addProperty("rightGamemode", config.gamemode2);
        settings.addProperty("leftSurface", String.valueOf(config.leftSurface));
        settings.addProperty("rightSurface", String.valueOf(config.rightSurface));
        settings.addProperty("tierNameFormat", String.valueOf(config.tierNameFormat));
        settings.addProperty("hudEnabled", config.hudEnabled);
        settings.addProperty("automaticDetection", config.automaticDetectionEnabled);
        json.add("settings", settings);
        return json;
    }

    private static void send(GTLTaggerConfig config, String body, long today) {
        try {
            GTLTaggerMod.LOGGER.info("Sending anonymous usage stats (opt-in): {}", body);
            HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "GTLTagger/" + ownVersion())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<Void> response = HTTP.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() / 100 == 2) {
                config.telemetryLastSentDay = today;
                config.save();
            }
        } catch (Exception ignored) {
            // Never let telemetry affect the game; it will simply retry next launch.
        } finally {
            inFlight = false;
        }
    }

    /**
     * This mod's own version. When GTLTagger ships as the all-versions bundle, the build that is
     * actually running is a nested mod whose id is "gtltagger-<something>", so look for either.
     */
    private static String ownVersion() {
        return FabricLoader.getInstance().getAllMods().stream()
                .filter(m -> m.getMetadata().getId().equals(GTLTaggerMod.MOD_ID)
                        || m.getMetadata().getId().startsWith(GTLTaggerMod.MOD_ID + "-"))
                .map(m -> m.getMetadata().getVersion().getFriendlyString())
                .findFirst().orElse("unknown");
    }

    private static String versionOf(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    private static String osFamily() {
        String name = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (name.contains("win")) return "windows";
        if (name.contains("mac") || name.contains("darwin")) return "macos";
        if (name.contains("linux") || name.contains("nix")) return "linux";
        return "other";
    }
}
