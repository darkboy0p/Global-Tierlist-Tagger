package com.gtltagger.detect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import com.gtltagger.gamemode.Gamemodes;

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

    private static final int SIDEBAR_SLOT = 1; // vanilla protocol slot id for the sidebar, MC 1.20.1

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
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(SIDEBAR_SLOT);
        if (objective == null) {
            return null;
        }
        return findKnownGamemode(objective.getDisplayName());
    }

    private static String detectFromTabHeaderFooter(MinecraftClient client) {
        if (client.getNetworkHandler() == null) {
            return null;
        }
        Text header = client.getNetworkHandler().getPlayerListHeader();
        Text footer = client.getNetworkHandler().getPlayerListFooter();
        String fromHeader = header != null ? findKnownGamemode(header) : null;
        if (fromHeader != null) {
            return fromHeader;
        }
        return footer != null ? findKnownGamemode(footer) : null;
    }

    private static String findKnownGamemode(Text text) {
        if (text == null) return null;
        String plain = text.getString();
        for (String gamemode : Gamemodes.ORDER) {
            if (plain.toLowerCase().contains(gamemode.toLowerCase())) {
                return gamemode;
            }
        }
        return null;
    }
}
