package com.gtltagger.mixin.client;

import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.tag.TierDisplayText;
import com.gtltagger.tag.TierResolver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds tested tier components to the in-world player nametag. */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void gtltagger$appendNametagTierTag(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!player.getWorld().isClient()) {
            return;
        }

        GTLTaggerConfig config = GTLTaggerConfig.get();
        boolean leftEnabled = config.leftSurface != null && config.leftSurface.tag;
        boolean rightEnabled = config.rightSurface != null && config.rightSurface.tag;
        if (!leftEnabled && !rightEnabled) {
            return;
        }

        String ign = player.getGameProfile().getName();
        if (ign == null || ign.isBlank()) {
            return;
        }

        MutableText left = leftEnabled
                ? TierDisplayText.build(TierResolver.resolveLeft(ign, config), true, config)
                : null;
        MutableText right = rightEnabled
                ? TierDisplayText.build(TierResolver.resolveRight(ign, config), true, config)
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
