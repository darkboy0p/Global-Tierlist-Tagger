package com.gtltagger.config;

/**
 * Which UI surface(s) show one side (left or right) of a player's
 * TierTag: the TAB list, the in-world nametag ("Tag"), both, or
 * neither. Each side (see {@link GTLTaggerConfig#leftSurface} /
 * {@link GTLTaggerConfig#rightSurface}) picks this independently, so
 * e.g. the left side can show in TAB only while the right side shows
 * in the nametag only, and either side can be fully disabled by
 * choosing {@link #OFF} - there's no separate master
 * "show tiers in TAB"/"show tiers in Nametag" toggle anymore.
 */
public enum DisplaySurface {
    OFF(false, false, "Off"),
    TAB(true, false, "Tab only"),
    TAG(false, true, "Tag only"),
    BOTH(true, true, "Tab + Tag");

    /** Whether this surface includes the TAB player list. */
    public final boolean tab;
    /** Whether this surface includes the in-world nametag ("Tag"). */
    public final boolean tag;
    public final String label;

    DisplaySurface(boolean tab, boolean tag, String label) {
        this.tab = tab;
        this.tag = tag;
        this.label = label;
    }

    /** True unless this side is fully {@link #OFF} - used where a caller just needs an on/off flag (e.g. the chat-paste TierTag). */
    public boolean enabled() {
        return tab || tag;
    }

    /** Cycling order: OFF -> TAB -> TAG -> BOTH -> OFF. */
    public DisplaySurface next() {
        switch (this) {
            case OFF: return TAB;
            case TAB: return TAG;
            case TAG: return BOTH;
            case BOTH:
            default: return OFF;
        }
    }
}
