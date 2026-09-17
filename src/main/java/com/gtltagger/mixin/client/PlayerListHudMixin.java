package com.gtltagger.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.gtltagger.client.IconGlyphs;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.tag.TierColors;
import com.gtltagger.tag.TierResolver;
import com.gtltagger.tag.TierText;

/**
 * Appends a TierTag to each name shown in the TAB player list
 * (spec section 5). Uses the player's actual tested gamemode data -
 * NOT whatever gamemode the user currently has selected - per the
 * spec's "important logic" note (see {@link TierResolver}).
 *
 * Left and right sides can show two different gamemodes' tiers (see
 * TierResolver), each independently on/off for TAB via its own
 * {@code config.leftSurface}/{@code rightSurface} (see
 * {@link com.gtltagger.config.DisplaySurface}). The tier text here is
 * always the short code (e.g. "LT5") - unlike the HUD and nametag,
 * TAB never spells it out, since a full-form tier per row gets long
 * fast in a crowded list. Each icon segment is colored by its own
 * tier number (see TierColors), independently of the other side.
 */
@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void gtltagger$appendTierTag(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        GTLTaggerConfig config = GTLTaggerConfig.get();
        boolean leftInTab = config.leftSurface.tab;
        boolean rightInTab = config.rightSurface.tab;
        if (!leftInTab && !rightInTab) {
            return;
        }

        GameProfile profile = entry.getProfile();
        if (profile == null || profile.getName() == null) {
            return;
        }
        String ign = profile.getName();

        MutableText leftIcon = leftInTab ? buildIconText(TierResolver.resolveLeft(ign, config)) : null;
        MutableText rightIcon = rightInTab ? buildIconText(TierResolver.resolveRight(ign, config)) : null;
        if (leftIcon == null && rightIcon == null) {
            return; // never tested in either slot - spec: do not invent a tier
        }

        MutableText result = Text.empty();
        if (leftIcon != null) {
            result.append(leftIcon).append(Text.literal(" "));
        }
        result.append(cir.getReturnValue());
        if (rightIcon != null) {
            result.append(Text.literal(" ")).append(rightIcon);
        }
        cir.setReturnValue(result);
    }

    /** Builds "[kit icon glyph] TIER" (always short form) - a genuine PNG icon (see {@link IconGlyphs}) followed by the tier code colored per its own tier number - or null if that slot is unresolved. */
    private static MutableText buildIconText(TierResolver.Resolution resolution) {
        if (resolution == null || resolution.entry() == null || resolution.entry().tier == null) {
            return null;
        }
        String tier = resolution.entry().tier;
        MutableText result = Text.empty();
        Character glyph = IconGlyphs.get(resolution.gamemode());
        if (glyph != null) {
            // White so the glyph draws at the icon PNG's real colors instead of being tinted by the tier color below.
            result.append(Text.literal(String.valueOf(glyph.charValue()))
                    .setStyle(Style.EMPTY.withFont(IconGlyphs.FONT).withColor(TextColor.fromRgb(0xFFFFFF))));
            result.append(Text.literal(" "));
        }
        result.append(Text.literal(TierText.shortForm(tier)).setStyle(Style.EMPTY.withColor(TierColors.forCode(tier))));
        return result;
    }
}
