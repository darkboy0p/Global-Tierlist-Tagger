package com.gtltagger.mixin.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.gtltagger.detect.TabTextHolder;

/**
 * Captures the raw TAB header/footer text as it arrives from the server,
 * so {@link com.gtltagger.detect.KitDetector} can read it back.
 *
 * The injector for the packet handler is required; the "world cleared"
 * cleanup is optional (require = 0) because that method has been renamed
 * before and it is only a courtesy to avoid stale text across servers.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Inject(method = "handleTabListCustomisation", at = @At("TAIL"))
    private void gtltagger$captureHeaderFooter(ClientboundTabListPacket packet, CallbackInfo ci) {
        TabTextHolder.set(packet.header(), packet.footer());
    }

    @Inject(method = "clearLevel", at = @At("TAIL"), require = 0)
    private void gtltagger$clearHeaderFooter(CallbackInfo ci) {
        TabTextHolder.clear();
    }
}
