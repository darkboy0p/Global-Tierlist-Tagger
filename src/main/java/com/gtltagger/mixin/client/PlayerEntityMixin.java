package com.gtltagger.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.config.LeftRightMode;
import com.gtltagger.gamemode.Gamemodes;
import com.gtltagger.tag.TierColors;
import com.gtltagger.tag.TierResolver;
import com.gtltagger.tag.TierText;

/**
 * Appends a TierTag to the player's in-world nametag (the label
 * rendered above their head) - same left/right tier data as the TAB
 * list (see {@link TierResolver}), but using the Short/Full text
 * format setting (unlike TAB, which is always short).
 *
 * Targets PlayerEntity#getDisplayName() rather than a renderer
 * method: the renderer's own label-building method
 * (EntityRenderer#renderLabelIfPresent) changed its own first
 * parameter from the entity itself to a separate "render state"
 * object starting in 1.21.2 (part of that version's rendering
 * refactor), so it isn't a single stable mixin target across this
 * repo's whole 1.21.1-1.21.7 matrix. getDisplayName() is unchanged
 * across that whole range, PlayerEntity already overrides it to
 * build exactly the text the renderer puts above a player's head, and
 * this is the same hook other nametag-editing mods use for this
 * purpose. Side effect: a few other vanilla UI spots that also read
 * getDisplayName() (e.g. the spectator player list) will show the
 * TierTag too - that's normally desirable and is what those other
 * mods do as well.
 *
 * Guarded to the logical client only: Mixin transforms the whole
 * classloader, not just "client-side" code, so on an integrated
 * singleplayer server this class is also loaded for the server's own
 * PlayerEntity instances - and this mod's config/tier data only
 * exists client-side.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void gtltagger$appendNametagTierTag(CallbackInfoReturnable<Text> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!self.getWorld().isClient()) {
            return;
        }

        GTLTaggerConfig config = GTLTaggerConfig.get();
        if (!config.nametagTiersEnabled) {
            return;
        }

        GameProfile profile = self.getGameProfile();
        if (profile == null || profile.getName() == null) {
            return;
        }
        String ign = profile.getName();

        LeftRightMode mode = config.tierMode;
        if (!mode.left && !mode.right) {
            return;
        }

        MutableText leftIcon = mode.left ? buildIconText(TierResolver.resolveLeft(ign, config), config) : null;
        MutableText rightIcon = mode.right ? buildIconText(TierResolver.resolveRight(ign, config), config) : null;
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

    /** Builds one "[:kiticon:TIER]" segment in the configured Short/Full format, colored per its own tier number, or null if that slot is unresolved. */
    private static MutableText buildIconText(TierResolver.Resolution resolution, GTLTaggerConfig config) {
        if (resolution == null || resolution.entry() == null || resolution.entry().tier == null) {
            return null;
        }
        String tier = resolution.entry().tier;
        String token = Gamemodes.iconToken(resolution.gamemode());
        String text = "[:" + token + ":" + TierText.render(tier, config.tierNameFormat) + "]";
        return Text.literal(text).setStyle(Style.EMPTY.withColor(TierColors.forCode(tier)));
    }
}
