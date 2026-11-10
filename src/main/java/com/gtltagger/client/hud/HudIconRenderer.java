package com.gtltagger.client.hud;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import com.gtltagger.client.IconGlyphs;

/**
 * Draws a kit icon as a single font glyph (see {@link IconGlyphs}).
 *
 * This replaces the old DrawContext.drawTexture(...) call, whose
 * signature changed several times across 1.21.x and forced one build
 * per Minecraft version. {@code DrawContext.drawText(TextRenderer,
 * Text, ...)} is stable across 1.21.1-1.21.7, so a single jar works
 * on all of them.
 *
 * Kits without a bundled icon (gamemodes added to the tier list later)
 * simply draw nothing.
 */
public final class HudIconRenderer {

    /** Default font baseline offset: glyph top = textY + 7 - fontAscent. */
    private static final int BASELINE = 7;

    private HudIconRenderer() {
    }

    /** True if {@code kit} has a bundled icon glyph. */
    public static boolean has(String kit) {
        return IconGlyphs.get(kit) != null;
    }

    /**
     * Draws {@code kit}'s icon with its top-left corner at (x, y).
     *
     * @param large true for the 20px HUD size (ascent 17), false for the 12px size (ascent 10)
     */
    public static void drawIcon(DrawContext context, TextRenderer textRenderer, String kit, int x, int y, boolean large) {
        Character glyph = IconGlyphs.get(kit);
        if (glyph == null) {
            return;
        }
        Identifier font = large ? IconGlyphs.FONT_LARGE : IconGlyphs.FONT;
        int ascent = large ? 17 : 10;
        Text text = Text.literal(String.valueOf(glyph.charValue()))
                .setStyle(Style.EMPTY.withFont(font).withColor(TextColor.fromRgb(0xFFFFFF)));
        context.drawText(textRenderer, text, x, y + ascent - BASELINE, 0xFFFFFF, false);
    }
}
