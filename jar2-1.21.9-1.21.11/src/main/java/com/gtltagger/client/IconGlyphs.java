package com.gtltagger.client;

import net.minecraft.util.Identifier;
import com.gtltagger.GTLTaggerMod;
import com.gtltagger.gamemode.Gamemodes;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps each core kit name (and "Overall") to a private-use-area codepoint
 * that renders as that kit's real icon, via the bitmap fonts
 * assets/gtltagger/font/icons.json (12px, TAB/nametag/search) and
 * icons_large.json (20px, HUD).
 *
 * Glyphs are embedded in a {@link net.minecraft.text.Text} with
 * {@code Style.withFont(FONT)}. That lets an icon show up inline in
 * text-only UI (TAB list, nametag) and, via HudIconRenderer, in the HUD
 * and search screen - all through drawText, which is stable across
 * every supported Minecraft version (unlike DrawContext.drawTexture).
 */
public final class IconGlyphs {

    /** The custom font (assets/gtltagger/font/icons.json) these glyphs are registered under. */
    public static final Identifier FONT = Identifier.of(GTLTaggerMod.MOD_ID, "icons");

    /** Same glyphs rendered 20px tall (assets/gtltagger/font/icons_large.json) - used by the HUD. */
    public static final Identifier FONT_LARGE = Identifier.of(GTLTaggerMod.MOD_ID, "icons_large");

    private static final Map<String, Character> GLYPHS = new HashMap<>();

    static {
        char c = '\uE000';
        for (String kit : Gamemodes.CORE) {
            GLYPHS.put(kit, c++);
        }
        GLYPHS.put("Overall", c);
    }

    private IconGlyphs() {
    }

    /** The glyph character for {@code kit} (case-sensitive, matching {@link Gamemodes#CORE} or "Overall"), or null if unknown. */
    public static Character get(String kit) {
        return GLYPHS.get(kit);
    }
}
