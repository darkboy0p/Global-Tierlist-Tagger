package com.gtltagger.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import com.gtltagger.client.KitIcons;
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
    private static final int ICON_SOURCE_PX = 512; // all bundled kit icons are 512x512
    private static final int ROW_HEIGHT = 16;
    private static final int MAX_SUGGESTIONS = 6;
    private static final int SUGGESTION_HEIGHT = 14;
    private static final int FIELD_WIDTH = 200;
    private static final int FIELD_Y = 30;

    private final Screen parent;
    private TextFieldWidget ignField;

    private String searchedIgn;
    private Map<String, TierEntry> results = Map.of();
    private int scrollRows = 0;

    private List<String> suggestions = List.of();
    private int selectedSuggestion = -1;
    private boolean suppressChange = false;

    public PlayerSearchScreen(Screen parent) {
        super(Text.literal("GTLTagger Player Search"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int fieldX = centerX - FIELD_WIDTH / 2 - 32;

        ignField = new TextFieldWidget(this.textRenderer, fieldX, FIELD_Y, FIELD_WIDTH, 20, Text.literal("Player name"));
        ignField.setMaxLength(32);
        ignField.setChangedListener(this::onTextChanged);
        this.addDrawableChild(ignField);
        this.setInitialFocus(ignField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Search"), button -> search(ignField.getText()))
                .dimensions(fieldX + FIELD_WIDTH + 4, FIELD_Y, 60, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> this.close())
                .dimensions(centerX - 75, this.height - 28, 150, 20).build());

        // Re-open on the previous search after a window resize.
        if (searchedIgn != null) {
            suppressChange = true;
            ignField.setText(searchedIgn);
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
        return !(suggestions.size() == 1 && suggestions.get(0).equalsIgnoreCase(ignField.getText().trim()));
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
        ignField.setText(name);
        suppressChange = false;
        search(name);
    }

    // ---------------------------------------------------------------- input

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ignField != null && ignField.isFocused()) {
            boolean open = dropdownVisible();
            if (open && keyCode == GLFW.GLFW_KEY_DOWN) {
                selectedSuggestion = (selectedSuggestion + 1) % suggestions.size();
                return true;
            }
            if (open && keyCode == GLFW.GLFW_KEY_UP) {
                selectedSuggestion = selectedSuggestion <= 0 ? suggestions.size() - 1 : selectedSuggestion - 1;
                return true;
            }
            if (open && keyCode == GLFW.GLFW_KEY_TAB) {
                choose(suggestions.get(selectedSuggestion >= 0 ? selectedSuggestion : 0));
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                if (open && selectedSuggestion >= 0) {
                    choose(suggestions.get(selectedSuggestion));
                } else {
                    search(ignField.getText());
                }
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            scrollRows += visibleRows();
            clampScroll();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            scrollRows -= visibleRows();
            clampScroll();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && dropdownVisible()) {
            int idx = suggestionAt(mouseX, mouseY);
            if (idx >= 0) {
                choose(suggestions.get(idx));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int y = FIELD_Y + 28;

        if (dropdownVisible()) {
            // Results are not drawn while the dropdown is open, so the two never overlap.
            renderDropdown(drawContext, mouseX, mouseY);
            return;
        }

        if (!TierDatabase.hasData()) {
            drawContext.drawCenteredTextWithShadow(
                    this.textRenderer, "Loading tier data...", centerX, y, 0xAAAAAA);
            return;
        }

        String typed = ignField.getText().trim();
        if (typed.isEmpty() && searchedIgn == null) {
            drawContext.drawCenteredTextWithShadow(this.textRenderer,
                    "Type a player's name - " + TierDatabase.playerCount() + " players loaded", centerX, y, 0xAAAAAA);
            return;
        }

        if (searchedIgn == null) {
            drawContext.drawCenteredTextWithShadow(
                    this.textRenderer, "Press Enter or Search to look up \"" + typed + "\"", centerX, y, 0xAAAAAA);
            return;
        }

        if (results.isEmpty()) {
            drawContext.drawCenteredTextWithShadow(
                    this.textRenderer, "No tested tiers found for " + searchedIgn, centerX, y, 0xAAAAAA);
            return;
        }

        drawContext.drawCenteredTextWithShadow(this.textRenderer,
                searchedIgn + " - " + results.size() + " gamemode" + (results.size() == 1 ? "" : "s"),
                centerX, y, 0xFFFFFF);

        clampScroll();
        int rows = visibleRows();
        int rowX = centerX - 100;
        int rowY = resultsTop();

        List<Map.Entry<String, TierEntry>> list = new ArrayList<>(results.entrySet());
        for (int i = scrollRows; i < Math.min(list.size(), scrollRows + rows); i++) {
            String gamemode = list.get(i).getKey();
            TierEntry entry = list.get(i).getValue();

            // Gamemodes without bundled art (newly added ones) simply show no icon.
            Identifier icon = KitIcons.get(gamemode);
            if (icon != null) {
                HudIconRenderer.drawIcon(drawContext, icon, rowX, rowY, ICON_SIZE, ICON_SOURCE_PX);
            }

            String line = gamemode + ": " + TierText.shortForm(entry.tier)
                    + (entry.peak != null ? "  (Peak: " + TierText.shortForm(entry.peak) + ")" : "");
            int color = TierColors.rgbForTierNumber(TierText.tierNumber(entry.tier));
            drawContext.drawText(this.textRenderer, line, rowX + ICON_SIZE + 4, rowY + 2, color, true);
            rowY += ROW_HEIGHT;
        }

        if (list.size() > rows) {
            String hint = "Scroll for more (" + (scrollRows + 1) + "-" + Math.min(list.size(), scrollRows + rows)
                    + " of " + list.size() + ")";
            drawContext.drawCenteredTextWithShadow(this.textRenderer, hint, centerX, this.height - 40, 0x888888);
        }
    }

    private void renderDropdown(DrawContext drawContext, int mouseX, int mouseY) {
        int x = dropdownX();
        int y = dropdownY();
        int hovered = suggestionAt(mouseX, mouseY);

        drawContext.fill(x, y, x + FIELD_WIDTH, y + suggestions.size() * SUGGESTION_HEIGHT, 0xE0101010);
        for (int i = 0; i < suggestions.size(); i++) {
            int rowY = y + i * SUGGESTION_HEIGHT;
            boolean active = i == selectedSuggestion || i == hovered;
            if (active) {
                drawContext.fill(x, rowY, x + FIELD_WIDTH, rowY + SUGGESTION_HEIGHT, 0x80FFFFFF);
            }
            drawContext.drawText(this.textRenderer, suggestions.get(i), x + 4, rowY + 3,
                    active ? 0xFFFFFF : 0xCCCCCC, true);
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
