package com.gtltagger.data;

/** A player's tier (and optional peak tier) for one gamemode. */
public class TierEntry {
    public String tier;
    public String peak;

    public TierEntry() {
    }

    public TierEntry(String tier, String peak) {
        this.tier = tier;
        this.peak = peak;
    }
}
