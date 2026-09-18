package com.gtltagger.mixin.client;

import com.mojang.authlib.GameProfile;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.tag.TierDisplayText;
import com.gtltagger.tag.TierResolver;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds tested tier components to player names rendered in the TAB list. */
@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void gtltagger$appendTierTag(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        GTLTaggerConfig config = GTLTaggerConfig.get();
        boolean leftEnabled = config.leftSurface != null && config.leftSurface.tab;
        boolean rightEnabled = config.rightSurface != null && config.rightSurface.tab;
        if (!leftEnabled && !rightEnabled) {
            return;
        }

        GameProfile profile = entry.getProfile();
        if (profile == null || profile.getName() == null) {
            return;
        }

        String ign = profile.getName();
        MutableText left = leftEnabled
                ? TierDisplayText.build(TierResolver.resolveLeft(ign, config), false, config)
                : null;
        MutableText right = rightEnabled
                ? TierDisplayText.build(TierResolver.resolveRight(ign, config), false, config)
                : null;

        if (left == null && right == null) {
            return;
        }

        MutableText result = Text.empty();
        if (left != null) {
            result.append(left).append(Text.literal(" "));
        }
        result.append(cir.getReturnValue());
        if (right != null) {
            result.append(Text.literal(" ")).append(right);
        }
        cir.setReturnValue(result);
    }
}
