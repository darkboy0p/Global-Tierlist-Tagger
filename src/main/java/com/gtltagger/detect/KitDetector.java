package com.gtltagger.detect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import com.gtltagger.gamemode.Gamemodes;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Best-effort automatic kit/gamemode detection (spec section 6).
 *
 * There is no universal, protocol-level way to know "what kit am I
 * in" on an arbitrary server — every KitPvP/practice server surfaces
 * this differently. This implementation looks for a known gamemode
 * name inside the sidebar scoreboard title and the TAB header/footer,
 * which covers the common case of servers that print the current kit
 * there (e.g. a sidebar line like "Kit: NethPot").
 *
 * If your server exposes the kit some other way (boss bar, action
 * bar, a specific scoreboard line, etc.) adjust {@link #detect()}
 * accordingly — the rest of the mod only depends on this returning
 * a name from {@link Gamemodes#ORDER}, or null.
 */
public final class KitDetector {

    /**
     * One whole-word, case-insensitive pattern per known gamemode.
     * Previously this used a raw {@code String.contains(...)} check,
     * which false-positived on any text merely containing a kit name
     * as a substring of an unrelated word - e.g. a line like
     * "Teleporting..." or a server/rank name containing "pot" or
     * "smp" would wrongly be detected as the Pot or SMP kit. Matching
     * on a whole word instead fixes that.
     */
    private static final Map<String, Pattern> WORD_PATTERNS = new HashMap<>();

    static {
        for (String gamemode : Gamemodes.ORDER) {
            WORD_PATTERNS.put(gamemode, Pattern.compile("\\b" + Pattern.quote(gamemode) + "\\b", Pattern.CASE_INSENSITIVE));
        }
    }

    private KitDetector() {
    }

    /** Returns a detected gamemode name from {@link Gamemodes#ORDER}, or null if unknown/unavailable. */
    public static String detect() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return null;
        }

        String fromSidebar = detectFromSidebar(client);
        if (fromSidebar != null) {
            return fromSidebar;
        }

        return detectFromTabHeaderFooter(client);
    }

    private static String detectFromSidebar(MinecraftClient client) {
        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) {
            return null;
        }
        return findKnownGamemode(objective.getDisplayName());
    }

    private static String detectFromTabHeaderFooter(MinecraftClient client) {
        if (client.getNetworkHandler() == null) {
            return null;
        }
        // ClientPlayNetworkHandler no longer exposes getters for these —
        // TabTextHolder is kept up to date by ClientPlayNetworkHandlerMixin.
        Text header = TabTextHolder.getHeader();
        Text footer = TabTextHolder.getFooter();
        String fromHeader = header != null ? findKnownGamemode(header) : null;
        if (fromHeader != null) {
            return fromHeader;
        }
        return footer != null ? findKnownGamemode(footer) : null;
    }

    private static String findKnownGamemode(Text text) {
        if (text == null) return null;
        String plain = text.getString();
        if (plain.isBlank()) return null;
        for (String gamemode : Gamemodes.ORDER) {
            if (WORD_PATTERNS.get(gamemode).matcher(plain).find()) {
                return gamemode;
            }
        }
        return null;
    }
}
