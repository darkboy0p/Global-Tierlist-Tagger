package com.gtltagger.client.compat;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Every Minecraft call that differs between 26.x releases lives HERE,
 * so a version bump means editing this one file. This is the 26.1
 * flavor (Minecraft 26.1 - 26.1.x).
 *
 * Other releases differ as follows (see the jar4-26.2 / jar5-26.3 copies):
 *  - 26.2: current screen + setScreen moved from Minecraft onto Gui, the
 *    overlay message moved to Gui.hud, options.hideGui became Hud#isHidden.
 *  - 26.3: GLFW replaced by SDL - KeyMapping no longer takes an
 *    InputConstants.Type, and Type.KEYSYM was renamed KEYBOARD.
 */
public final class Mc {

    private Mc() {
    }

    public static void openScreen(Minecraft mc, Screen screen) {
        mc.setScreen(screen);
    }

    public static Screen currentScreen(Minecraft mc) {
        return mc.screen;
    }

    public static boolean isHudHidden(Minecraft mc) {
        return mc.options.hideGui;
    }

    /** Shows a short message above the hotbar. */
    public static void overlayMessage(Minecraft mc, Component message) {
        if (mc.player != null) {
            mc.player.sendOverlayMessage(message);
        }
    }

    public static KeyMapping newKey(String id, int keyCode, KeyMapping.Category category) {
        return new KeyMapping(id, InputConstants.Type.KEYSYM, keyCode, category);
    }

    public static void bindKey(KeyMapping mapping, int keyCode) {
        mapping.setKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
    }
}
