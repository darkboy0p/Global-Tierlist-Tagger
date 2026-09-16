package com.gtltagger.tag;

import com.gtltagger.config.TierNameFormat;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a raw tier code from the GlobalTierlist API (e.g. "LT5",
 * "HT3" - Low Tier / High Tier followed by a number) into display
 * text, and extracts its 1-5 tier number for coloring (see
 * {@link TierColors}). Codes that don't match the LT/HT + digits
 * shape are passed through unchanged in both forms, uncolored.
 */
public final class TierText {

    private static final Pattern TRAILING_DIGITS = Pattern.compile("(\\d+)$");

    private TierText() {
    }

    /** The short form is just the raw code, upper-cased (e.g. "lt5" -> "LT5"). */
    public static String shortForm(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    /** The full form spells the prefix out (e.g. "HT3" -> "High Tier 3"). Unrecognized codes pass through unchanged. */
    public static String fullForm(String code) {
        if (code == null) {
            return null;
        }
        String upper = code.trim().toUpperCase(Locale.ROOT);
        if (upper.startsWith("HT")) {
            return "High Tier " + upper.substring(2);
        }
        if (upper.startsWith("LT")) {
            return "Low Tier " + upper.substring(2);
        }
        return code;
    }

    /** {@link #shortForm} or {@link #fullForm}, per {@code format}. */
    public static String render(String code, TierNameFormat format) {
        return format == TierNameFormat.FULL ? fullForm(code) : shortForm(code);
    }

    /** The 1-5 tier number this code represents (for {@link TierColors}), or -1 if it can't be parsed. */
    public static int tierNumber(String code) {
        if (code == null) {
            return -1;
        }
        Matcher matcher = TRAILING_DIGITS.matcher(code.trim());
        if (!matcher.find()) {
            return -1;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
