package com.gtltagger.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import com.gtltagger.client.hud.GTLTaggerHud;
import com.gtltagger.config.GTLTaggerConfig;

import java.util.List;

/**
 * Lets the user drag the GTLTagger HUD to a new position (spec
 * section 7 — "HUD editor/settings option ... reposition it").
 * Click-drag the preview box, then "Done" to save.
 */
public class HudPositionScreen extends Screen {

    private final Screen parent;
    private final GTLTaggerConfig config;
    private boolean dragging = false;

    protected HudPositionScreen(Screen parent, GTLTaggerConfig config) {
        super(Text.literal("Position GTLTagger HUD"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            config.save();
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 30, 150, 20).build());
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);

        drawContext.drawCenteredTextWithShadow(
                this.textRenderer,
                "Drag the box below to reposition the HUD",
                this.width / 2, 20, 0xFFFFFF
        );

        List<GTLTaggerHud.HudLine> lines = GTLTaggerHud.buildLines(this.client, config);
        if (lines.isEmpty()) {
            lines = List.of(
                    new GTLTaggerHud.HudLine("GTLTagger"),
                    new GTLTaggerHud.HudLine("Kit: NethPot"),
                    new GTLTaggerHud.HudLine("Tier: ?"));
        }
        GTLTaggerHud.renderBox(drawContext, this.client, lines, config.hudX, config.hudY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInsideHud(mouseX, mouseY)) {
            dragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            config.hudX = Math.max(0, (int) (config.hudX + deltaX));
            config.hudY = Math.max(0, (int) (config.hudY + deltaY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isInsideHud(double mouseX, double mouseY) {
        // Generous fixed hit box around the HUD's anchor point — good enough for a drag handle.
        int width = 120;
        int height = 60;
        return mouseX >= config.hudX && mouseX <= config.hudX + width
                && mouseY >= config.hudY && mouseY <= config.hudY + height;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
