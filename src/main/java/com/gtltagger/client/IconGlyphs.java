package com.gtltagger.client;

import net.minecraft.util.Identifier;
import com.gtltagger.GTLTaggerMod;
import com.gtltagger.gamemode.Gamemodes;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps each kit name (and "Overall") to a private-use-area codepoint
 * that renders as that kit's real icon texture, via the bitmap font
 * defined at assets/gtltagger/font/icons.json (each provider in that
 * file points at the same PNGs {@link KitIcons} uses for the HUD).
 *
 * Unlike {@link KitIcons} - whose Identifier is drawn manually with
 * DrawContext.drawTexture(...) inside the HUD's own render call -
 * these glyphs are meant to be embedded directly into a {@link
 * net.minecraft.text.Text} via {@code Style.withFont(FONT)}. That's
 * what lets a genuine PNG show up inline in text-only UI like the
 * TAB list and the in-world nametag (see PlayerListHudMixin,
 * PlayerEntityMixin), without needing a per-version DrawContext hook
 * into those renderers.
 */
public final class IconGlyphs {

    /** The custom font (assets/gtltagger/font/icons.json) these glyphs are registered under. */
    public static final Identifier FONT = Identifier.of(GTLTaggerMod.MOD_ID, "icons");

    private static final Map<String, Character> GLYPHS = new HashMap<>();

    static {
        char c = '\uE000';
        for (String kit : Gamemodes.ORDER) {
            GLYPHS.put(kit, c++);
        }
        GLYPHS.put("Overall", c);
    }

    private IconGlyphs() {
    }

    /** The glyph character for {@code kit} (case-sensitive, matching {@link Gamemodes#ORDER} or "Overall"), or null if unknown. */
    public static Character get(String kit) {
        return GLYPHS.get(kit);
    }
}
