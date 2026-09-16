package com.gtltagger.detect;

import net.minecraft.text.Text;

/**
 * Latest TAB header/footer text, captured by
 * {@link com.gtltagger.mixin.client.ClientPlayNetworkHandlerMixin} as each
 * PlayerListHeaderS2CPacket arrives.
 *
 * ClientPlayNetworkHandler used to expose getPlayerListHeader()/
 * getPlayerListFooter() getters, but those were removed — the handler
 * now only has the onPlayerListHeader(...) callback that applies the
 * packet to the HUD. This holder is the only way left to read the
 * current header/footer back out, which {@link KitDetector} needs.
 */
public final class TabTextHolder {

    private static volatile Text header;
    private static volatile Text footer;

    private TabTextHolder() {
    }

    public static void set(Text newHeader, Text newFooter) {
        header = newHeader;
        footer = newFooter;
    }

    /** Called when the network handler is torn down, to avoid leaking stale text into a new server/session. */
    public static void clear() {
        header = null;
        footer = null;
    }

    public static Text getHeader() {
        return header;
    }

    public static Text getFooter() {
        return footer;
    }
}
