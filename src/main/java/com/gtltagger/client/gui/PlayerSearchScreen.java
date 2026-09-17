package com.gtltagger.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.gtltagger.client.KitIcons;
import com.gtltagger.client.hud.HudIconRenderer;
import com.gtltagger.data.TierDatabase;
import com.gtltagger.data.TierEntry;
import com.gtltagger.gamemode.Gamemodes;
import com.gtltagger.tag.TierColors;
import com.gtltagger.tag.TierText;

import java.util.Map;
import java.util.TreeMap;

/**
 * Look up any player's tested tiers (and, unlike the HUD or TAB, their
 * PEAK tier per gamemode) - opened by the "Player Search" keybind (H
 * by default). Reads straight out of {@link TierDatabase}'s already
 * client-side-cached data (refreshed in the background every couple
 * minutes - see TierDatabase.REFRESH_INTERVAL_MINUTES), so a search
 * here never blocks on a network call.
 */
public class PlayerSearchScreen extends Screen {

    private static final int ICON_SIZE = 12;
    private static final int ICON_SOURCE_PX = 256; // all kit icons ship as 256x256
    private static final int ROW_HEIGHT = 16;

    private final Screen parent;
    private TextFieldWidget ignField;
    private String searchedIgn;
    private Map<String, TierEntry> results = Map.of();

    public PlayerSearchScreen(Screen parent) {
        super(Text.literal("GTLTagger Player Search"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        ignField = new TextFieldWidget(this.textRenderer, centerX - 100, 30, 160, 20, Text.literal("Player name"));
        ignField.setMaxLength(16);
        this.addDrawableChild(ignField);
        this.setInitialFocus(ignField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Search"), button -> search())
                .dimensions(centerX + 64, 30, 60, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> this.client.setScreen(parent))
                .dimensions(centerX - 75, this.height - 30, 150, 20).build());
    }

    private void search() {
        String ign = ignField.getText().trim();
        searchedIgn = ign.isEmpty() ? null : ign;
        results = searchedIgn != null ? new TreeMap<>(TierDatabase.getAll(searchedIgn)) : Map.of();
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int y = 64;

        if (searchedIgn == null) {
            drawContext.drawCenteredTextWithShadow(
                    this.textRenderer, "Type a player's name and press Search", centerX, y, 0xAAAAAA);
            return;
        }

        if (results.isEmpty()) {
            drawContext.drawCenteredTextWithShadow(
                    this.textRenderer, "No tested tiers found for " + searchedIgn, centerX, y, 0xAAAAAA);
            return;
        }

        drawContext.drawCenteredTextWithShadow(
                this.textRenderer, searchedIgn + "'s tiers:", centerX, y, 0xFFFFFF);
        y += ROW_HEIGHT;

        int rowX = centerX - 90;
        for (String gamemode : Gamemodes.ORDER) {
            TierEntry entry = results.get(gamemode);
            if (entry == null) {
                continue;
            }

            Identifier icon = KitIcons.get(gamemode);
            if (icon != null) {
                HudIconRenderer.drawIcon(drawContext, icon, rowX, y, ICON_SIZE, ICON_SOURCE_PX);
            }

            String line = gamemode + ": " + TierText.shortForm(entry.tier)
                    + (entry.peak != null ? "  (Peak: " + TierText.shortForm(entry.peak) + ")" : "");
            int color = TierColors.rgbForTierNumber(TierText.tierNumber(entry.tier));
            drawContext.drawText(this.textRenderer, line, rowX + ICON_SIZE + 4, y + 2, color, true);
            y += ROW_HEIGHT;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
