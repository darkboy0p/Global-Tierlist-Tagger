package com.gtltagger.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.gtltagger.client.compat.Mc;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.data.TierDatabase;
import com.gtltagger.data.TierEntry;
import com.gtltagger.detect.KitDetector;
import com.gtltagger.tag.TierColors;
import com.gtltagger.tag.TierText;

import java.util.ArrayList;
import java.util.List;

/**
 * Small transparent overlay showing the LOCAL player's own kit/tier.
 * Position is stored in config and edited via
 * {@link com.gtltagger.client.gui.HudPositionScreen}. The kit line shows
 * the real kit icon (a font glyph, see {@link HudIconRenderer}); the tier
 * line is the short code ("LT5"), colored per tier. Never shows a peak
 * tier - the player-search screen does that.
 */
public final class GTLTaggerHud {

    private static final int BG_COLOR = 0x50000000;
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 20;
    private static final int ICON_SIZE = 20;
    private static final int ICON_TEXT_GAP = 4;
    private static final int DEFAULT_TEXT_COLOR = 0xFFFFFFFF;

    private GTLTaggerHud() {
    }

    /** One HUD row: text, the kit icon to draw before it (if any), and an RGB text color override (if any - defaults to white). */
    public record HudLine(String text, String iconKit, Integer color) {
        public HudLine(String text) {
            this(text, null, null);
        }

        public HudLine(String text, String iconKit) {
            this(text, iconKit, null);
        }
    }

    public static void render(GuiGraphicsExtractor graphics, GTLTaggerConfig config) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || Mc.isHudHidden(client)) {
            return;
        }

        List<HudLine> lines = buildLines(client, config);
        if (lines.isEmpty()) {
            return;
        }
        renderBox(graphics, client, lines, config.hudX, config.hudY);
    }

    /** Also used by {@link com.gtltagger.client.gui.HudPositionScreen} for a live preview. */
    public static List<HudLine> buildLines(Minecraft client, GTLTaggerConfig config) {
        List<HudLine> lines = new ArrayList<>();
        String ign = client.player.getName().getString();

        String kit = config.automaticDetectionEnabled ? KitDetector.detect() : null;
        if (kit == null) {
            kit = config.gamemode1;
        }
        TierEntry entry = TierDatabase.get(ign, kit);

        lines.add(new HudLine("Kit: " + kit, kit));
        lines.add(tierLine("Tier: ", entry != null ? entry.tier : null));
        return lines;
    }

    private static HudLine tierLine(String label, String tier) {
        if (tier == null) {
            return new HudLine(label + "?");
        }
        return new HudLine(label + TierText.shortForm(tier), null,
                TierColors.rgbForTierNumber(TierText.tierNumber(tier)));
    }

    private static boolean hasIcon(HudLine line) {
        return line.iconKit() != null && HudIconRenderer.has(line.iconKit());
    }

    /** Colors passed to GuiGraphicsExtractor.text are ARGB, so make sure the alpha byte is set. */
    private static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    public static void renderBox(GuiGraphicsExtractor graphics, Minecraft client, List<HudLine> lines, int x, int y) {
        int maxWidth = 0;
        for (HudLine line : lines) {
            int textWidth = client.font.width(line.text());
            int lineWidth = hasIcon(line) ? ICON_SIZE + ICON_TEXT_GAP + textWidth : textWidth;
            maxWidth = Math.max(maxWidth, lineWidth);
        }

        int boxWidth = maxWidth + PADDING * 2;
        int boxHeight = lines.size() * LINE_HEIGHT + PADDING * 2;
        graphics.fill(x, y, x + boxWidth, y + boxHeight, BG_COLOR);

        for (int i = 0; i < lines.size(); i++) {
            HudLine line = lines.get(i);
            int rowY = y + PADDING + i * LINE_HEIGHT;
            int textX = x + PADDING;

            if (hasIcon(line)) {
                int iconY = rowY + (LINE_HEIGHT - ICON_SIZE) / 2;
                HudIconRenderer.drawIcon(graphics, client.font, line.iconKit(), textX, iconY, true);
                textX += ICON_SIZE + ICON_TEXT_GAP;
            }

            graphics.text(client.font, line.text(), textX, rowY + 1,
                    line.color() != null ? opaque(line.color()) : DEFAULT_TEXT_COLOR, true);
        }
    }
}
