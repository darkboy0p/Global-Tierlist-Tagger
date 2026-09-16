package com.gtltagger.tag;

import com.gtltagger.config.LeftRightMode;

/**
 * Builds the literal TierTag string:
 *
 *   [:icon:TIER]IGN[:icon:TIER]
 *
 * with either side toggled off per {@link LeftRightMode}. This is
 * plain text meant to be typed/pasted into chat — "icon" here is a
 * literal token (matching servers that resolve [:icon:XXX] via a
 * resource-pack font/emoji system), not an image rendered by this mod.
 */
public final class TierTagGenerator {

    private TierTagGenerator() {
    }

    /** @param tier the tier code to embed, e.g. "LT3" — required if either side is shown. */
    public static String generate(String ign, String tier, LeftRightMode mode) {
        if (tier == null) {
            // No tested tier for this gamemode: no icon to show on either side.
            return ign;
        }

        StringBuilder sb = new StringBuilder();
        if (mode.left) {
            sb.append("[:icon:").append(tier).append(']');
        }
        sb.append(ign);
        if (mode.right) {
            sb.append("[:icon:").append(tier).append(']');
        }
        return sb.toString();
    }
}
