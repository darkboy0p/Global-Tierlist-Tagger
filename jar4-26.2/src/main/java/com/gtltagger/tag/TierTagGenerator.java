package com.gtltagger.tag;

import com.gtltagger.gamemode.Gamemodes;

/**
 * Builds the literal TierTag string:
 *
 *   [:kiticon:TIER]IGN[:kiticon:TIER]
 *
 * with either side toggled off via {@code leftEnabled}/{@code
 * rightEnabled}. This is plain text meant to be typed/pasted into
 * chat - "kiticon" (see Gamemodes.iconToken) is a literal per-kit
 * token (matching servers that resolve [:nethpoticon:LT5]-shaped
 * segments via a resource-pack font/emoji system keyed per kit), not
 * an image rendered by this mod.
 */
public final class TierTagGenerator {

    private TierTagGenerator() {
    }

    /**
     * @param gamemode the kit this tier is for, used to pick the icon token (see {@link Gamemodes#iconToken}).
     * @param tier     the tier code to embed, e.g. "LT3" - required if either side is shown.
     */
    public static String generate(String ign, String gamemode, String tier, boolean leftEnabled, boolean rightEnabled) {
        if (tier == null) {
            // No tested tier for this gamemode: no icon to show on either side.
            return ign;
        }

        String token = "[:" + Gamemodes.iconToken(gamemode) + ":" + tier + "]";

        StringBuilder sb = new StringBuilder();
        if (leftEnabled) {
            sb.append(token);
        }
        sb.append(ign);
        if (rightEnabled) {
            sb.append(token);
        }
        return sb.toString();
    }
}
