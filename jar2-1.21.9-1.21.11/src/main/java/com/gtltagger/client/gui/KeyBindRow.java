package com.gtltagger.client.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * A button showing a KeyBinding's current key; click it, then press a
 * key to rebind (Escape cancels). Composition instead of subclassing
 * ButtonWidget, whose constructor/press signatures change between
 * Minecraft versions. The owning screen's keyPressed must call
 * {@link #tryConsumeKey} first.
 */
public final class KeyBindRow {

    private final KeyBinding keyBinding;
    private final ButtonWidget button;
    private boolean listening = false;

    public KeyBindRow(int x, int y, int width, int height, KeyBinding keyBinding) {
        this.keyBinding = keyBinding;
        this.button = ButtonWidget.builder(Text.literal(currentKeyName()), b -> {
            listening = true;
            b.setMessage(Text.literal("> press a key <"));
        }).dimensions(x, y, width, height).build();
    }

    public ButtonWidget button() {
        return button;
    }

    /** Returns true if this row consumed the key (it was waiting for one). */
    public boolean tryConsumeKey(int keyCode) {
        if (!listening) {
            return false;
        }
        listening = false;
        if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
            keyBinding.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(keyCode));
            KeyBinding.updateKeysByCode();
        }
        button.setMessage(Text.literal(currentKeyName()));
        return true;
    }

    private String currentKeyName() {
        return keyBinding.getBoundKeyLocalizedText().getString();
    }
}
