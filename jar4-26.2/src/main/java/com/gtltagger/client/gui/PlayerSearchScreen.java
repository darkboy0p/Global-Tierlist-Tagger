package com.gtltagger.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import com.gtltagger.client.compat.Mc;
import com.gtltagger.client.hud.HudIconRenderer;
import com.gtltagger.data.TierDatabase;
import com.gtltagger.data.TierEntry;
import com.gtltagger.gamemode.Gamemodes;
import com.gtltagger.tag.TierColors;
import com.gtltagger.tag.TierText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Player search: look up any player's tested tiers (and PEAK tier) in
 * EVERY gamemode, including ones added to the Global Tierlist after
 * this build. Opened by the "Player Search" keybind (H by default).
 *
 * - Type to get live name suggestions (dropdown + ghost-text completion).
 * - Up/Down pick a suggestion, Tab completes, Enter searches.
 * - Results update live once the typed name is an exact match.
 * - Results scroll with the mouse wheel when there are many gamemodes.
 *
 * Everything reads from {@link TierDatabase}'s in-memory snapshot, so
 * nothing here blocks on the network.
 */
public class PlayerSearchScreen extends Screen {

    private static final int ICON_SIZE = 12;
    private static final int ROW_HEIGHT = 16;
    private static final int MAX_SUGGESTIONS = 6;
    private static final int SUGGESTION_HEIGHT = 14;
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_Y = 30;

    private final Screen parent;
    private EditBox ignField;

    private String searchedIgn;
    private Map<String, TierEntry> results = Map.of();
    private int scrollRows = 0;

    private List<String> suggestions = List.of();
    private int selectedSuggestion = -1;
    private boolean suppressChange = false;

