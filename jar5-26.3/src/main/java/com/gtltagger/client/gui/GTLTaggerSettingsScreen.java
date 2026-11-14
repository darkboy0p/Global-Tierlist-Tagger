package com.gtltagger.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import com.gtltagger.client.GTLTaggerClient;
import com.gtltagger.config.DisplaySurface;
import com.gtltagger.client.compat.Mc;
import com.gtltagger.config.GTLTaggerConfig;
import com.gtltagger.config.TierNameFormat;
import com.gtltagger.gamemode.Gamemodes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * GTLTagger's dedicated settings menu. Every option is a plain button
 * (click to cycle / toggle) - deliberately not CyclingButton or
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
        super(Component.literal("GTLTagger Settings"));
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

        this.addRenderableWidget(Button.builder(Component.literal("Edit HUD Position"), button ->
                        Mc.openScreen(this.minecraft, new HudPositionScreen(this, config)))
                .bounds(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20).build());
        y += ROW_HEIGHT + 6;

        addKeyBindRow(centerX, y, "Cycle Left Gamemode", "key.gtltagger.cycle_left_gamemode");
        y += ROW_HEIGHT;
        addKeyBindRow(centerX, y, "Cycle Right Gamemode", "key.gtltagger.cycle_right_gamemode");
        y += ROW_HEIGHT;
        addKeyBindRow(centerX, y, "Search Player (Peek)", "key.gtltagger.open_player_search");
        y += ROW_HEIGHT;
        addKeyBindRow(centerX, y, "Copy My TierTag", "key.gtltagger.copy_tag");
        y += ROW_HEIGHT + 10;

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> {
            config.save();
            Mc.openScreen(this.minecraft, parent);
        }).bounds(centerX - 75, y, 150, 20).build());
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
        this.addRenderableWidget(Button.builder(
                        Component.literal(label + ": " + name.apply(values.get(index[0]))),
                        button -> {
                            index[0] = (index[0] + 1) % values.size();
                            T value = values.get(index[0]);
                            button.setMessage(Component.literal(label + ": " + name.apply(value)));
                            onChange.accept(value);
                        })
                .bounds(x, y, width, 20).build());
    }

    private void addToggle(int x, int y, int width, String label, boolean initial, Consumer<Boolean> onChange) {
        boolean[] state = { initial };
        this.addRenderableWidget(Button.builder(
                        Component.literal(label + ": " + (initial ? "ON" : "OFF")),
                        button -> {
                            state[0] = !state[0];
                            button.setMessage(Component.literal(label + ": " + (state[0] ? "ON" : "OFF")));
                            onChange.accept(state[0]);
                        })
                .bounds(x, y, width, 20).build());
    }

    private void addKeyBindRow(int centerX, int y, String label, String bindingId) {
        this.addRenderableWidget(Button.builder(Component.literal(label), b -> { })
                .bounds(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH / 2 - 4, 20).build()).active = false;

        KeyMapping binding = GTLTaggerClient.binding(bindingId);
        if (binding != null) {
            KeyBindRow row = new KeyBindRow(centerX + 4, y, WIDGET_WIDTH / 2 - 4, 20, binding);
            keyBindRows.add(row);
            this.addRenderableWidget(row.button());
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        for (KeyBindRow row : keyBindRows) {
            if (row.tryConsumeKey(input.key())) {
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public void onClose() {
        config.save();
        Mc.openScreen(this.minecraft, parent);
    }
}
