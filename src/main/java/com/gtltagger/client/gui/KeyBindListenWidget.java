package com.gtltagger.client.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

/**
 * A button that shows a KeyBinding's current key; click it, then press
 * any key to rebind (spec section 8: "click a keybind and press a new
 * key to rebind it"). The owning {@link Screen}'s keyPressed override
 * must call {@link #tryConsumeKey} before its own handling — see
 * {@link GTLTaggerSettingsScreen}.
 */
public class KeyBindListenWidget extends ButtonWidget {

    private final KeyBinding keyBinding;
    private boolean listening = false;

    public KeyBindListenWidget(int x, int y, int width, int height, KeyBinding keyBinding) {
        super(x, y, width, height, Text.literal(""), button -> {}, DEFAULT_NARRATION_SUPPLIER);
        this.keyBinding = keyBinding;
        updateLabel();
    }

    @Override
    public void onPress() {
        listening = true;
        setMessage(Text.literal("> press a key <"));
    }

    /** Called by the screen's keyPressed before normal handling. Returns true if this widget consumed it. */
    public boolean tryConsumeKey(int keyCode, int scanCode) {
        if (!listening) {
            return false;
        }
        keyBinding.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(keyCode));
        KeyBinding.updateKeysByCode();
        listening = false;
        updateLabel();
        return true;
    }

    public boolean isListening() {
        return listening;
    }

    private void updateLabel() {
        setMessage(Text.literal(keyBinding.getBoundKeyLocalizedText().getString()));
    }
}