    public PlayerSearchScreen(Screen parent) {
        super(Component.literal("GTLTagger Player Search"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int fieldX = centerX - FIELD_WIDTH / 2 - 32;

        ignField = new EditBox(this.font, fieldX, FIELD_Y, FIELD_WIDTH, 20, Component.literal("Player name"));
        ignField.setMaxLength(32);
        ignField.setResponder(this::onTextChanged);
        this.addRenderableWidget(ignField);
        this.setInitialFocus(ignField);

        this.addRenderableWidget(Button.builder(Component.literal("Search"), button -> search(ignField.getValue()))
                .bounds(fieldX + FIELD_WIDTH + 4, FIELD_Y, 60, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> this.onClose())
                .bounds(centerX - 75, this.height - 28, 150, 20).build());

        // Re-open on the previous search after a window resize.
        if (searchedIgn != null) {
            suppressChange = true;
            ignField.setValue(searchedIgn);
            suppressChange = false;
        }
    }

    // ---------------------------------------------------------------- state

    private void onTextChanged(String text) {
        if (suppressChange) {
            return;
        }
        suggestions = TierDatabase.suggest(text, MAX_SUGGESTIONS);
        selectedSuggestion = -1;

        // Ghost-text completion from the best prefix match.
        String ghost = null;
        if (!suggestions.isEmpty()) {
            String first = suggestions.get(0);
            if (first.length() > text.length() && first.regionMatches(true, 0, text, 0, text.length())) {
                ghost = first.substring(text.length());
            }
        }
        ignField.setSuggestion(ghost);

        // Live results once the text is an exact (case-insensitive) match.
        String canonical = TierDatabase.canonicalName(text.trim());
        if (canonical != null) {
            applyResults(canonical);
        } else {
            searchedIgn = null;
            results = Map.of();
        }
    }

    private boolean dropdownVisible() {
        if (suggestions.isEmpty()) {
            return false;
        }
        // Hide when the only suggestion is exactly what's typed.
        return !(suggestions.size() == 1 && suggestions.get(0).equalsIgnoreCase(ignField.getValue().trim()));
    }

    private void search(String raw) {
        String ign = raw == null ? "" : raw.trim();
        if (ign.isEmpty()) {
            searchedIgn = null;
            results = Map.of();
            return;
        }
        String canonical = TierDatabase.canonicalName(ign);
        applyResults(canonical != null ? canonical : ign);
        suggestions = List.of();
        selectedSuggestion = -1;
        ignField.setSuggestion(null);
    }

    private void applyResults(String ign) {
        searchedIgn = ign;
        scrollRows = 0;
        Map<String, TierEntry> found = TierDatabase.getAll(ign);
        Map<String, TierEntry> ordered = new LinkedHashMap<>();
        // Stable order: core kits first, then discovered ones (Gamemodes.all()).
        for (String mode : Gamemodes.all()) {
            TierEntry e = found.get(mode);
            if (e != null) {
                ordered.put(mode, e);
            }
        }
        // Safety net: anything not in the registry yet.
        for (Map.Entry<String, TierEntry> e : found.entrySet()) {
            ordered.putIfAbsent(e.getKey(), e.getValue());
        }
        results = ordered;
    }

    private void choose(String name) {
        suppressChange = true;
        ignField.setValue(name);
        suppressChange = false;
        search(name);
    }

    // ---------------------------------------------------------------- input

    @Override
    public boolean keyPressed(KeyEvent input) {
        int keyCode = input.key();
        if (ignField != null && ignField.isFocused()) {
            boolean open = dropdownVisible();
            if (open && keyCode == InputConstants.KEY_DOWN) {
                selectedSuggestion = (selectedSuggestion + 1) % suggestions.size();
                return true;
            }
            if (open && keyCode == InputConstants.KEY_UP) {
                selectedSuggestion = selectedSuggestion <= 0 ? suggestions.size() - 1 : selectedSuggestion - 1;
                return true;
            }
            if (open && keyCode == InputConstants.KEY_TAB) {
                choose(suggestions.get(selectedSuggestion >= 0 ? selectedSuggestion : 0));
                return true;
            }
            if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
                if (open && selectedSuggestion >= 0) {
                    choose(suggestions.get(selectedSuggestion));
                } else {
                    search(ignField.getValue());
                }
                return true;
            }
        }
        if (keyCode == InputConstants.KEY_PAGEDOWN) {
            scrollRows += visibleRows();
            clampScroll();
            return true;
        }
        if (keyCode == InputConstants.KEY_PAGEUP) {
            scrollRows -= visibleRows();
            clampScroll();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == InputConstants.MOUSE_BUTTON_LEFT && dropdownVisible()) {
            int idx = suggestionAt(click.x(), click.y());
            if (idx >= 0) {
                choose(suggestions.get(idx));
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!results.isEmpty() && !dropdownVisible()) {
            scrollRows -= (int) Math.signum(verticalAmount);
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private int dropdownX() {
        return ignField.getX();
    }

    private int dropdownY() {
        return ignField.getY() + ignField.getHeight() + 1;
    }

    private int suggestionAt(double mouseX, double mouseY) {
        int x = dropdownX();
        int y = dropdownY();
        if (mouseX < x || mouseX >= x + FIELD_WIDTH || mouseY < y) {
            return -1;
        }
        int idx = (int) ((mouseY - y) / SUGGESTION_HEIGHT);
        return idx >= 0 && idx < suggestions.size() ? idx : -1;
    }

    private int resultsTop() {
        return FIELD_Y + 50;
    }

    private int visibleRows() {
        return Math.max(1, (this.height - 38 - resultsTop()) / ROW_HEIGHT);
    }

    private void clampScroll() {
        int max = Math.max(0, results.size() - visibleRows());
        scrollRows = Math.max(0, Math.min(scrollRows, max));
    }

    // --------------------------------------------------------------- render

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int y = FIELD_Y + 28;

        if (dropdownVisible()) {
            // Results are not drawn while the dropdown is open, so the two never overlap.
            renderDropdown(graphics, mouseX, mouseY);
            return;
        }

        if (!TierDatabase.hasData()) {
            graphics.centeredText(
                    this.font, "Loading tier data...", centerX, y, 0xFFAAAAAA);
            return;
        }

        String typed = ignField.getValue().trim();
        if (typed.isEmpty() && searchedIgn == null) {
            graphics.centeredText(this.font,
                    "Type a player's name - " + TierDatabase.playerCount() + " players loaded", centerX, y, 0xFFAAAAAA);
            return;
        }

        if (searchedIgn == null) {
            graphics.centeredText(
                    this.font, "Press Enter or Search to look up \"" + typed + "\"", centerX, y, 0xFFAAAAAA);
            return;
        }

        if (results.isEmpty()) {
            graphics.centeredText(
                    this.font, "No tested tiers found for " + searchedIgn, centerX, y, 0xFFAAAAAA);
            return;
        }

        graphics.centeredText(this.font,
                searchedIgn + " - " + results.size() + " gamemode" + (results.size() == 1 ? "" : "s"),
                centerX, y, 0xFFFFFFFF);

        clampScroll();
        int rows = visibleRows();
        int rowX = centerX - 100;
        int rowY = resultsTop();

        List<Map.Entry<String, TierEntry>> list = new ArrayList<>(results.entrySet());
        for (int i = scrollRows; i < Math.min(list.size(), scrollRows + rows); i++) {
            String gamemode = list.get(i).getKey();
            TierEntry entry = list.get(i).getValue();

            // Gamemodes without bundled art (newly added ones) simply show no icon.
            HudIconRenderer.drawIcon(graphics, this.font, gamemode, rowX, rowY, false);

            String line = gamemode + ": " + TierText.shortForm(entry.tier)
                    + (entry.peak != null ? "  (Peak: " + TierText.shortForm(entry.peak) + ")" : "");
            int color = 0xFF000000 | TierColors.rgbForTierNumber(TierText.tierNumber(entry.tier));
            graphics.text(this.font, line, rowX + ICON_SIZE + 4, rowY + 2, color, true);
            rowY += ROW_HEIGHT;
        }

        if (list.size() > rows) {
            String hint = "Scroll for more (" + (scrollRows + 1) + "-" + Math.min(list.size(), scrollRows + rows)
                    + " of " + list.size() + ")";
            graphics.centeredText(this.font, hint, centerX, this.height - 40, 0xFF888888);
        }
    }

    private void renderDropdown(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = dropdownX();
        int y = dropdownY();
        int hovered = suggestionAt(mouseX, mouseY);

        graphics.fill(x, y, x + FIELD_WIDTH, y + suggestions.size() * SUGGESTION_HEIGHT, 0xE0101010);
        for (int i = 0; i < suggestions.size(); i++) {
            int rowY = y + i * SUGGESTION_HEIGHT;
            boolean active = i == selectedSuggestion || i == hovered;
            if (active) {
                graphics.fill(x, rowY, x + FIELD_WIDTH, rowY + SUGGESTION_HEIGHT, 0x80FFFFFF);
            }
            graphics.text(this.font, suggestions.get(i), x + 4, rowY + 3,
                    active ? 0xFFFFFFFF : 0xFFCCCCCC, true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Mc.openScreen(this.minecraft, parent);
    }
}
