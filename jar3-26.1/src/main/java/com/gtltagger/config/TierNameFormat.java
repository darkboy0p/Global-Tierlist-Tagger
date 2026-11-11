package com.gtltagger.config;

/**
 * Whether a TierTag's tier text spells the tier out ("Low Tier 5") or
 * uses the short code ("LT5"). Applies to the HUD and the in-world
 * nametag - the TAB list always uses the short form regardless of
 * this setting, since a full-form tier per player gets long fast in
 * a crowded tab list.
 */
public enum TierNameFormat {
    SHORT("Short (LT5)"),
    FULL("Full (Low Tier 5)");

    public final String label;

    TierNameFormat(String label) {
        this.label = label;
    }

    public TierNameFormat next() {
        return this == SHORT ? FULL : SHORT;
    }
}
