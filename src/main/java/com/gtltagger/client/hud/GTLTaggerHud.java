package com.gtltagger.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import com.gtltagger.client.KitIcons;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.data.TierDatabase;
import com.gtltagger.data.TierEntry;
import com.gtltagger.detect.KitDetector;
import com.gtltagger.tag.TierColors;
import com.gtltagger.tag.TierText;

import java.util.ArrayList;
import java.util.List;

/**
 * Small transparent overlay showing the LOCAL player's own kit/tier
 * (spec section 7). Position is stored in config and edited via
 * {@link com.gtltagger.client.gui.HudPositionScreen}.
 *
 * Gamemode lines are drawn with the real kit icon texture (from
 * assets/gtltagger/textures/icon/) rather than as plain text. The
 * actual drawTexture(...) call lives in {@link HudIconRenderer}
 * instead of here, because that method's argument shape changed twice
 * within the 1.21.1-1.21.7 range this repo's CI matrix builds against
 * (see HudIconRenderer's own javadoc, and KitIcons.java). build.gradle
 * adds exactly one version-specific HudIconRenderer.java to the
 * compile classpath per matrix leg, so this class only ever sees one
 * implementation of drawIcon(...) at a time.
 *
 * Tier line always uses the short tier code ("LT5") - the Short/Full
 * text-format setting applies only to the in-world nametag, not here
 * or in TAB - and is colored per-tier via {@link TierColors}. Never
 * shows a peak tier (see {@link com.gtltagger.client.gui.PlayerSearchScreen}
 * for that - press the player-search keybind to look a peak up).
 */
public final class GTLTaggerHud {

    private static final int BG_COLOR = 0x50000000; // ~31% black - deliberately translucent, not a solid panel
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 20;
    private static final int ICON_SIZE = 20; // bumped up from 14 - was rendering as a barely-visible speck
    private static final int ICON_TEXT_GAP = 4;
    private static final int ICON_SOURCE_PX = 512; // all kit icons ship as 512x512
    private static final int DEFAULT_TEXT_COLOR = 0xFFFFFF;

    private GTLTaggerHud() {
    }

    /** One HUD row: text, the kit icon to draw before it (if any), and a text color override (if any - defaults to white). */
    public record HudLine(String text, Identifier icon, Integer color) {
        public HudLine(String text) {
            this(text, null, null);
        }

        public HudLine(String text, Identifier icon) {
            this(text, icon, null);
        }
    }

    public static void render(DrawContext drawContext, GTLTaggerConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        List<HudLine> lines = buildLines(client, config);
        if (lines.isEmpty()) {
            return;
        }

        renderBox(drawContext, client, lines, config.hudX, config.hudY);
    }

    /** Also used by {@link com.gtltagger.client.gui.HudPositionScreen} for a live preview. */
    public static List<HudLine> buildLines(MinecraftClient client, GTLTaggerConfig config) {
        List<HudLine> lines = new ArrayList<>();
        String ign = client.player.getGameProfile().getName();

        String kit = config.automaticDetectionEnabled ? KitDetector.detect() : null;
        if (kit == null) {
            kit = config.gamemode1;
        }
        TierEntry entry = TierDatabase.get(ign, kit);

        lines.add(new HudLine("Kit: " + kit, KitIcons.get(kit)));
        lines.add(tierLine("Tier: ", entry != null ? entry.tier : null));
        return lines;
    }

    /** A "Tier: ..." row: "?" and no color for an untested tier, else the short-form, colored tier. Always the short code, like TAB - peak is deliberately not shown here (see PlayerSearchScreen). */
    private static HudLine tierLine(String label, String tier) {
        if (tier == null) {
            return new HudLine(label + "?");
        }
        return new HudLine(label + TierText.shortForm(tier), null,
                TierColors.rgbForTierNumber(TierText.tierNumber(tier)));
    }

    public static void renderBox(DrawContext drawContext, MinecraftClient client, List<HudLine> lines, int x, int y) {
        int maxWidth = 0;
        for (HudLine line : lines) {
            int textWidth = client.textRenderer.getWidth(line.text());
            int lineWidth = line.icon() != null ? ICON_SIZE + ICON_TEXT_GAP + textWidth : textWidth;
            maxWidth = Math.max(maxWidth, lineWidth);
        }

        int boxWidth = maxWidth + PADDING * 2;
        int boxHeight = lines.size() * LINE_HEIGHT + PADDING * 2;
        drawContext.fill(x, y, x + boxWidth, y + boxHeight, BG_COLOR);

        for (int i = 0; i < lines.size(); i++) {
            HudLine line = lines.get(i);
            int rowY = y + PADDING + i * LINE_HEIGHT;
            int textX = x + PADDING;

            if (line.icon() != null) {
                int iconY = rowY + (LINE_HEIGHT - ICON_SIZE) / 2;
                HudIconRenderer.drawIcon(drawContext, line.icon(), textX, iconY, ICON_SIZE, ICON_SOURCE_PX);
                textX += ICON_SIZE + ICON_TEXT_GAP;
            }

            drawContext.drawText(
                    client.textRenderer,
                    line.text(),
                    textX,
                    rowY + 1,
                    line.color() != null ? line.color() : DEFAULT_TEXT_COLOR,
                    true
            );
        }
    }
}
