package com.gtltagger.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import com.gtltagger.client.GTLTaggerClient;
import com.gtltagger.config.DisplaySurface;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.config.TierNameFormat;
import com.gtltagger.gamemode.Gamemodes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * GTLTagger's dedicated settings menu. Every option is a plain button
 * (click to cycle / toggle) - deliberately not CyclingButtonWidget or
 * CheckboxWidget, whose builder signatures keep changing between
 * Minecraft versions.
 */
public class GTLTaggerSettingsScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int WIDGET_WIDTH = 200;
    private static final int HALF_WIDTH = WIDGET_WIDTH / 2 - 4;

    private final Screen parent;
    private final GTLTaggerConfig config;
    private final List<KeyBindRow> keyBindRows = new ArrayList<>();

    public GTLTaggerSettingsScreen(Screen parent, GTLTaggerConfig config) {
        super(Text.literal("GTLTagger Settings"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        keyBindRows.clear();
        int centerX = this.width / 2;
        int y = 24;

        y = addSideRow(centerX, y, "Left", config.gamemode1, config.leftSurface,
                gamemode -> { config.gamemode1 = gamemode; config.save(); },
                surface -> { config.leftSurface = surface; config.save(); });
        y = addSideRow(centerX, y, "Right", config.gamemode2, config.rightSurface,
                gamemode -> { config.gamemode2 = gamemode; config.save(); },
                surface -> { config.rightSurface = surface; config.save(); });
        y += 6;

        addCycler(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, "Tier Text Format (Tag only)",
                List.of(TierNameFormat.values()), config.tierNameFormat, f -> f.label,
                value -> { config.tierNameFormat = value; config.save(); });
        y += ROW_HEIGHT + 6;

        addToggle(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, "Automatic Kit Detection",
                config.automaticDetectionEnabled,
                on -> { config.automaticDetectionEnabled = on; config.save(); });
        y += ROW_HEIGHT;

        addToggle(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, "Enable HUD", config.hudEnabled,
                on -> { config.hudEnabled = on; config.save(); });
        y += ROW_HEIGHT;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Edit HUD Position"), button ->
                        this.client.setScreen(new HudPositionScreen(this, config)))
                .dimensions(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20).build());
        y += ROW_HEIGHT + 6;

        addKeyBindRow(centerX, y, "Cycle Left Gamemode", "key.gtltagger.cycle_left_gamemode");
        y += ROW_HEIGHT;
        addKeyBindRow(centerX, y, "Cycle Right Gamemode", "key.gtltagger.cycle_right_gamemode");
        y += ROW_HEIGHT;
        addKeyBindRow(centerX, y, "Search Player (Peek)", "key.gtltagger.open_player_search");
        y += ROW_HEIGHT;
        addKeyBindRow(centerX, y, "Copy My TierTag", "key.gtltagger.copy_tag");
        y += ROW_HEIGHT + 10;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            config.save();
            this.client.setScreen(parent);
        }).dimensions(centerX - 75, y, 150, 20).build());
    }

    private int addSideRow(int centerX, int y, String label, String currentGamemode, DisplaySurface currentSurface,
                           Consumer<String> onGamemode, Consumer<DisplaySurface> onSurface) {
        addCycler(centerX - WIDGET_WIDTH / 2, y, HALF_WIDTH, label + " Gamemode",
                Gamemodes.all(), currentGamemode, g -> g, onGamemode);
        addCycler(centerX + 4, y, HALF_WIDTH, label + " Shows In",
                List.of(DisplaySurface.values()), currentSurface, s -> s.label, onSurface);
        return y + ROW_HEIGHT;
    }

    /** A button that shows "label: value" and advances to the next value on each click. */
    private <T> void addCycler(int x, int y, int width, String label, List<T> values, T current,
                               Function<T, String> name, Consumer<T> onChange) {
        int[] index = { Math.max(0, values.indexOf(current)) };
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal(label + ": " + name.apply(values.get(index[0]))),
                        button -> {
                            index[0] = (index[0] + 1) % values.size();
                            T value = values.get(index[0]);
                            button.setMessage(Text.literal(label + ": " + name.apply(value)));
                            onChange.accept(value);
                        })
                .dimensions(x, y, width, 20).build());
    }

    private void addToggle(int x, int y, int width, String label, boolean initial, Consumer<Boolean> onChange) {
        boolean[] state = { initial };
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal(label + ": " + (initial ? "ON" : "OFF")),
                        button -> {
                            state[0] = !state[0];
                            button.setMessage(Text.literal(label + ": " + (state[0] ? "ON" : "OFF")));
                            onChange.accept(state[0]);
                        })
                .dimensions(x, y, width, 20).build());
    }

    private void addKeyBindRow(int centerX, int y, String label, String bindingId) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> { })
                .dimensions(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH / 2 - 4, 20).build()).active = false;

        KeyBinding binding = GTLTaggerClient.binding(bindingId);
        if (binding != null) {
            KeyBindRow row = new KeyBindRow(centerX + 4, y, WIDGET_WIDTH / 2 - 4, 20, binding);
            keyBindRows.add(row);
            this.addDrawableChild(row.button());
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        for (KeyBindRow row : keyBindRows) {
            if (row.tryConsumeKey(input.key())) {
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        config.save();
        this.client.setScreen(parent);
    }
}
