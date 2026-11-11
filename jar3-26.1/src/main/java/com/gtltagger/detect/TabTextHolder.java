package com.gtltagger.detect;

import net.minecraft.network.chat.Component;

/**
 * Latest TAB header/footer text, captured by
 * {@link com.gtltagger.mixin.client.ClientPlayNetworkHandlerMixin} as each
 * tab-list customisation packet arrives. {@link KitDetector} reads it back.
 */
public final class TabTextHolder {

    private static volatile Component header;
    private static volatile Component footer;

    private TabTextHolder() {
    }

    public static void set(Component newHeader, Component newFooter) {
        header = newHeader;
        footer = newFooter;
    }

    /** Called when the network handler is torn down, to avoid leaking stale text into a new server/session. */
    public static void clear() {
        header = null;
        footer = null;
    }

    public static Component getHeader() {
        return header;
    }

    public static Component getFooter() {
        return footer;
    }
}
