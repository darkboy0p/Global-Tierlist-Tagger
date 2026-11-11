package com.gtltagger.mixin.client;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
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
 * Appends a TierTag to the player's in-world nametag. Hooks
 * Player#getDisplayName() (the text the renderer puts above a head)
 * rather than a renderer method, which keeps this stable across versions.
 * Uses the Short/Full text format setting - the only place it applies.
 * Guarded to client-side players, since this mod's data only exists there.
 */
@Mixin(Player.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void gtltagger$appendNametagTierTag(CallbackInfoReturnable<Component> cir) {
        Player self = (Player) (Object) this;
        if (!(self instanceof AbstractClientPlayer)) {
            return;
        }

        GTLTaggerConfig config = GTLTaggerConfig.get();
        boolean leftInTag = config.leftSurface.tag;
        boolean rightInTag = config.rightSurface.tag;
        if (!leftInTag && !rightInTag) {
            return;
        }

        String ign = self.getName().getString();
        if (ign == null || ign.isEmpty()) {
            return;
        }

        MutableComponent leftIcon = leftInTag ? buildIconText(TierResolver.resolveLeft(ign, config), config) : null;
        MutableComponent rightIcon = rightInTag ? buildIconText(TierResolver.resolveRight(ign, config), config) : null;
        if (leftIcon == null && rightIcon == null) {
            return;
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

    /** "[kit icon glyph] TIER" in the configured Short/Full format, or null if that slot is unresolved. */
    private static MutableComponent buildIconText(TierResolver.Resolution resolution, GTLTaggerConfig config) {
        if (resolution == null || resolution.entry() == null || resolution.entry().tier == null) {
            return null;
        }
        String tier = resolution.entry().tier;
        MutableComponent result = Component.empty();
        Character glyph = IconGlyphs.get(resolution.gamemode());
        if (glyph != null) {
            result.append(Component.literal(String.valueOf(glyph.charValue()))
                    .setStyle(Style.EMPTY.withFont(new FontDescription.Resource(IconGlyphs.FONT))
                            .withColor(TextColor.fromRgb(0xFFFFFF))));
            result.append(Component.literal(" "));
        }
        result.append(Component.literal(TierText.render(tier, config.tierNameFormat))
                .setStyle(Style.EMPTY.withColor(TierColors.forCode(tier))));
        return result;
    }
}
