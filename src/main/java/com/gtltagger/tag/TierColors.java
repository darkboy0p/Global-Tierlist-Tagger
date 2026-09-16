package com.gtltagger.tag;

import net.minecraft.text.TextColor;

import java.util.Map;

/**
 * Tier-number to color mapping, used to color tier text/icons
 * wherever a TierTag is shown (TAB, in-world nametag, HUD):
 *
 *   Tier 1 (HT1/LT1) - Red    - highest skill
 *   Tier 2 (HT2/LT2) - Orange
 *   Tier 3 (HT3/LT3) - Gold
 *   Tier 4 (HT4/LT4) - Green
 *   Tier 5 (HT5/LT5) - Cyan
 *
 * These are plain RGB {@link TextColor}s rather than vanilla
 * {@link net.minecraft.util.Formatting} constants, because vanilla's
 * Formatting enum has no "orange" or "cyan" entry to match these
 * against - it only has GOLD (used here for Tier 3) and AQUA (close
 * to, but not, this Cyan).
 */
public final class TierColors {

    private static final Map<Integer, TextColor> BY_TIER = Map.of(
            1, TextColor.fromRgb(0xFF5555), // Red
            2, TextColor.fromRgb(0xFF8000), // Orange
            3, TextColor.fromRgb(0xFFD700), // Gold
            4, TextColor.fromRgb(0x55FF55), // Green
            5, TextColor.fromRgb(0x55FFFF)  // Cyan
    );

    /** Neutral gray for a tier number outside 1-5 (e.g. -1/unparsed) - keeps untiered text visible without implying a rank. */
    private static final TextColor FALLBACK = TextColor.fromRgb(0xAAAAAA);

    private TierColors() {
    }

    /** The color for a 1-5 tier number, or {@link #FALLBACK} for anything else. */
    public static TextColor forTierNumber(int tierNumber) {
        return BY_TIER.getOrDefault(tierNumber, FALLBACK);
    }

    /** Convenience: colors a raw tier code (e.g. "LT5") directly via {@link TierText#tierNumber}. */
    public static TextColor forCode(String code) {
        return forTierNumber(TierText.tierNumber(code));
    }

    /** Same mapping as a packed RGB int, for APIs that want that instead of a {@link TextColor} (e.g. DrawContext.drawText). */
    public static int rgbForTierNumber(int tierNumber) {
        return forTierNumber(tierNumber).getRgb();
    }
}
