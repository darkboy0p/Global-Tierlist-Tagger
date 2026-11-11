package com.gtltagger.mixin.client;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.gtltagger.client.IconGlyphs;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.tag.TierColors;
import com.gtltagger.tag.TierResolver;
import com.gtltagger.tag.TierText;
import com.gtltagger.util.ProfileNames;

/**
 * Appends a TierTag to each name shown in the TAB player list, using the
 * player's actual tested gamemode data (see {@link TierResolver}). Left
 * and right sides are each independently on/off for TAB. The tier text
 * here is always the short code (e.g. "LT5").
 */
@Mixin(PlayerTabOverlay.class)
public abstract class PlayerListHudMixin {

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void gtltagger$appendTierTag(PlayerInfo entry, CallbackInfoReturnable<Component> cir) {
        GTLTaggerConfig config = GTLTaggerConfig.get();
        boolean leftInTab = config.leftSurface.tab;
        boolean rightInTab = config.rightSurface.tab;
        if (!leftInTab && !rightInTab) {
            return;
        }

        String ign = ProfileNames.of(entry.getProfile());
        if (ign == null) {
            return;
        }

        MutableComponent leftIcon = leftInTab ? buildIconText(TierResolver.resolveLeft(ign, config)) : null;
        MutableComponent rightIcon = rightInTab ? buildIconText(TierResolver.resolveRight(ign, config)) : null;
        if (leftIcon == null && rightIcon == null) {
            return; // never tested in either slot - do not invent a tier
        }

        MutableComponent result = Component.empty();
        if (leftIcon != null) {
            result.append(leftIcon).append(Component.literal(" "));
        }
        result.append(cir.getReturnValue());
        if (rightIcon != null) {
            result.append(Component.literal(" ")).append(rightIcon);
        }
        cir.setReturnValue(result);
    }

    /** "[kit icon glyph] TIER" (always short form), or null if that slot is unresolved. */
    private static MutableComponent buildIconText(TierResolver.Resolution resolution) {
        if (resolution == null || resolution.entry() == null || resolution.entry().tier == null) {
            return null;
        }
        String tier = resolution.entry().tier;
        MutableComponent result = Component.empty();
        Character glyph = IconGlyphs.get(resolution.gamemode());
        if (glyph != null) {
            // White so the glyph keeps the icon's real colors instead of being tinted by the tier color.
            result.append(Component.literal(String.valueOf(glyph.charValue()))
                    .setStyle(Style.EMPTY.withFont(new FontDescription.Resource(IconGlyphs.FONT))
                            .withColor(TextColor.fromRgb(0xFFFFFF))));
            result.append(Component.literal(" "));
        }
        result.append(Component.literal(TierText.shortForm(tier))
                .setStyle(Style.EMPTY.withColor(TierColors.forCode(tier))));
        return result;
    }
}
