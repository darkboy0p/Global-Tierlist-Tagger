package com.gtltagger.tag;

import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.data.TierDatabase;
import com.gtltagger.data.TierEntry;
import com.gtltagger.detect.KitDetector;

import java.util.Map;

/**
 * Resolves which gamemode + tier to show on each side of a player's
 * TierTag (TAB list, in-world nametag), per spec section 5's
 * "important logic" note: use the player's actual tested data, never
 * invent a tier for an untested gamemode.
 *
 * The two sides intentionally carry different meaning, matching how
 * this mod's two gamemode slots are used elsewhere:
 *  - LEFT tracks whatever's currently relevant - the auto-detected
 *    kit if enabled and tested, else Gamemode 1. This is the "what
 *    are we playing right now" slot.
 *  - RIGHT is a fixed secondary reference: Gamemode 2's tier -
 *    whether it's actually shown in TAB/the nametag is up to
 *    config.rightSurface, not this class - e.g. always show a
 *    player's Crystal tier alongside whatever kit is currently
 *    active in NethPot.
 * Either side is simply omitted (returns null) if its target
 * gamemode was never tested for that player - this mod never
 * fabricates a tier to fill a slot.
 */
public final class TierResolver {

    private TierResolver() {
    }

    /** One resolved slot: which gamemode it came from (for picking that kit's icon token), and the player's entry for it. */
    public record Resolution(String gamemode, TierEntry entry) {
    }

    public static Resolution resolveLeft(String ign, GTLTaggerConfig config) {
        Map<String, TierEntry> tested = TierDatabase.getAll(ign);
        if (tested.isEmpty()) {
            return null;
        }

        if (config.automaticDetectionEnabled) {
            String detected = KitDetector.detect();
            if (detected != null && tested.containsKey(detected)) {
                return new Resolution(detected, tested.get(detected));
            }
        }

        TierEntry entry = tested.get(config.gamemode1);
        return entry != null ? new Resolution(config.gamemode1, entry) : null;
    }

    public static Resolution resolveRight(String ign, GTLTaggerConfig config) {
        // Whether the right side is actually shown anywhere is up to
        // config.rightSurface (checked by the TAB/nametag mixins) -
        // this always resolves Gamemode 2's tier if tested.
        TierEntry entry = TierDatabase.get(ign, config.gamemode2);
        return entry != null ? new Resolution(config.gamemode2, entry) : null;
    }
}
