package com.gtltagger.config;

/**
 * Which side(s) of the IGN get a tier icon in the generated TierTag.
 * Cycling order (spec section 4): BOTH -> LEFT -> RIGHT -> OFF -> BOTH
 */
public enum LeftRightMode {
    BOTH(true, true, "Both"),
    LEFT(true, false, "Left only"),
    RIGHT(false, true, "Right only"),
    OFF(false, false, "Off");

    public final boolean left;
    public final boolean right;
    public final String label;

    LeftRightMode(boolean left, boolean right, String label) {
        this.left = left;
        this.right = right;
        this.label = label;
    }

    public LeftRightMode next() {
        switch (this) {
            case BOTH: return LEFT;
            case LEFT: return RIGHT;
            case RIGHT: return OFF;
            case OFF:
            default: return BOTH;
        }
    }
}
