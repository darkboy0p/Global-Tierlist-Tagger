package com.gtltagger.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import com.gtltagger.config.DisplaySurface;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.config.TierNameFormat;
import com.gtltagger.gamemode.Gamemodes;

import java.util.ArrayList;
import java.util.List;

/**
 * GTLTagger's dedicated settings menu (spec section 8). Left and
 * right each get an identical pair of controls - which gamemode, and
 * which surface(s) (TAB / Tag / Both / Off) show it - since the two
 * sides are otherwise symmetric.
 */
public class GTLTaggerSettingsScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int WIDGET_WIDTH = 200;
    private static final int HALF_WIDTH = WIDGET_WIDTH / 2 - 4;

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
        int y = 24;

        // --- Left side ---
        y = addSideRow(centerX, y, "Left", config.gamemode1, config.leftSurface,
                gamemode -> {
                    config.gamemode1 = gamemode;
                    config.save();
                },
                surface -> {
                    config.leftSurface = surface;
                    config.save();
                });

        // --- Right side (identical layout to Left) ---
        y = addSideRow(centerX, y, "Right", config.gamemode2, config.rightSurface,
                gamemode -> {
                    config.gamemode2 = gamemode;
                    config.save();
                },
                surface -> {
                    config.rightSurface = surface;
                    config.save();
                });
        y += 6;

        // --- Tier text format (nametag only) ---
        this.addDrawableChild(CyclingButtonWidget.<TierNameFormat>builder(format -> Text.literal(format.label))
                .values(TierNameFormat.values())
                .initially(config.tierNameFormat)
                .build(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20,
                        Text.literal("Tier Text Format (Tag only)"),
                        (button, value) -> {
                            config.tierNameFormat = value;
                            config.save();
                        }));
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
        addKeyBindRow(centerX, y, "Cycle Left Gamemode", findKeyBinding("key.gtltagger.cycle_left_gamemode"));
        y += ROW_HEIGHT;
        addKeyBindRow(centerX, y, "Cycle Right Gamemode", findKeyBinding("key.gtltagger.cycle_right_gamemode"));
        y += ROW_HEIGHT;
        addKeyBindRow(centerX, y, "Search Player (Peek)", findKeyBinding("key.gtltagger.open_player_search"));
        y += ROW_HEIGHT;
        addKeyBindRow(centerX, y, "Copy My TierTag", findKeyBinding("key.gtltagger.copy_tag"));
        y += ROW_HEIGHT + 10;

        // --- Done ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            config.save();
            this.client.setScreen(parent);
        }).dimensions(centerX - 75, y, 150, 20).build());
    }

    /** One "Left"/"Right" row pair: a gamemode cycler and a display-surface cycler side by side. Returns the y position after this row. */
    private int addSideRow(int centerX, int y, String label, String currentGamemode, DisplaySurface currentSurface,
                            java.util.function.Consumer<String> onGamemode,
                            java.util.function.Consumer<DisplaySurface> onSurface) {
        this.addDrawableChild(CyclingButtonWidget.<String>builder(Text::literal)
                .values(Gamemodes.ORDER)
                .initially(currentGamemode)
                .build(centerX - WIDGET_WIDTH / 2, y, HALF_WIDTH, 20,
                        Text.literal(label + " Gamemode"),
                        (button, value) -> onGamemode.accept(value)));

        this.addDrawableChild(CyclingButtonWidget.<DisplaySurface>builder(surface -> Text.literal(surface.label))
                .values(DisplaySurface.values())
                .initially(currentSurface)
                .build(centerX + 4, y, HALF_WIDTH, 20,
                        Text.literal(label + " Shows In"),
                        (button, value) -> onSurface.accept(value)));

        return y + ROW_HEIGHT;
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
