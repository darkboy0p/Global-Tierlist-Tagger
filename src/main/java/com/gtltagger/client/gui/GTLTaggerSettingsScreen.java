package com.gtltagger.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import com.gtltagger.config.LeftRightMode;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.config.TierNameFormat;
import com.gtltagger.gamemode.Gamemodes;

import java.util.ArrayList;
import java.util.List;

/**
 * GTLTagger's dedicated settings menu (spec section 8). Deliberately
 * limited to exactly the options the spec lists — no extra pages.
 */
public class GTLTaggerSettingsScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int WIDGET_WIDTH = 200;

    private final Screen parent;
    private final GTLTaggerConfig config;
    private final List<KeyBindListenWidget> keyBindWidgets = new ArrayList<>();

    public GTLTaggerSettingsScreen(Screen parent, GTLTaggerConfig config) {
        super(Text.literal("GTLTagger Settings"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        keyBindWidgets.clear();
        int centerX = this.width / 2;
        int y = 30;

        // --- Tier Display ---
        this.addDrawableChild(CheckboxWidget.builder(Text.literal("Left Tier"), this.textRenderer)
                .pos(centerX - WIDGET_WIDTH / 2, y)
                .checked(config.tierMode.left)
                .callback((cb, checked) -> setLeft(checked))
                .build());
        y += ROW_HEIGHT;

        this.addDrawableChild(CheckboxWidget.builder(Text.literal("Right Tier"), this.textRenderer)
                .pos(centerX - WIDGET_WIDTH / 2, y)
                .checked(config.tierMode.right)
                .callback((cb, checked) -> setRight(checked))
                .build());
        y += ROW_HEIGHT;

        this.addDrawableChild(CyclingButtonWidget.<TierNameFormat>builder(format -> Text.literal(format.label))
                .values(TierNameFormat.values())
                .initially(config.tierNameFormat)
                .build(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20,
                        Text.literal("Tier Text Format"),
                        (button, value) -> {
                            config.tierNameFormat = value;
                            config.save();
                        }));
        y += ROW_HEIGHT + 6;

        // --- Gamemodes ---
        this.addDrawableChild(CyclingButtonWidget.<String>builder(Text::literal)
                .values(Gamemodes.ORDER)
                .initially(config.gamemode1)
                .build(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20,
                        Text.literal("Gamemode 1"),
                        (button, value) -> {
                            config.gamemode1 = value;
                            config.save();
                        }));
        y += ROW_HEIGHT;

        this.addDrawableChild(CyclingButtonWidget.<String>builder(Text::literal)
                .values(Gamemodes.ORDER)
                .initially(config.gamemode2)
                .build(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20,
                        Text.literal("Gamemode 2"),
                        (button, value) -> {
                            config.gamemode2 = value;
                            config.save();
                        }));
        y += ROW_HEIGHT;

        this.addDrawableChild(CheckboxWidget.builder(Text.literal("Enable second gamemode"), this.textRenderer)
                .pos(centerX - WIDGET_WIDTH / 2, y)
                .checked(config.secondGamemodeEnabled)
                .callback((cb, checked) -> {
                    config.secondGamemodeEnabled = checked;
                    config.save();
                })
                .build());
        y += ROW_HEIGHT + 6;

        // --- TAB ---
        this.addDrawableChild(CheckboxWidget.builder(Text.literal("Show tiers in TAB"), this.textRenderer)
                .pos(centerX - WIDGET_WIDTH / 2, y)
                .checked(config.tabTiersEnabled)
                .callback((cb, checked) -> {
                    config.tabTiersEnabled = checked;
                    config.save();
                })
                .build());
        y += ROW_HEIGHT;

        // --- Nametag ---
        this.addDrawableChild(CheckboxWidget.builder(Text.literal("Show tiers in Nametag"), this.textRenderer)
                .pos(centerX - WIDGET_WIDTH / 2, y)
                .checked(config.nametagTiersEnabled)
                .callback((cb, checked) -> {
                    config.nametagTiersEnabled = checked;
                    config.save();
                })
                .build());
        y += ROW_HEIGHT + 6;

        // --- Automatic Detection ---
        this.addDrawableChild(CheckboxWidget.builder(Text.literal("Automatic Kit Detection"), this.textRenderer)
                .pos(centerX - WIDGET_WIDTH / 2, y)
                .checked(config.automaticDetectionEnabled)
                .callback((cb, checked) -> {
                    config.automaticDetectionEnabled = checked;
                    config.save();
                })
                .build());
        y += ROW_HEIGHT + 6;

        // --- HUD ---
        this.addDrawableChild(CheckboxWidget.builder(Text.literal("Enable HUD"), this.textRenderer)
                .pos(centerX - WIDGET_WIDTH / 2, y)
                .checked(config.hudEnabled)
                .callback((cb, checked) -> {
                    config.hudEnabled = checked;
                    config.save();
                })
                .build());
        y += ROW_HEIGHT;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Edit HUD Position"), button ->
                        this.client.setScreen(new HudPositionScreen(this, config)))
                .dimensions(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20)
                .build());
        y += ROW_HEIGHT + 6;

        // --- Keybinds ---
        addKeyBindRow(centerX, y, "Switch Gamemode", findKeyBinding("key.gtltagger.switch_gamemode"));
        y += ROW_HEIGHT;
        addKeyBindRow(centerX, y, "Toggle Left/Right", findKeyBinding("key.gtltagger.toggle_left_right"));
        y += ROW_HEIGHT + 10;

        // --- Done ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            config.save();
            this.client.setScreen(parent);
        }).dimensions(centerX - 75, y, 150, 20).build());
    }

    private void addKeyBindRow(int centerX, int y, String label, KeyBinding binding) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> {}).dimensions(
                centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH / 2 - 4, 20).build()).active = false;

        if (binding != null) {
            KeyBindListenWidget widget = new KeyBindListenWidget(
                    centerX + 4, y, WIDGET_WIDTH / 2 - 4, 20, binding);
            keyBindWidgets.add(widget);
            this.addDrawableChild(widget);
        }
    }

    private KeyBinding findKeyBinding(String translationKey) {
        for (KeyBinding binding : this.client.options.allKeys) {
            if (binding.getTranslationKey().equals(translationKey)) {
                return binding;
            }
        }
        return null;
    }

    private void setLeft(boolean enabled) {
        config.tierMode = fromFlags(enabled, config.tierMode.right);
        config.save();
    }

    private void setRight(boolean enabled) {
        config.tierMode = fromFlags(config.tierMode.left, enabled);
        config.save();
    }

    private static LeftRightMode fromFlags(boolean left, boolean right) {
        if (left && right) return LeftRightMode.BOTH;
        if (left) return LeftRightMode.LEFT;
        if (right) return LeftRightMode.RIGHT;
        return LeftRightMode.OFF;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (KeyBindListenWidget widget : keyBindWidgets) {
            if (widget.tryConsumeKey(keyCode, scanCode)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        config.save();
        this.client.setScreen(parent);
    }
}
