package com.gtltagger.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.data.TierDatabase;
import com.gtltagger.data.TierEntry;
import com.gtltagger.detect.KitDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Small transparent overlay showing the LOCAL player's own kit/tier
 * (spec section 7). Position is stored in config and edited via
 * {@link com.gtltagger.client.gui.HudPositionScreen}.
 */
public final class GTLTaggerHud {

    private static final int BG_COLOR = 0x90000000; // ~56% black
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 10;

    private GTLTaggerHud() {
    }

    public static void render(DrawContext drawContext, GTLTaggerConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        List<String> lines = buildLines(client, config);
        if (lines.isEmpty()) {
            return;
        }

        renderBox(drawContext, client, lines, config.hudX, config.hudY);
    }

    /** Also used by {@link com.gtltagger.client.gui.HudPositionScreen} for a live preview. */
    public static List<String> buildLines(MinecraftClient client, GTLTaggerConfig config) {
        List<String> lines = new ArrayList<>();
        String ign = client.player.getGameProfile().getName();

        if (config.secondGamemodeEnabled) {
            appendModeLine(lines, ign, config.gamemode1);
            appendModeLine(lines, ign, config.gamemode2);
            return lines;
        }

        String kit = config.automaticDetectionEnabled ? KitDetector.detect() : null;
        if (kit == null) {
            kit = config.gamemode1;
        }
        TierEntry entry = TierDatabase.get(ign, kit);

        lines.add("GTLTagger");
        lines.add("Kit: " + kit);
        lines.add("Tier: " + (entry != null && entry.tier != null ? entry.tier : "?"));
        if (entry != null && entry.peak != null) {
            lines.add("Peak: " + entry.peak);
        }
        return lines;
    }

    private static void appendModeLine(List<String> lines, String ign, String gamemode) {
        TierEntry entry = TierDatabase.get(ign, gamemode);
        String tier = entry != null && entry.tier != null ? entry.tier : "?";
        lines.add(gamemode + ": " + tier);
    }

    public static void renderBox(DrawContext drawContext, MinecraftClient client, List<String> lines, int x, int y) {
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, client.textRenderer.getWidth(line));
        }

        int boxWidth = maxWidth + PADDING * 2;
        int boxHeight = lines.size() * LINE_HEIGHT + PADDING * 2;
        drawContext.fill(x, y, x + boxWidth, y + boxHeight, BG_COLOR);

        for (int i = 0; i < lines.size(); i++) {
            drawContext.drawText(
                    client.textRenderer,
                    lines.get(i),
                    x + PADDING,
                    y + PADDING + i * LINE_HEIGHT,
                    0xFFFFFF,
                    true
            );
        }
    }
}
