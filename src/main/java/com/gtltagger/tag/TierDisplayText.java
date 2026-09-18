package com.gtltagger.tag;

import com.gtltagger.client.IconGlyphs;
import com.gtltagger.config.GTLTaggerConfig;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

/** Builds the inline icon + tier component used by TAB and player nametags. */
public final class TierDisplayText {

    private TierDisplayText() {
    }

    /** Returns null when the resolution has no tested tier. */
    public static MutableText build(TierResolver.Resolution resolution, boolean fullFormat, GTLTaggerConfig config) {
        if (resolution == null || resolution.entry() == null || resolution.entry().tier == null) {
            return null;
        }

        String tier = resolution.entry().tier;
        MutableText result = Text.empty();
        Character glyph = IconGlyphs.get(resolution.gamemode());
        if (glyph != null) {
            // The icon must keep the custom bitmap font style. Do not apply the tier color to it.
            result.append(Text.literal(String.valueOf(glyph))
                    .setStyle(Style.EMPTY
                            .withFont(IconGlyphs.FONT)
                            .withColor(TextColor.fromRgb(0xFFFFFF))));
            result.append(Text.literal(" "));
        }

        String displayTier = fullFormat
                ? TierText.render(tier, config.tierNameFormat)
                : TierText.shortForm(tier);
        result.append(Text.literal(displayTier)
                .setStyle(Style.EMPTY.withColor(TierColors.forCode(tier))));
        return result;
    }
}
