package com.gtltagger.data;

import com.gtltagger.GTLTaggerMod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * In-memory tier database, populated entirely from the GlobalTierlist
 * API (https://globaltierlist-api.vercel.app/api/players) - this mod
 * no longer reads a local config/gtltagger/players.json file.
 *
 * A background refresh is kicked off immediately on {@link #load()}
 * and repeats every {@link #REFRESH_INTERVAL_MINUTES} (2) minutes. If a
 * refresh fails (offline, API down, bad response), the previous
 * successful snapshot is kept rather than cleared, so a transient
 * outage doesn't blank out tier lookups mid-session - check the log
 * for warnings if data looks stale.
 */
public class TierDatabase {

    private static final long REFRESH_INTERVAL_MINUTES = 2;

    private static final ScheduledExecutorService EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(daemonThreadFactory());

    private static volatile Map<String, Map<String, TierEntry>> data = new HashMap<>();
    private static final AtomicBoolean scheduled = new AtomicBoolean(false);

    private TierDatabase() {
    }

    /**
     * Triggers an immediate refresh from the API and, the first time
     * this is called, schedules the periodic background refresh.
     * Safe to call repeatedly (e.g. from "/gtltagger reload") - it
     * never blocks the calling thread.
     */
    public static void load() {
        if (scheduled.compareAndSet(false, true)) {
            EXECUTOR.scheduleWithFixedDelay(
                    TierDatabase::refreshOnce, 0, REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES);
        } else {
            EXECUTOR.execute(TierDatabase::refreshOnce);
        }
    }

    /**
     * Like {@link #load()}, but runs {@code onDone} (with whether the
     * refresh succeeded) once it completes. {@code onDone} is called
     * from the background thread - hop back to the client thread
     * yourself if you touch client state from it.
     */
    public static void reload(Consumer<Boolean> onDone) {
        EXECUTOR.execute(() -> onDone.accept(refreshOnce()));
    }

    private static boolean refreshOnce() {
        try {
            Map<String, Map<String, TierEntry>> fetched = GlobalTierlistApi.fetchAll();
            data = fetched;
            GTLTaggerMod.LOGGER.info("GTLTagger: refreshed tier data for {} player(s) from GlobalTierlist API",
                    fetched.size());
            return true;
        } catch (Exception e) {
            GTLTaggerMod.LOGGER.warn("GTLTagger: failed to refresh tier data from GlobalTierlist API, "
                    + "keeping previous data", e);
            return false;
        }
    }

    private static ThreadFactory daemonThreadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "gtltagger-tier-refresh");
            thread.setDaemon(true);
            return thread;
        };
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

    /**
     * Player-name suggestions for the search box: names starting with
     * {@code query} first (alphabetical), then names merely containing
     * it, up to {@code limit}. Case-insensitive. Reads the in-memory
     * snapshot only, so it is cheap enough to call on every keystroke.
     */
    public static java.util.List<String> suggest(String query, int limit) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (query == null || query.isBlank() || limit <= 0) {
            return out;
        }
        String q = query.trim().toLowerCase(java.util.Locale.ROOT);
        Map<String, Map<String, TierEntry>> snapshot = data;
        java.util.List<String> contains = new java.util.ArrayList<>();
        for (String name : snapshot.keySet()) {
            String lower = name.toLowerCase(java.util.Locale.ROOT);
            if (lower.startsWith(q)) {
                out.add(name);
                if (out.size() >= limit) {
                    return out;
                }
            } else if (contains.size() < limit && lower.contains(q)) {
                contains.add(name);
            }
        }
        for (String name : contains) {
            if (out.size() >= limit) {
                break;
            }
            out.add(name);
        }
        return out;
    }

    /** True once at least one successful refresh has populated the database. */
    public static boolean hasData() {
        return !data.isEmpty();
    }

    /** Number of players currently loaded. */
    public static int playerCount() {
        return data.size();
    }

    /** The canonical (API-cased) spelling of {@code ign}, or null if the player isn't loaded. */
    public static String canonicalName(String ign) {
        if (ign == null) return null;
        Map<String, Map<String, TierEntry>> snapshot = data;
        if (snapshot instanceof java.util.TreeMap<String, Map<String, TierEntry>> tree) {
            java.util.Map.Entry<String, Map<String, TierEntry>> e = tree.ceilingEntry(ign);
            if (e != null && e.getKey().equalsIgnoreCase(ign)) {
                return e.getKey();
            }
        }
        return null;
    }
}
