package com.gtltagger.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import com.gtltagger.client.compat.Mc;

/**
 * A button showing a KeyMapping's current key; click it, then press a
 * key to rebind (Escape cancels). Composition instead of subclassing
 * Button, whose constructor signature is not stable across versions.
 * The owning screen's keyPressed must call {@link #tryConsumeKey} first.
 */
public final class KeyBindRow {

    private final KeyMapping mapping;
    private final Button button;
    private boolean listening = false;

    public KeyBindRow(int x, int y, int width, int height, KeyMapping mapping) {
        this.mapping = mapping;
        this.button = Button.builder(Component.literal(currentKeyName()), b -> {
            listening = true;
            b.setMessage(Component.literal("> press a key <"));
        }).bounds(x, y, width, height).build();
    }

    public Button button() {
        return button;
    }

    /** Returns true if this row consumed the key (it was waiting for one). */
    public boolean tryConsumeKey(int keyCode) {
        if (!listening) {
            return false;
        }
        listening = false;
        if (keyCode != InputConstants.KEY_ESCAPE) {
            Mc.bindKey(mapping, keyCode);
            KeyMapping.resetMapping();
        }
        button.setMessage(Component.literal(currentKeyName()));
        return true;
    }

    private String currentKeyName() {
        return mapping.getTranslatedKeyMessage().getString();
    }
}
