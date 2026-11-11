package com.gtltagger.client.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import com.gtltagger.client.IconGlyphs;

/**
 * Draws a kit icon as a single font glyph (see {@link IconGlyphs}), so
 * no texture-blit API is needed. Kits without a bundled icon (gamemodes
 * added to the tier list later) draw nothing.
 */
public final class HudIconRenderer {

    /** Default font baseline offset: glyph top = textY + 7 - fontAscent. */
    private static final int BASELINE = 7;

    private HudIconRenderer() {
    }

    public static boolean has(String kit) {
        return IconGlyphs.get(kit) != null;
    }

    /** Draws {@code kit}'s icon with its top-left at (x, y). {@code large}: 20px HUD size (ascent 17) vs 12px (ascent 10). */
    public static void drawIcon(GuiGraphicsExtractor graphics, Font font, String kit, int x, int y, boolean large) {
        Character glyph = IconGlyphs.get(kit);
        if (glyph == null) {
            return;
        }
        Identifier fontId = large ? IconGlyphs.FONT_LARGE : IconGlyphs.FONT;
        int ascent = large ? 17 : 10;
        Component text = Component.literal(String.valueOf(glyph.charValue()))
                .setStyle(Style.EMPTY.withFont(new FontDescription.Resource(fontId))
                        .withColor(TextColor.fromRgb(0xFFFFFF)));
        graphics.text(font, text, x, y + ascent - BASELINE, 0xFFFFFFFF, false);
    }
}
