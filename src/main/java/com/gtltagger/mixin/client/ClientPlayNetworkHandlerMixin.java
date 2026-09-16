package com.gtltagger.mixin.client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.gtltagger.detect.TabTextHolder;

/**
 * Captures the raw TAB header/footer text as it arrives from the server.
 *
 * ClientPlayNetworkHandler.getPlayerListHeader()/getPlayerListFooter()
 * (used by {@link com.gtltagger.detect.KitDetector} for automatic kit
 * detection, spec section 6) were removed — the handler now only has
 * the onPlayerListHeader(PlayerListHeaderS2CPacket) callback that hands
 * the text straight to the HUD. This mixin taps that same callback and
 * stashes the text in {@link TabTextHolder} so KitDetector can still
 * read it back out.
 */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onPlayerListHeader", at = @At("TAIL"))
    private void gtltagger$captureHeaderFooter(PlayerListHeaderS2CPacket packet, CallbackInfo ci) {
        TabTextHolder.set(packet.header(), packet.footer());
    }

    @Inject(method = "clearWorld", at = @At("TAIL"))
    private void gtltagger$clearHeaderFooter(CallbackInfo ci) {
        TabTextHolder.clear();
    }
}
